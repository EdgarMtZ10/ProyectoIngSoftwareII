package datos;

import excepciones.ErrorDatosException;
import modelo.EstadoProyecto;
import modelo.Proyecto;

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
 * DAO encargado de gestionar los proyectos de factibilidad en la tabla {@code proyecto}.
 */
public class ProyectoDao {

    /**
     * Inserta un nuevo proyecto y devuelve su ID autogenerado.
     *
     * @param c        Conexión activa.
     * @param proyecto Proyecto a insertar.
     * @return ID generado en {@code id_proyecto}.
     */
    public int insertar(Connection c, Proyecto proyecto) {
        String sql = "INSERT INTO proyecto (id_idea, id_analista, nombre, fecha_creacion, estado) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, proyecto.getIdIdea());
            ps.setInt(2, proyecto.getIdAnalista());
            ps.setString(3, proyecto.getNombre().trim());
            ps.setDate(4, Date.valueOf(proyecto.getFechaCreacion()));
            ps.setString(5, proyecto.getEstado().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    proyecto.setIdProyecto(id);
                    return id;
                }
                throw new ErrorDatosException("No se obtuvo el identificador del proyecto creado.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo crear el proyecto.", e);
        }
    }

    /**
     * Busca un proyecto por su identificador único.
     *
     * @param c          Conexión activa.
     * @param idProyecto Identificador del proyecto.
     * @return Proyecto encontrado o {@code null} si no existe.
     */
    public Proyecto buscarPorId(Connection c, int idProyecto) {
        String sql = "SELECT id_proyecto, id_idea, id_analista, nombre, fecha_creacion, estado " +
                     "FROM proyecto WHERE id_proyecto = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar el proyecto.", e);
        }
    }

    /**
     * Lista proyectos aplicando filtros opcionales por texto en el nombre y/o por estado.
     *
     * @param c      Conexión activa.
     * @param texto  Filtro opcional por coincidencia parcial en el nombre (puede ser {@code null}).
     * @param estado Filtro opcional por estado exacto (puede ser {@code null}).
     * @return Lista de proyectos que cumplen los criterios.
     */
    public List<Proyecto> buscar(Connection c, String texto, EstadoProyecto estado) {
        StringBuilder sql = new StringBuilder(
                "SELECT id_proyecto, id_idea, id_analista, nombre, fecha_creacion, estado " +
                "FROM proyecto WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        // Se agregan fragmentos fijos de WHERE y los valores viajan como parámetros seguros
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND nombre LIKE ?");
            parametros.add("%" + texto.trim() + "%");
        }
        if (estado != null) {
            sql.append(" AND estado = ?");
            parametros.add(estado.name());
        }
        sql.append(" ORDER BY id_proyecto DESC");

        List<Proyecto> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar los proyectos.", e);
        }
    }

    /**
     * Actualiza el estado operativo de un proyecto (por ejemplo: EN_EJECUCION, FINALIZADO, CANCELADO).
     *
     * @param c           Conexión activa.
     * @param idProyecto  Identificador del proyecto.
     * @param nuevoEstado Nuevo estado a asignar.
     */
    public void actualizarEstado(Connection c, int idProyecto, EstadoProyecto nuevoEstado) {
        String sql = "UPDATE proyecto SET estado = ? WHERE id_proyecto = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idProyecto);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo actualizar el estado del proyecto.", e);
        }
    }

    /**
     * Mapea una fila SQL a un objeto {@link Proyecto}.
     */
    private Proyecto mapear(ResultSet rs) throws SQLException {
        Proyecto p = new Proyecto();
        p.setIdProyecto(rs.getInt("id_proyecto"));
        p.setIdIdea(rs.getInt("id_idea"));
        p.setIdAnalista(rs.getInt("id_analista"));
        p.setNombre(rs.getString("nombre"));
        p.setFechaCreacion(rs.getObject("fecha_creacion", LocalDate.class));
        p.setEstado(EstadoProyecto.valueOf(rs.getString("estado")));
        return p;
    }
}