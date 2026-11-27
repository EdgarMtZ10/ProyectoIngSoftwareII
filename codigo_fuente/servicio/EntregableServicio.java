package servicio;

import datos.ActividadDao;
import datos.EntregableDao;
import datos.EtapaDao;
import datos.ProyectoDao;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.Actividad;
import modelo.Entregable;
import modelo.EntregablePendiente;
import modelo.EstadoEntregable;
import modelo.RolUsuario;
import util.Sesion;
import util.Validaciones;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementa el Caso de Uso 04: revisión y aprobación/rechazo de entregables exclusivamente
 * por usuarios con rol {@link RolUsuario#APROBADOR}.
 */
public class EntregableServicio {

    private final EntregableDao entregableDao = new EntregableDao();
    private final ActividadDao actividadDao = new ActividadDao();
    private final EtapaDao etapaDao = new EtapaDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final SeguimientoServicio seguimientoServicio = new SeguimientoServicio();

    /**
     * Lista los entregables con contexto jerárquico (proyecto, criterio, etapa, actividad).
     */
    public List<EntregablePendiente> listarEntregables(Integer idProyecto, boolean soloPendientes) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(
                c -> entregableDao.listarConContexto(c, idProyecto, soloPendientes));
    }

    /**
     * Aprueba o rechaza un entregable. Exige motivo obligatorio cuando se rechaza,
     * registra la fecha de revisión, actualiza el estado de la etapa padre y audita en historial.
     */
    public void revisarEntregable(int idEntregable, boolean aprobado, String motivoRechazo) {
        Validaciones.requiereRol(RolUsuario.APROBADOR);
        if (!aprobado) {
            Validaciones.noVacio(motivoRechazo, "motivo de rechazo");
        }

        TransaccionHelper.ejecutarEnTransaccion(c -> {
            Entregable entregable = entregableDao.buscarPorId(c, idEntregable);
            if (entregable == null) {
                throw new ValidacionException("El entregable seleccionado no existe.");
            }
            int idProyecto = entregableDao.obtenerIdProyectoPorEntregable(c, idEntregable);
            ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);

            EstadoEntregable nuevoEstado = aprobado ? EstadoEntregable.APROBADO : EstadoEntregable.RECHAZADO;
            int idAprobador = Sesion.getUsuario().getIdUsuario();

            entregableDao.registrarRevision(
                    c,
                    idEntregable,
                    idAprobador,
                    nuevoEstado,
                    aprobado ? null : motivoRechazo.trim(),
                    LocalDateTime.now()
            );

            Actividad act = actividadDao.buscarPorId(c, entregable.getIdActividad());
            if (act != null) {
                seguimientoServicio.recalcularEstadoEtapaInterno(c, act.getIdEtapa());
            }

            TransaccionHelper.auditar(c, "entregable", idEntregable,
                    aprobado ? AccionHistorial.APROBAR : AccionHistorial.RECHAZAR);
            return null;
        });
    }
}