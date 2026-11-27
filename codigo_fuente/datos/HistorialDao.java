package datos;

import excepciones.ErrorDatosException;
import modelo.AccionHistorial;
import modelo.Historial;
import modelo.HistorialDetalle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de inserción y consulta para la bitácora inmutable {@code historial}.
 * No implementa métodos de actualización ni borrado por requerimiento RNF7.
 */
public class HistorialDao {

    /**
     * Inserta un nuevo asiento de auditoría dentro de la transacción activa.
     *
     * @param c         Conexión activa.
     * @param historial Evento de auditoría a registrar.
     * @return ID autogenerado en {@code id_historial}.
     */
    public int insertar(Connection c, Historial historial) {
        String sql = "INSERT INTO historial (id_usuario, tabla_afectada, id_registro, accion, fecha_hora) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, historial.getIdUsuario());
            ps.setString(2, historial.getTablaAfectada());
            ps.setInt(3, historial.getIdRegistro());
            ps.setString(4, historial.getAccion().name());
            ps.setTimestamp(5, Timestamp.valueOf(historial.getFechaHora()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    historial.setIdHistorial(id);
                    return id;
                }
                throw new ErrorDatosException("No se obtuvo el identificador del historial.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo registrar el evento en el historial.", e);
        }
    }

    /**
     * Lista los registros del historial enriquecidos con el nombre y correo del usuario,
     * ordenados del más reciente al más antiguo.
     *
     * @param c             Conexión activa.
     * @param tablaAfectada Filtro opcional por nombre de tabla (puede ser {@code null}).
     * @return Lista de eventos de auditoría.
     */
    public List<HistorialDetalle> listar(Connection c, String tablaAfectada) {
        StringBuilder sql = new StringBuilder(
                "SELECT h.id_historial, h.id_usuario, u.nombre AS nombre_usuario, u.correo AS correo_usuario, " +
                "h.tabla_afectada, h.id_registro, h.accion, h.fecha_hora " +
                "FROM historial h " +
                "INNER JOIN usuario u ON u.id_usuario = h.id_usuario " +
                "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (tablaAfectada != null && !tablaAfectada.trim().isEmpty()) {
            sql.append(" AND h.tabla_afectada = ?");
            params.add(tablaAfectada.trim());
        }
        sql.append(" ORDER BY h.fecha_hora DESC, h.id_historial DESC");

        List<HistorialDetalle> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HistorialDetalle det = new HistorialDetalle();
                    det.setIdHistorial(rs.getInt("id_historial"));
                    det.setIdUsuario(rs.getInt("id_usuario"));
                    det.setNombreUsuario(rs.getString("nombre_usuario"));
                    det.setCorreoUsuario(rs.getString("correo_usuario"));
                    det.setTablaAfectada(rs.getString("tabla_afectada"));
                    det.setIdRegistro(rs.getInt("id_registro"));
                    det.setAccion(AccionHistorial.valueOf(rs.getString("accion")));
                    Timestamp ts = rs.getTimestamp("fecha_hora");
                    det.setFechaHora(ts != null ? ts.toLocalDateTime() : null);
                    lista.add(det);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar el historial de cambios.", e);
        }
    }
}