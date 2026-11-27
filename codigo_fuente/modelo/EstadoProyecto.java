package modelo;

/**
 * Estados posibles del ciclo de vida de un proyecto de estudio de factibilidad.
 */
public enum EstadoProyecto {
    /** Estado inicial al crear el proyecto; permite definir etapas y actividades. */
    EN_PLANIFICACION,
    /** El proyecto está en marcha ejecutando actividades y recopilando datos. */
    EN_EJECUCION,
    /** Proyecto cerrado tras obtener recomendación; queda en modo solo lectura. */
    FINALIZADO,
    /** Proyecto descartado antes de concluir; queda en modo solo lectura. */
    CANCELADO
}