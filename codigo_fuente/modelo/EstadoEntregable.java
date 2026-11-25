package modelo;

/**
 * Estados de revisión de un entregable vinculado a una actividad.
 */
public enum EstadoEntregable {
    /** En espera de revisión por parte de un usuario con rol APROBADOR. */
    PENDIENTE,
    /** Validado y aceptado por un aprobador. */
    APROBADO,
    /** Devuelto por un aprobador con un motivo obligatorio de rechazo. */
    RECHAZADO
}