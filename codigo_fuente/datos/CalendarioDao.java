package datos;

import excepciones.ErrorDatosException;
import modelo.ItemCalendario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Recupera todas las etapas y actividades de un proyecto
 * en una única consulta SQL ({@code UNION ALL}) para que el calendario abra en menos de 5 segundos.
 */
public class CalendarioDao {

    /**
     * Obtiene la lista unificada de etapas y actividades de un proyecto ordenadas por fecha.
     *
     * @param c          Conexión activa.
     * @param idProyecto Identificador del proyecto activo.
     * @return Lista de {@link ItemCalendario} lista para renderizar en la vista calendario.
     */
    public List<ItemCalendario> listarItemsPorProyecto(Connection c, int idProyecto) {
        String sql =
                "SELECT 'ETAPA' AS tipo, e.id_etapa AS id, e.nombre, e.fecha_inicio, e.fecha_cierre, " +
                "e.estado, cr.nombre AS criterio " +
                "FROM etapa e " +
                "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                "INNER JOIN criterio cr ON cr.id_criterio = pc.id_criterio " +
                "WHERE pc.id_proyecto = ? " +
                "UNION ALL " +
                "SELECT 'ACTIVIDAD' AS tipo, a.id_actividad AS id, a.nombre, a.fecha_inicio, a.fecha_cierre, " +
                "CASE WHEN a.terminada = 1 THEN 'TERMINADA' ELSE 'PENDIENTE' END AS estado, " +
                "cr.nombre AS criterio " +
                "FROM actividad a " +
                "INNER JOIN etapa e ON e.id_etapa = a.id_etapa " +
                "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                "INNER JOIN criterio cr ON cr.id_criterio = pc.id_criterio " +
                "WHERE pc.id_proyecto = ? " +
                "ORDER BY fecha_inicio ASC, tipo DESC, id ASC";

        List<ItemCalendario> items = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemCalendario item = new ItemCalendario();
                    item.setTipo(rs.getString("tipo"));
                    item.setId(rs.getInt("id"));
                    item.setNombre(rs.getString("nombre"));
                    item.setInicio(rs.getObject("fecha_inicio", LocalDate.class));
                    item.setFin(rs.getObject("fecha_cierre", LocalDate.class));
                    item.setEstado(rs.getString("estado"));
                    item.setCriterio(rs.getString("criterio"));
                    items.add(item);
                }
            }
            return items;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo cargar los elementos del calendario.", e);
        }
    }
}