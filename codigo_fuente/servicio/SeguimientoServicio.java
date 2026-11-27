package servicio;

import datos.ActividadDao;
import datos.AvanceDao;
import datos.EntregableDao;
import datos.EtapaDao;
import datos.ProyectoDao;
import excepciones.BloqueoException;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.Actividad;
import modelo.Atraso;
import modelo.Avance;
import modelo.Avance.AvanceEtapa;
import modelo.EstadoEtapa;
import modelo.Etapa;
import modelo.RolUsuario;
import util.ConexionBD;
import util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementa los Casos de Uso 05 y 06:
 * - Cálculo automático del avance por etapa y por proyecto (0% si no hay terminadas).
 * - Detección de etapas y actividades atrasadas.
 * - Verificación de bloqueos secuenciales antes de marcar actividades terminadas.
 */
public class SeguimientoServicio {

    private final AvanceDao avanceDao = new AvanceDao();
    private final EtapaDao etapaDao = new EtapaDao();
    private final ActividadDao actividadDao = new ActividadDao();
    private final EntregableDao entregableDao = new EntregableDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();

    /**
     * Recalcula el porcentaje de avance de cada etapa y el porcentaje global del proyecto (CU-05).
     */
    public Avance calcularAvanceProyecto(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> calcularAvanceInterno(c, idProyecto));
    }

    /**
     * Lista todas las etapas no completadas y actividades no terminadas con fecha de cierre vencida (CU-05).
     */
    public List<Atraso> listarAtrasos(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> avanceDao.listarAtrasos(c, idProyecto, LocalDate.now()));
    }

    /**
     * Marca una actividad como terminada previa verificación de que la etapa anterior del mismo criterio
     * tenga todas sus actividades terminadas y todos sus entregables aprobados (CU-06).
     * Tanto la finalización como el bloqueo quedan registrados en el historial.
     */
    public void marcarActividadTerminada(int idActividad) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);

        try (Connection c = ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                Actividad actividad = actividadDao.buscarPorId(c, idActividad);
                if (actividad == null) {
                    throw new ValidacionException("La actividad seleccionada no existe.");
                }

                Etapa etapaActual = etapaDao.buscarPorId(c, actividad.getIdEtapa());
                int idProyecto = etapaDao.obtenerIdProyectoPorEtapa(c, etapaActual.getIdEtapa());
                ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);

                if (actividad.isTerminada()) {
                    throw new ValidacionException("Esta actividad ya fue marcada como terminada.");
                }

                // Verificar bloqueo respecto a la etapa anterior en secuencia (CU-06)
                Etapa etapaAnterior = etapaDao.buscarEtapaAnterior(
                        c, etapaActual.getIdProyectoCriterio(), etapaActual.getOrden());
                if (etapaAnterior != null) {
                    int[] conteoActs = actividadDao.contarTotalesYPendientesPorEtapa(c, etapaAnterior.getIdEtapa());
                    int pendientesAct = conteoActs[1];
                    int noAprobadosEnt = entregableDao.contarNoAprobadosPorEtapa(c, etapaAnterior.getIdEtapa());

                    if (pendientesAct > 0 || noAprobadosEnt > 0) {
                        etapaDao.actualizarEstado(c, etapaActual.getIdEtapa(), EstadoEtapa.BLOQUEADA);
                        TransaccionHelper.auditar(c, "actividad", idActividad, AccionHistorial.BLOQUEAR);
                        c.commit();

                        String motivo = construirMotivoBloqueo(etapaAnterior.getNombre(), pendientesAct, noAprobadosEnt);
                        throw new BloqueoException(motivo);
                    }
                }

                actividadDao.marcarTerminada(c, idActividad);
                recalcularEstadoEtapaInterno(c, etapaActual.getIdEtapa());
                TransaccionHelper.auditar(c, "actividad", idActividad, AccionHistorial.MODIFICAR);
                c.commit();
            } catch (BloqueoException be) {
                throw be;
            } catch (Exception ex) {
                try {
                    c.rollback();
                } catch (SQLException rbEx) {
                    ex.addSuppressed(rbEx);
                }
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new ValidacionException("No se pudo actualizar el estado de la actividad.", ex);
            }
        } catch (SQLException e) {
            throw new ValidacionException("Error de conexión al actualizar la actividad.", e);
        }
    }

    Avance calcularAvanceInterno(Connection c, int idProyecto) {
        List<AvanceEtapa> etapas = avanceDao.listarConteosPorEtapa(c, idProyecto);
        int totalGlobal = 0;
        int terminadasGlobal = 0;

        for (AvanceEtapa ae : etapas) {
            totalGlobal += ae.getTotalActividades();
            terminadasGlobal += ae.getActividadesTerminadas();
            ae.setPorcentaje(calcularPorcentaje(ae.getActividadesTerminadas(), ae.getTotalActividades()));
        }

        Avance avance = new Avance();
        avance.setIdProyecto(idProyecto);
        avance.setTotalActividades(totalGlobal);
        avance.setActividadesTerminadas(terminadasGlobal);
        avance.setPorcentajeGeneral(calcularPorcentaje(terminadasGlobal, totalGlobal));
        avance.setEtapas(etapas);
        return avance;
    }

    void recalcularEstadoEtapaInterno(Connection c, int idEtapa) {
        Etapa etapa = etapaDao.buscarPorId(c, idEtapa);
        if (etapa == null) {
            return;
        }
        int[] conteos = actividadDao.contarTotalesYPendientesPorEtapa(c, idEtapa);
        int total = conteos[0];
        int pendientes = conteos[1];
        int noAprobados = entregableDao.contarNoAprobadosPorEtapa(c, idEtapa);

        EstadoEtapa nuevoEstado;
        if (total > 0 && pendientes == 0 && noAprobados == 0) {
            nuevoEstado = EstadoEtapa.COMPLETADA;
        } else if (total > pendientes) {
            nuevoEstado = EstadoEtapa.EN_PROGRESO;
        } else {
            nuevoEstado = EstadoEtapa.PENDIENTE;
        }

        if (etapa.getEstado() != nuevoEstado) {
            etapaDao.actualizarEstado(c, idEtapa, nuevoEstado);
        }
    }

    private BigDecimal calcularPorcentaje(int terminadas, int total) {
        if (total <= 0 || terminadas <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(terminadas)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private String construirMotivoBloqueo(String nombreEtapaAnterior, int pendientesAct, int noAprobadosEnt) {
        StringBuilder sb = new StringBuilder("Bloqueado: la etapa anterior '")
                .append(nombreEtapaAnterior)
                .append("' no está completa (");
        if (pendientesAct > 0) {
            sb.append("faltan ").append(pendientesAct).append(" actividad(es) por terminar");
        }
        if (pendientesAct > 0 && noAprobadosEnt > 0) {
            sb.append(" y ");
        }
        if (noAprobadosEnt > 0) {
            sb.append("faltan ").append(noAprobadosEnt).append(" entregable(s) por aprobar");
        }
        sb.append(").");
        return sb.toString();
    }
}