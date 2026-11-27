package modelo;

/**
 * Tipos de eventos auditables registrados en la tabla {@code historial}.
 */
public enum AccionHistorial {
    CREAR,
    MODIFICAR,
    APROBAR,
    RECHAZAR,
    FINALIZAR,
    DESACTIVAR,
    BLOQUEAR
}