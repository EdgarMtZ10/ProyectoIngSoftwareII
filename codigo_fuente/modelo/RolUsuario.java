package modelo;

/**
 * Roles disponibles para los usuarios del sistema.
 * Deben coincidir exactamente con los valores permitidos en la base de datos.
 */
public enum RolUsuario {
    /** Registra ideas, planifica etapas/actividades, ingresa datos y consulta avances. */
    ANALISTA,
    /** Revisa y aprueba o rechaza los entregables asociados a las actividades. */
    APROBADOR,
    /** Administra las cuentas, roles y activación de usuarios del sistema. */
    ADMINISTRADOR
}