package excepciones;

/**
 * Excepción lanzada cuando una operación está impedida por una regla de secuencia
 * o dependencia del flujo (por ejemplo, intentar cerrar una actividad mientras la etapa
 * anterior tiene actividades pendientes o entregables sin aprobar).
 */
public class BloqueoException extends RuntimeException {

    /**
     * Crea una excepción de bloqueo con el motivo exacto de la restricción.
     *
     * Explicación detallada del bloqueo para el usuario.
     */
    public BloqueoException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea una excepción de bloqueo conservando la causa original.
     *
     * @param mensaje Explicación detallada del bloqueo para el usuario.
     * @param causa   Excepción subyacente.
     */
    public BloqueoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}