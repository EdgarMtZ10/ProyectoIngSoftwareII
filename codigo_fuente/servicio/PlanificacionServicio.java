package servicio;

import datos.ActividadDao;
import datos.CalendarioDao;
import datos.EntregableDao;
import datos.EtapaDao;
import datos.ProyectoCriterioDao;
import datos.ProyectoDao;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.Actividad;
import modelo.Entregable;
import modelo.EstadoEntregable;
import modelo.EstadoEtapa;
import modelo.EstadoProyecto;
import modelo.Etapa;
import modelo.ItemCalendario;
import modelo.Proyecto;
import modelo.ProyectoCriterio;
import modelo.RolUsuario;
import util.Validaciones;

import java.time.LocalDate;
import java.util.List;

/**
 * Implementa el Caso de Uso 02: creación y reprogramación de etapas, actividades y entregables,
 * validación estricta de rangos de fecha y consulta unificada para el calendario visual.
 */
public class PlanificacionServicio {

    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final ProyectoCriterioDao proyectoCriterioDao = new ProyectoCriterioDao();
    private final EtapaDao etapaDao = new EtapaDao();
    private final ActividadDao actividadDao = new ActividadDao();
    private final EntregableDao entregableDao = new EntregableDao();
    private final CalendarioDao calendarioDao = new CalendarioDao();

    /**
     * Crea una nueva etapa dentro de un criterio de proyecto asignándole su orden secuencial.
     */
    public Etapa crearEtapa(int idProyectoCriterio, String nombre, LocalDate fechaInicio, LocalDate fechaCierre) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        Validaciones.noVacio(nombre, "nombre de la etapa");
        Validaciones.maxLargo(nombre, 150, "nombre de la etapa");
        Validaciones.rangoFechas(fechaInicio, fechaCierre, "de la etapa");

        return TransaccionHelper.ejecutarEnTransaccion(c -> {
            ProyectoCriterio pc = proyectoCriterioDao.buscarPorId(c, idProyectoCriterio);
            if (pc == null) {
                throw new ValidacionException("El criterio del proyecto seleccionado no existe.");
            }
            Proyecto proyecto = ProyectoServicio.verificarEditable(c, proyectoDao, pc.getIdProyecto());

            int orden = etapaDao.obtenerSiguienteOrden(c, idProyectoCriterio);
            Etapa etapa = new Etapa();
            etapa.setIdProyectoCriterio(idProyectoCriterio);
            etapa.setNombre(nombre.trim());
            etapa.setOrden(orden);
            etapa.setFechaInicio(fechaInicio);
            etapa.setFechaCierre(fechaCierre);
            etapa.setEstado(EstadoEtapa.PENDIENTE);

            int idEtapa = etapaDao.insertar(c, etapa);
            if (proyecto.getEstado() == EstadoProyecto.EN_PLANIFICACION) {
                proyectoDao.actualizarEstado(c, proyecto.getIdProyecto(), EstadoProyecto.EN_EJECUCION);
            }
            TransaccionHelper.auditar(c, "etapa", idEtapa, AccionHistorial.CREAR);
            return etapa;
        });
    }

    /**
     * Crea una actividad dentro de una etapa validando que sus fechas estén contenidas
     * dentro del rango de fechas de la etapa padre (CU-02).
     */
    public Actividad crearActividad(int idEtapa, String nombre, String responsable,
                                    LocalDate fechaInicio, LocalDate fechaCierre) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        Validaciones.noVacio(nombre, "nombre de la actividad");
        Validaciones.maxLargo(nombre, 150, "nombre de la actividad");
        Validaciones.noVacio(responsable, "responsable");
        Validaciones.maxLargo(responsable, 100, "responsable");
        Validaciones.rangoFechas(fechaInicio, fechaCierre, "de la actividad");

        return TransaccionHelper.ejecutarEnTransaccion(c -> {
            Etapa etapa = etapaDao.buscarPorId(c, idEtapa);
            if (etapa == null) {
                throw new ValidacionException("La etapa seleccionada no existe.");
            }
            int idProyecto = etapaDao.obtenerIdProyectoPorEtapa(c, idEtapa);
            ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);

            validarActividadDentroDeEtapa(fechaInicio, fechaCierre, etapa);

            Actividad act = new Actividad();
            act.setIdEtapa(idEtapa);
            act.setNombre(nombre.trim());
            act.setResponsable(responsable.trim());
            act.setFechaInicio(fechaInicio);
            act.setFechaCierre(fechaCierre);
            act.setTerminada(false);

            int idAct = actividadDao.insertar(c, act);
            if (etapa.getEstado() == EstadoEtapa.COMPLETADA) {
                etapaDao.actualizarEstado(c, idEtapa, EstadoEtapa.EN_PROGRESO);
            }
            TransaccionHelper.auditar(c, "actividad", idAct, AccionHistorial.CREAR);
            return act;
        });
    }

    /**
     * Crea un entregable asociado a una actividad en estado {@code PENDIENTE}.
     */
    public Entregable crearEntregable(int idActividad, String nombre) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        Validaciones.noVacio(nombre, "nombre del entregable");
        Validaciones.maxLargo(nombre, 150, "nombre del entregable");

        return TransaccionHelper.ejecutarEnTransaccion(c -> {
            Actividad act = actividadDao.buscarPorId(c, idActividad);
            if (act == null) {
                throw new ValidacionException("La actividad seleccionada no existe.");
            }
            int idProyecto = etapaDao.obtenerIdProyectoPorEtapa(c, act.getIdEtapa());
            ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);

            Entregable ent = new Entregable();
            ent.setIdActividad(idActividad);
            ent.setNombre(nombre.trim());
            ent.setEstado(EstadoEntregable.PENDIENTE);

            int idEnt = entregableDao.insertar(c, ent);
            Etapa etapa = etapaDao.buscarPorId(c, act.getIdEtapa());
            if (etapa != null && etapa.getEstado() == EstadoEtapa.COMPLETADA) {
                etapaDao.actualizarEstado(c, etapa.getIdEtapa(), EstadoEtapa.EN_PROGRESO);
            }
            TransaccionHelper.auditar(c, "entregable", idEnt, AccionHistorial.CREAR);
            return ent;
        });
    }

    /**
     * Reprograma las fechas de una etapa desde el calendario verificando que ninguna de sus
     * actividades existentes quede fuera del nuevo rango (CU-02).
     */
    public void reprogramarEtapa(int idEtapa, LocalDate nuevoInicio, LocalDate nuevoCierre) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        Validaciones.rangoFechas(nuevoInicio, nuevoCierre, "de la etapa");

        TransaccionHelper.ejecutarEnTransaccion(c -> {
            Etapa etapa = etapaDao.buscarPorId(c, idEtapa);
            if (etapa == null) {
                throw new ValidacionException("La etapa seleccionada no existe.");
            }
            int idProyecto = etapaDao.obtenerIdProyectoPorEtapa(c, idEtapa);
            ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);

            List<Actividad> actividades = actividadDao.listarPorEtapa(c, idEtapa);
            for (Actividad act : actividades) {
                if (act.getFechaInicio().isBefore(nuevoInicio) || act.getFechaCierre().isAfter(nuevoCierre)) {
                    throw new ValidacionException(
                            "No se puede reprogramar la etapa porque la actividad '" + act.getNombre() +
                            "' (" + act.getFechaInicio() + " al " + act.getFechaCierre() +
                            ") quedaría fuera del nuevo rango.");
                }
            }

            etapaDao.actualizarFechas(c, idEtapa, nuevoInicio, nuevoCierre);
            TransaccionHelper.auditar(c, "etapa", idEtapa, AccionHistorial.MODIFICAR);
            return null;
        });
    }

    /**
     * Reprograma las fechas de una actividad validando que se mantenga dentro de su etapa (CU-02).
     */
    public void reprogramarActividad(int idActividad, LocalDate nuevoInicio, LocalDate nuevoCierre) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        Validaciones.rangoFechas(nuevoInicio, nuevoCierre, "de la actividad");

        TransaccionHelper.ejecutarEnTransaccion(c -> {
            Actividad act = actividadDao.buscarPorId(c, idActividad);
            if (act == null) {
                throw new ValidacionException("La actividad seleccionada no existe.");
            }
            Etapa etapa = etapaDao.buscarPorId(c, act.getIdEtapa());
            int idProyecto = etapaDao.obtenerIdProyectoPorEtapa(c, etapa.getIdEtapa());
            ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);

            validarActividadDentroDeEtapa(nuevoInicio, nuevoCierre, etapa);

            actividadDao.actualizarFechas(c, idActividad, nuevoInicio, nuevoCierre);
            TransaccionHelper.auditar(c, "actividad", idActividad, AccionHistorial.MODIFICAR);
            return null;
        });
    }

    public List<Etapa> listarEtapasPorCriterio(int idProyectoCriterio) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> etapaDao.listarPorProyectoCriterio(c, idProyectoCriterio));
    }

    public List<Actividad> listarActividadesPorEtapa(int idEtapa) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> actividadDao.listarPorEtapa(c, idEtapa));
    }

    public List<Entregable> listarEntregablesPorActividad(int idActividad) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> entregableDao.listarPorActividad(c, idActividad));
    }

    /**
     * Devuelve todas las etapas y actividades del proyecto listas para renderizar en el calendario.
     */
    public List<ItemCalendario> obtenerItemsCalendario(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> calendarioDao.listarItemsPorProyecto(c, idProyecto));
    }

    private void validarActividadDentroDeEtapa(LocalDate inicioAct, LocalDate cierreAct, Etapa etapa) {
        if (inicioAct.isBefore(etapa.getFechaInicio()) || cierreAct.isAfter(etapa.getFechaCierre())) {
            throw new ValidacionException(
                    "Las fechas de la actividad deben estar dentro del rango de la etapa '" +
                    etapa.getNombre() + "' (" + etapa.getFechaInicio() + " al " + etapa.getFechaCierre() + ").");
        }
    }
}