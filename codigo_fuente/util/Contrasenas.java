package util;

import excepciones.ValidacionException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Gestiona el cifrado y la verificación de contraseñas mediante jBCrypt (prefijo $2a$).
 */
public class Contrasenas {
    /** Factor de trabajo fijo en 10 rondas según estándar del proyecto. */
    private static final int COSTO = 10;

    /**
     * Genera un hash BCrypt seguro a partir de una contraseña en texto plano.
     *
     * @param clave Contraseña en texto plano.
     * @return Hash cifrado con prefijo {@code $2a$}.
     */
    public static String hash(String clave) {
        if (clave == null || clave.isEmpty()) {
            throw new ValidacionException("La contraseña no puede estar vacía.");
        }
        return BCrypt.hashpw(clave, BCrypt.gensalt(COSTO));
    }

    /**
     * Compara una contraseña ingresada en texto plano contra el hash almacenado en MySQL.
     *
     * @param clave        Contraseña digitada por el usuario.
     * @param hashGuardado Hash BCrypt recuperado desde la base de datos.
     * @return {@code true} si coinciden; {@code false} si no coinciden o el hash es inválido.
     */
    public static boolean verificar(String clave, String hashGuardado) {
        if (clave == null || hashGuardado == null || hashGuardado.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(clave, hashGuardado);
        } catch (IllegalArgumentException e) {
            // Protege contra hashes corruptos o con prefijos incompatibles
            return false;
        }
    }
}