package excepciones;

/**
 * Excepción lanzada cuando los datos ingresados por el usuario no cumplen
 * con las reglas de validación (campos vacíos, formatos inválidos, rangos de fecha incorrectos, etc.).
 */
public class ValidacionException extends RuntimeException {

    /**
     * Crea una excepción de validación con un mensaje claro para mostrar en pantalla.
     *
     * @param mensaje Descripción en español del error de validación.
     */
    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    /**
     * Crea una excepción de validación conservando la excepción original como causa.
     *
     * @param mensaje Descripción en español del error de validación.
     * @param causa   Excepción subyacente que originó el fallo.
     */
    public ValidacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}