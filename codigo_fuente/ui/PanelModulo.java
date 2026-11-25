package ui;

/**
 * Contrato común para todos los paneles funcionales integrados en {@link VentanaPrincipal}.
 * Permite recargar automáticamente los datos cuando el usuario cambia de vista o de proyecto activo.
 */
public interface PanelModulo {

    /**
     * Actualiza las tablas, listas y contadores del módulo desde la base de datos.
     */
    void refrescar();
}