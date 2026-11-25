package servicio;

import datos.EtapaDao;
import datos.ProyectoCriterioDao;
import datos.ProyectoDao;
import datos.RecomendacionDao;
import excepciones.BloqueoException;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.EstadoEtapa;
import modelo.Etapa;
import modelo.ProyectoCriterio;
import modelo.Recomendacion;
import modelo.ResultadoRecomendacion;
import modelo.ResumenProyecto.PuntajeCriterioDetalle;
import modelo.RolUsuario;
import util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementa el Caso de Uso 07: calificación individual de los 6 criterios (0 a 100)
 * verificando etapas completadas, cálculo del promedio con igual peso y generación de recomendación.
 */
public class EvaluacionServicio {

    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal UMBRAL_VIABLE = new BigDecimal("70.00");
    private static final BigDecimal UMBRAL_AJUSTES = new BigDecimal("40.00");

    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final ProyectoCriterioDao proyectoCriterioDao = new ProyectoCriterioDao();
    private final EtapaDao etapaDao = new EtapaDao();
    private final RecomendacionDao recomendacionDao = new RecomendacionDao();

    /**
     * Asigna un puntaje de 0.00 a 100.00 a un criterio siempre que todas sus etapas
     * estén en estado {@code COMPLETADA} (CU-07).
     */
    public void evaluarCriterio(int idProyectoCriterio, BigDecimal puntaje) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        if (puntaje == null) {
            throw new ValidacionException("Debe ingresar un puntaje numérico entre 0 y 100.");
        }
        BigDecimal puntajeNormalizado = puntaje.setScale(2, RoundingMode.HALF_UP);
        if (puntajeNormalizado.compareTo(BigDecimal.ZERO) < 0 || puntajeNormalizado.compareTo(CIEN) > 0) {
            throw new ValidacionException("El puntaje del criterio debe estar entre 0.00 y 100.00.");
        }

        TransaccionHelper.ejecutarEnTransaccion(c -> {
            ProyectoCriterio pc = proyectoCriterioDao.buscarPorId(c, idProyectoCriterio);
            if (pc == null) {
                throw new ValidacionException("El criterio seleccionado no existe.");
            }
            ProyectoServicio.verificarEditable(c, proyectoDao, pc.getIdProyecto());

            List<Etapa> etapas = etapaDao.listarPorProyectoCriterio(c, idProyectoCriterio);
            if (etapas.isEmpty()) {
                throw new BloqueoException("No se puede evaluar este criterio porque aún no tiene etapas planificadas.");
            }
            for (Etapa etapa : etapas) {
                if (etapa.getEstado() != EstadoEtapa.COMPLETADA) {
                    throw new BloqueoException(
                            "No se puede evaluar el criterio porque la etapa '" + etapa.getNombre() +
                            "' aún no está completada (estado actual: " + etapa.getEstado().name() + ").");
                }
            }

            proyectoCriterioDao.actualizarPuntaje(c, idProyectoCriterio, puntajeNormalizado);
            TransaccionHelper.auditar(c, "proyecto_criterio", idProyectoCriterio, AccionHistorial.MODIFICAR);
            return null;
        });
    }

    /**
     * Calcula el promedio aritmético de los 6 criterios (igual peso) y guarda el dictamen:
     * - >= 70.00: {@code VIABLE}
     * - 40.00 a 69.99: {@code VIABLE_CON_AJUSTES}
     * - < 40.00: {@code NO_VIABLE}
     */
    public Recomendacion generarRecomendacion(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);

        return TransaccionHelper.ejecutarEnTransaccion(c -> {
            ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);

            List<PuntajeCriterioDetalle> criterios = proyectoCriterioDao.listarDetallePorProyecto(c, idProyecto);
            if (criterios.size() != 6) {
                throw new ValidacionException("El proyecto no tiene los 6 criterios requeridos.");
            }

            BigDecimal suma = BigDecimal.ZERO;
            int faltantes = 0;
            for (PuntajeCriterioDetalle det : criterios) {
                if (det.getPuntaje() == null) {
                    faltantes++;
                } else {
                    suma = suma.add(det.getPuntaje());
                }
            }
            if (faltantes > 0) {
                throw new ValidacionException(
                        "Faltan " + faltantes + " criterio(s) por evaluar antes de generar la recomendación.");
            }

            BigDecimal promedio = suma.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
            ResultadoRecomendacion resultado;
            if (promedio.compareTo(UMBRAL_VIABLE) >= 0) {
                resultado = ResultadoRecomendacion.VIABLE;
            } else if (promedio.compareTo(UMBRAL_AJUSTES) >= 0) {
                resultado = ResultadoRecomendacion.VIABLE_CON_AJUSTES;
            } else {
                resultado = ResultadoRecomendacion.NO_VIABLE;
            }

            Recomendacion rec = new Recomendacion();
            rec.setIdProyecto(idProyecto);
            rec.setPromedio(promedio);
            rec.setResultado(resultado);
            rec.setFechaGeneracion(LocalDateTime.now());

            int idRec = recomendacionDao.guardarOActualizar(c, rec);
            TransaccionHelper.auditar(c, "recomendacion", idRec, AccionHistorial.CREAR);
            return rec;
        });
    }

    public Recomendacion obtenerRecomendacion(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> recomendacionDao.buscarPorProyecto(c, idProyecto));
    }
}