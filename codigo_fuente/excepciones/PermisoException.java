package excepciones;

/**
 * Excepción lanzada cuando el usuario autenticado no posee el rol requerido
 * o no ha iniciado sesión para ejecutar la operación solicitada.
 */
public class PermisoException extends RuntimeException {

    /**
     * Crea una excepción de permisos con un mensaje comprensible para el usuario.
     *
     * @param mensaje Mensaje en español indicando la falta de permisos.
     */
    public PermisoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea una excepción de permisos conservando la causa original.
     *
     * @param mensaje Mensaje en español indicando la falta de permisos.
     * @param causa   Excepción subyacente.
     */
    public PermisoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}