package servicio;

import datos.UsuarioDao;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.RolUsuario;
import modelo.Usuario;
import util.Contrasenas;
import util.Sesion;
import util.Validaciones;

import java.util.List;

/**
 * Administración de cuentas de usuario reservada para el rol {@link RolUsuario#ADMINISTRADOR}.
 */
public class UsuarioServicio {

    private final UsuarioDao usuarioDao = new UsuarioDao();

    /**
     * Lista todos los usuarios registrados en el sistema.
     */
    public List<Usuario> listarUsuarios() {
        Validaciones.requiereRol(RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(usuarioDao::listarTodos);
    }

    /**
     * Crea un nuevo usuario cifrando su contraseña en BCrypt y registrando la acción en historial.
     */
    public Usuario crearUsuario(String nombre, String correo, String contrasenaPlana, RolUsuario rol) {
        Validaciones.requiereRol(RolUsuario.ADMINISTRADOR);
        Validaciones.noVacio(nombre, "nombre");
        Validaciones.maxLargo(nombre, 100, "nombre");
        Validaciones.formatoCorreo(correo);
        Validaciones.maxLargo(correo, 100, "correo");
        Validaciones.contrasenaSegura(contrasenaPlana);
        if (rol == null) {
            throw new ValidacionException("Debe seleccionar un rol válido para el usuario.");
        }

        return TransaccionHelper.ejecutarEnTransaccion(c -> {
            if (usuarioDao.buscarPorCorreo(c, correo) != null) {
                throw new ValidacionException("Ya existe un usuario registrado con el correo: " + correo.trim());
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombre(nombre.trim());
            nuevo.setCorreo(correo.trim().toLowerCase());
            nuevo.setContrasena(Contrasenas.hash(contrasenaPlana));
            nuevo.setRol(rol);
            nuevo.setActivo(true);

            int id = usuarioDao.insertar(c, nuevo);
            TransaccionHelper.auditar(c, "usuario", id, AccionHistorial.CREAR);
            return nuevo;
        });
    }

    /**
     * Actualiza los datos generales y el estado activo de un usuario existente.
     */
    public void actualizarUsuario(int idUsuario, String nombre, String correo, RolUsuario rol, boolean activo) {
        Validaciones.requiereRol(RolUsuario.ADMINISTRADOR);
        Validaciones.noVacio(nombre, "nombre");
        Validaciones.maxLargo(nombre, 100, "nombre");
        Validaciones.formatoCorreo(correo);
        Validaciones.maxLargo(correo, 100, "correo");
        if (rol == null) {
            throw new ValidacionException("Debe seleccionar un rol válido.");
        }

        TransaccionHelper.ejecutarEnTransaccion(c -> {
            Usuario existente = usuarioDao.buscarPorId(c, idUsuario);
            if (existente == null) {
                throw new ValidacionException("El usuario seleccionado no existe.");
            }
            if (!activo && Sesion.getUsuario().getIdUsuario() == idUsuario) {
                throw new ValidacionException("No puede desactivar su propia cuenta mientras tiene la sesión abierta.");
            }

            Usuario porCorreo = usuarioDao.buscarPorCorreo(c, correo);
            if (porCorreo != null && porCorreo.getIdUsuario() != idUsuario) {
                throw new ValidacionException("Otro usuario ya utiliza el correo: " + correo.trim());
            }

            boolean seDesactivo = existente.isActivo() && !activo;
            existente.setNombre(nombre.trim());
            existente.setCorreo(correo.trim().toLowerCase());
            existente.setRol(rol);
            existente.setActivo(activo);

            usuarioDao.actualizar(c, existente);
            TransaccionHelper.auditar(c, "usuario", idUsuario,
                    seDesactivo ? AccionHistorial.DESACTIVAR : AccionHistorial.MODIFICAR);
            return null;
        });
    }

    /**
     * Restablece la contraseña de un usuario aplicando hash BCrypt.
     */
    public void cambiarContrasena(int idUsuario, String nuevaContrasena) {
        Validaciones.requiereRol(RolUsuario.ADMINISTRADOR);
        Validaciones.contrasenaSegura(nuevaContrasena);

        TransaccionHelper.ejecutarEnTransaccion(c -> {
            Usuario existente = usuarioDao.buscarPorId(c, idUsuario);
            if (existente == null) {
                throw new ValidacionException("El usuario seleccionado no existe.");
            }
            usuarioDao.actualizarContrasena(c, idUsuario, Contrasenas.hash(nuevaContrasena));
            TransaccionHelper.auditar(c, "usuario", idUsuario, AccionHistorial.MODIFICAR);
            return null;
        });
    }

    /**
     * Crea un usuario administrador por defecto únicamente si la tabla {@code usuario} está vacía.
     * Facilita el primer arranque del sistema sin necesidad de insertar hashes manuales en SQL.
     */
    public void inicializarAdministradorSiVacio(String nombre, String correo, String contrasenaPlana) {
        TransaccionHelper.ejecutarEnTransaccion(c -> {
            if (usuarioDao.listarTodos(c).isEmpty()) {
                Usuario admin = new Usuario();
                admin.setNombre(nombre.trim());
                admin.setCorreo(correo.trim().toLowerCase());
                admin.setContrasena(Contrasenas.hash(contrasenaPlana));
                admin.setRol(RolUsuario.ADMINISTRADOR);
                admin.setActivo(true);
                int id = usuarioDao.insertar(c, admin);
                TransaccionHelper.auditarConUsuario(c, id, "usuario", id, AccionHistorial.CREAR);
            }
            return null;
        });
    }
}