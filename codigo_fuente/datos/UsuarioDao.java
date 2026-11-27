package datos;

import excepciones.ErrorDatosException;
import modelo.RolUsuario;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de las operaciones CRUD sobre la tabla {@code usuario}.
 */
public class UsuarioDao {

    /**
     * Busca un usuario por su correo electrónico exacto (insensible a mayúsculas en MySQL).
     *
     * @param c      Conexión activa proporcionada por el servicio.
     * @param correo Correo electrónico del usuario.
     * @return El objeto {@link Usuario} si existe, o {@code null} si no se encuentra.
     */
    public Usuario buscarPorCorreo(Connection c, String correo) {
        String sql = "SELECT id_usuario, nombre, correo, contrasena, rol, activo " +
                     "FROM usuario WHERE correo = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, correo.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar el usuario por correo.", e);
        }
    }

    /**
     * Busca un usuario por su identificador único.
     *
     * @param c         Conexión activa.
     * @param idUsuario Identificador del usuario.
     * @return El objeto {@link Usuario} o {@code null} si no existe.
     */
    public Usuario buscarPorId(Connection c, int idUsuario) {
        String sql = "SELECT id_usuario, nombre, correo, contrasena, rol, activo " +
                     "FROM usuario WHERE id_usuario = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar el usuario.", e);
        }
    }

    /**
     * Lista todos los usuarios registrados ordenados alfabéticamente por nombre.
     *
     * @param c Conexión activa.
     * @return Lista completa de usuarios.
     */
    public List<Usuario> listarTodos(Connection c) {
        String sql = "SELECT id_usuario, nombre, correo, contrasena, rol, activo " +
                     "FROM usuario ORDER BY nombre ASC";
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar los usuarios.", e);
        }
    }

    /**
     * Inserta un nuevo usuario en la base de datos y devuelve su llave primaria generada.
     *
     * @param c       Conexión activa.
     * @param usuario Datos del usuario a crear (con contraseña ya cifrada en BCrypt).
     * @return ID autogenerado en {@code id_usuario}.
     */
    public int insertar(Connection c, Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, correo, contrasena, rol, activo) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre().trim());
            ps.setString(2, usuario.getCorreo().trim());
            ps.setString(3, usuario.getContrasena());
            ps.setString(4, usuario.getRol().name());
            ps.setBoolean(5, usuario.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    usuario.setIdUsuario(id);
                    return id;
                }
                throw new ErrorDatosException("No se obtuvo el identificador del usuario creado.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo registrar el usuario.", e);
        }
    }

    /**
     * Actualiza nombre, correo, rol y estado activo de un usuario existente.
     *
     * @param c       Conexión activa.
     * @param usuario Usuario con los campos actualizados.
     */
    public void actualizar(Connection c, Usuario usuario) {
        String sql = "UPDATE usuario SET nombre = ?, correo = ?, rol = ?, activo = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre().trim());
            ps.setString(2, usuario.getCorreo().trim());
            ps.setString(3, usuario.getRol().name());
            ps.setBoolean(4, usuario.isActivo());
            ps.setInt(5, usuario.getIdUsuario());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo actualizar el usuario.", e);
        }
    }

    /**
     * Actualiza el hash de contraseña de un usuario.
     *
     * @param c         Conexión activa.
     * @param idUsuario Identificador del usuario.
     * @param nuevoHash Nuevo hash BCrypt ($2a$).
     */
    public void actualizarContrasena(Connection c, int idUsuario, String nuevoHash) {
        String sql = "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo actualizar la contraseña.", e);
        }
    }

    /**
     * Convierte una fila del {@link ResultSet} en un objeto {@link Usuario}.
     */
    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setContrasena(rs.getString("contrasena"));
        u.setRol(RolUsuario.valueOf(rs.getString("rol")));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }
}