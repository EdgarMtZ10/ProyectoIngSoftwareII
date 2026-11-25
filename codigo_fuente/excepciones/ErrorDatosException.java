package excepciones;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Envuelve errores técnicos ocurridos en la capa de persistencia (MySQL / JDBC)
 * para evitar exponer detalles SQL directamente en la interfaz de usuario.
 */
public class ErrorDatosException extends RuntimeException {

    /**
     * Crea una excepción de acceso a datos con un mensaje amigable y la causa técnica.
     *
     * @param mensaje Mensaje comprensible para el usuario final.
     * @param causa   Excepción SQL o de E/S original.
     */
    public ErrorDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /**
     * Verifica si el error fue provocado por una violación de integridad referencial
     * o restricción única en MySQL (código SQLState clase "23", como llaves duplicadas).
     *
     * @return {@code true} si corresponde a una violación de restricción; {@code false} en caso contrario.
     */
    public boolean esViolacionDeRestriccion() {
        Throwable actual = getCause();
        while (actual != null) {
            if (actual instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            if (actual instanceof SQLException sqlEx) {
                String state = sqlEx.getSQLState();
                // En el estándar SQL, los códigos SQLState que inician con "23" indican violación de integridad
                if (state != null && state.startsWith("23")) {
                    return true;
                }
            }
            actual = actual.getCause();
        }
        return false;
    }
}