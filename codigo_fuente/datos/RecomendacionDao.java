package datos;

import excepciones.ErrorDatosException;
import modelo.Recomendacion;
import modelo.ResultadoRecomendacion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * DAO encargado de almacenar y consultar el dictamen final en {@code recomendacion}.
 */
public class RecomendacionDao {

    /**
     * Busca la recomendación generada para un proyecto.
     *
     * @param c          Conexión activa.
     * @param idProyecto Identificador del proyecto.
     * @return La recomendación si ya fue calculada, o {@code null} si aún no existe.
     */
    public Recomendacion buscarPorProyecto(Connection c, int idProyecto) {
        String sql = "SELECT id_recomendacion, id_proyecto, promedio, resultado, fecha_generacion " +
                     "FROM recomendacion WHERE id_proyecto = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Recomendacion r = new Recomendacion();
                    r.setIdRecomendacion(rs.getInt("id_recomendacion"));
                    r.setIdProyecto(rs.getInt("id_proyecto"));
                    r.setPromedio(rs.getBigDecimal("promedio"));
                    r.setResultado(ResultadoRecomendacion.valueOf(rs.getString("resultado")));
                    Timestamp ts = rs.getTimestamp("fecha_generacion");
                    r.setFechaGeneracion(ts != null ? ts.toLocalDateTime() : null);
                    return r;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar la recomendación del proyecto.", e);
        }
    }

    /**
     * Inserta o actualiza la recomendación calculada de un proyecto (garantizando un único registro por proyecto).
     *
     * @param c   Conexión activa.
     * @param rec Objeto {@link Recomendacion} con promedio, resultado y fecha de generación.
     * @return ID de la recomendación guardada.
     */
    public int guardarOActualizar(Connection c, Recomendacion rec) {
        Recomendacion existente = buscarPorProyecto(c, rec.getIdProyecto());
        if (existente == null) {
            String sql = "INSERT INTO recomendacion (id_proyecto, promedio, resultado, fecha_generacion) " +
                         "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, rec.getIdProyecto());
                ps.setBigDecimal(2, rec.getPromedio());
                ps.setString(3, rec.getResultado().name());
                ps.setTimestamp(4, Timestamp.valueOf(rec.getFechaGeneracion()));
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        rec.setIdRecomendacion(id);
                        return id;
                    }
                    throw new ErrorDatosException("No se obtuvo el identificador de la recomendación.", null);
                }
            } catch (SQLException e) {
                throw new ErrorDatosException("No se pudo guardar la recomendación.", e);
            }
        } else {
            String sql = "UPDATE recomendacion SET promedio = ?, resultado = ?, fecha_generacion = ? " +
                         "WHERE id_recomendacion = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setBigDecimal(1, rec.getPromedio());
                ps.setString(2, rec.getResultado().name());
                ps.setTimestamp(3, Timestamp.valueOf(rec.getFechaGeneracion()));
                ps.setInt(4, existente.getIdRecomendacion());
                ps.executeUpdate();
                rec.setIdRecomendacion(existente.getIdRecomendacion());
                return existente.getIdRecomendacion();
            } catch (SQLException e) {
                throw new ErrorDatosException("No se pudo actualizar la recomendación.", e);
            }
        }
    }
}