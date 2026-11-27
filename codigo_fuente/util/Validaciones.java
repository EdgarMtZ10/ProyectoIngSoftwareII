package util;

import excepciones.PermisoException;
import excepciones.ValidacionException;
import modelo.RolUsuario;
import modelo.Usuario;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Métodos utilitarios de validación reutilizados por todos los servicios
 * para garantizar integridad de datos y control de acceso basado en roles.
 */
public class Validaciones {
    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * Verifica que exista una sesión activa y que el rol del usuario pertenezca
     * a la lista de roles autorizados para ejecutar la operación.
     *
     * @param permitidos Roles habilitados para la acción.
     * @throws PermisoException si no hay sesión o el rol no está autorizado.
     */
    public static void requiereRol(RolUsuario... permitidos) {
        Usuario actual = Sesion.getUsuario();
        if (actual == null) {
            throw new PermisoException("Debe iniciar sesión para realizar esta acción.");
        }
        if (permitidos == null || permitidos.length == 0) {
            return;
        }
        for (RolUsuario rol : permitidos) {
            if (actual.getRol() == rol) {
                return;
            }
        }
        throw new PermisoException("Su rol no tiene permiso para realizar esta acción.");
    }

    /**
     * Valida que una cadena de texto no sea nula ni contenga únicamente espacios.
     *
     * @param valor       Texto a verificar.
     * @param nombreCampo Nombre legible del campo para el mensaje de error.
     */
    public static void noVacio(String valor, String nombreCampo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo " + nombreCampo + " es obligatorio.");
        }
    }

    /**
     * Valida que la longitud de una cadena no exceda el máximo permitido en base de datos.
     *
     * @param valor       Texto a verificar.
     * @param max         Cantidad máxima de caracteres permitidos.
     * @param nombreCampo Nombre legible del campo para el mensaje de error.
     */
    public static void maxLargo(String valor, int max, String nombreCampo) {
        if (valor != null && valor.length() > max) {
            throw new ValidacionException(
                    "El campo " + nombreCampo + " no puede superar los " + max + " caracteres.");
        }
    }

 /**
     * Valida que ambas fechas existan y que el cierre no sea anterior al inicio.
     */
    public static void rangoFechas(LocalDate inicio, LocalDate cierre) {
        rangoFechas(inicio, cierre, "");
    }

    /**
     * Valida el rango de fechas incluyendo el contexto en el mensaje de error.
     */
    public static void rangoFechas(LocalDate inicio, LocalDate cierre, String contexto) {
        String sufijo = (contexto == null || contexto.trim().isEmpty()) ? "" : " " + contexto.trim();
        if (inicio == null || cierre == null) {
            throw new ValidacionException("Debe indicar las fechas de inicio y cierre" + sufijo + ".");
        }
        if (cierre.isBefore(inicio)) {
            throw new ValidacionException("La fecha de cierre" + sufijo + " no puede ser anterior a la fecha de inicio.");
        }
    }

    /**
     * Valida que el texto corresponda a una dirección de correo electrónico bien formada.
     *
     * @param correo Correo electrónico a verificar.
     */
    public static void formatoCorreo(String correo) {
        noVacio(correo, "correo");
        if (!PATRON_CORREO.matcher(correo.trim()).matches()) {
            throw new ValidacionException("El formato del correo electrónico no es válido.");
        }
    }

    /**
     * Valida que una contraseña cumpla con la política mínima de seguridad:
     * al menos 8 caracteres, incluyendo como mínimo una letra y un dígito.
     *
     * @param clave Contraseña en texto plano a validar.
     */
    public static void contrasenaSegura(String clave) {
        noVacio(clave, "contraseña");
        if (clave.length() < 8) {
            throw new ValidacionException("La contraseña debe tener al menos 8 caracteres.");
        }
        boolean tieneLetra = false;
        boolean tieneNumero = false;
        for (char c : clave.toCharArray()) {
            if (Character.isLetter(c)) tieneLetra = true;
            if (Character.isDigit(c)) tieneNumero = true;
        }
        if (!tieneLetra || !tieneNumero) {
            throw new ValidacionException("La contraseña debe incluir al menos una letra y un número.");
        }
    }
}