package modelo;

/**
 * Estados operativos de una etapa dentro de un criterio de factibilidad.
 */
public enum EstadoEtapa {
    /** La etapa aún no tiene actividades iniciadas ni terminadas. */
    PENDIENTE,
    /** La etapa tiene trabajo en curso o parcialmente completado. */
    EN_PROGRESO,
    /** Todas las actividades están terminadas y sus entregables aprobados. */
    COMPLETADA,
    /** La etapa no puede avanzar porque la etapa anterior sigue incompleta. */
    BLOQUEADA
}