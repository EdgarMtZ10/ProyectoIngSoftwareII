package datos;

import excepciones.ErrorDatosException;
import modelo.IdeaNegocio;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar las ideas de negocio en la tabla {@code idea_negocio}.
 */
public class IdeaNegocioDao {

    /**
     * Registra una nueva idea de negocio y asigna el ID generado al objeto.
     *
     * @param c    Conexión activa.
     * @param idea Idea de negocio a insertar.
     * @return ID autogenerado de la idea.
     */
    public int insertar(Connection c, IdeaNegocio idea) {
        String sql = "INSERT INTO idea_negocio (id_usuario, titulo, descripcion, fecha_registro) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idea.getIdUsuario());
            ps.setString(2, idea.getTitulo().trim());
            ps.setString(3, idea.getDescripcion().trim());
            ps.setDate(4, Date.valueOf(idea.getFechaRegistro()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    idea.setIdIdea(id);
                    return id;
                }
                throw new ErrorDatosException("No se obtuvo el identificador de la idea registrada.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo guardar la idea de negocio.", e);
        }
    }

    /**
     * Busca una idea de negocio por su identificador único.
     *
     * @param c      Conexión activa.
     * @param idIdea Identificador de la idea.
     * @return La idea encontrada o {@code null} si no existe.
     */
    public IdeaNegocio buscarPorId(Connection c, int idIdea) {
        String sql = "SELECT id_idea, id_usuario, titulo, descripcion, fecha_registro " +
                     "FROM idea_negocio WHERE id_idea = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idIdea);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar la idea de negocio.", e);
        }
    }

    /**
     * Lista todas las ideas de negocio registradas ordenadas de la más reciente a la más antigua.
     *
     * @param c Conexión activa.
     * @return Lista de ideas de negocio.
     */
    public List<IdeaNegocio> listarTodas(Connection c) {
        String sql = "SELECT id_idea, id_usuario, titulo, descripcion, fecha_registro " +
                     "FROM idea_negocio ORDER BY id_idea DESC";
        List<IdeaNegocio> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar las ideas de negocio.", e);
        }
    }

    /**
     * Mapea una fila SQL a un objeto {@link IdeaNegocio}.
     */
    private IdeaNegocio mapear(ResultSet rs) throws SQLException {
        IdeaNegocio idea = new IdeaNegocio();
        idea.setIdIdea(rs.getInt("id_idea"));
        idea.setIdUsuario(rs.getInt("id_usuario"));
        idea.setTitulo(rs.getString("titulo"));
        idea.setDescripcion(rs.getString("descripcion"));
        idea.setFechaRegistro(rs.getObject("fecha_registro", LocalDate.class));
        return idea;
    }
}