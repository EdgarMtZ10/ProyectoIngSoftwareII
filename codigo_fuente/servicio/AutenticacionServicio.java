package servicio;

import datos.UsuarioDao;
import excepciones.ValidacionException;
import modelo.Usuario;
import util.Contrasenas;
import util.Sesion;
import util.Validaciones;

/**
 * Gestiona el inicio y cierre de sesión con verificación de contraseña BCrypt (RNF1).
 */
public class AutenticacionServicio {

    private final UsuarioDao usuarioDao = new UsuarioDao();

    /**
     * Valida credenciales, comprueba que la cuenta esté activa e inicia la sesión en memoria.
     *
     * @param correo     Correo electrónico ingresado.
     * @param contrasena Contraseña en texto plano ingresada en el formulario.
     * @return El {@link Usuario} autenticado.
     */
    public Usuario iniciarSesion(String correo, String contrasena) {
        Validaciones.noVacio(correo, "correo electrónico");
        Validaciones.noVacio(contrasena, "contraseña");

        Usuario usuario = TransaccionHelper.ejecutarLectura(c -> usuarioDao.buscarPorCorreo(c, correo));

        if (usuario == null || !Contrasenas.verificar(contrasena, usuario.getContrasena())) {
            throw new ValidacionException("Correo o contraseña incorrectos.");
        }
        if (!usuario.isActivo()) {
            throw new ValidacionException("Esta cuenta de usuario está desactivada. Contacte al administrador.");
        }

        Sesion.iniciar(usuario);
        return usuario;
    }

    /**
     * Cierra la sesión activa y limpia el proyecto seleccionado en memoria.
     */
    public void cerrarSesion() {
        Sesion.cerrar();
    }
}