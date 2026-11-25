package datos;

import excepciones.ErrorDatosException;
import modelo.Atraso;
import modelo.Avance.AvanceEtapa;
import modelo.EstadoEtapa;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de consultas agregadas para el cálculo de avance por etapa/proyecto y detección de atrasos.
 */
public class AvanceDao {

    /**
     * Obtiene el conteo de actividades totales y terminadas de todas las etapas de un proyecto
     * en una sola consulta agregada ({@code LEFT JOIN}).
     *
     * @param c          Conexión activa.
     * @param idProyecto Identificador del proyecto.
     * @return Lista de {@link AvanceEtapa} con los conteos base listos para calcular porcentajes.
     */
    public List<AvanceEtapa> listarConteosPorEtapa(Connection c, int idProyecto) {
        String sql =
                "SELECT e.id_etapa, e.nombre AS nombre_etapa, cr.nombre AS criterio, e.estado, " +
                "COUNT(a.id_actividad) AS total_actividades, " +
                "COALESCE(SUM(CASE WHEN a.terminada = 1 THEN 1 ELSE 0 END), 0) AS actividades_terminadas " +
                "FROM etapa e " +
                "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                "INNER JOIN criterio cr ON cr.id_criterio = pc.id_criterio " +
                "LEFT JOIN actividad a ON a.id_etapa = e.id_etapa " +
                "WHERE pc.id_proyecto = ? " +
                "GROUP BY e.id_etapa, e.nombre, cr.nombre, e.estado, pc.id_criterio, e.orden " +
                "ORDER BY pc.id_criterio ASC, e.orden ASC";

        List<AvanceEtapa> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AvanceEtapa ae = new AvanceEtapa();
                    ae.setIdEtapa(rs.getInt("id_etapa"));
                    ae.setNombreEtapa(rs.getString("nombre_etapa"));
                    ae.setCriterio(rs.getString("criterio"));
                    ae.setEstado(EstadoEtapa.valueOf(rs.getString("estado")));
                    ae.setTotalActividades(rs.getInt("total_actividades"));
                    ae.setActividadesTerminadas(rs.getInt("actividades_terminadas"));
                    lista.add(ae);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo calcular el avance por etapas.", e);
        }
    }

    /**
     * Detecta todas las etapas no completadas y actividades no terminadas cuya fecha de cierre
     * es estrictamente anterior a {@code fechaCorte}.
     *
     * @param c          Conexión activa.
     * @param idProyecto Identificador del proyecto.
     * @param fechaCorte Fecha de referencia (normalmente {@link LocalDate#now()}).
     * @return Lista de elementos atrasados ordenados de mayor a menor atraso.
     */
    public List<Atraso> listarAtrasos(Connection c, int idProyecto, LocalDate fechaCorte) {
        String sql =
                "SELECT 'ETAPA' AS tipo, e.id_etapa AS id, e.nombre, e.fecha_cierre " +
                "FROM etapa e " +
                "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                "WHERE pc.id_proyecto = ? AND e.estado <> 'COMPLETADA' AND e.fecha_cierre < ? " +
                "UNION ALL " +
                "SELECT 'ACTIVIDAD' AS tipo, a.id_actividad AS id, a.nombre, a.fecha_cierre " +
                "FROM actividad a " +
                "INNER JOIN etapa e ON e.id_etapa = a.id_etapa " +
                "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                "WHERE pc.id_proyecto = ? AND a.terminada = 0 AND a.fecha_cierre < ? " +
                "ORDER BY fecha_cierre ASC, tipo DESC";

        List<Atraso> atrasos = new ArrayList<>();
        Date sqlFecha = Date.valueOf(fechaCorte);
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            ps.setDate(2, sqlFecha);
            ps.setInt(3, idProyecto);
            ps.setDate(4, sqlFecha);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Atraso at = new Atraso();
                    at.setTipo(rs.getString("tipo"));
                    at.setId(rs.getInt("id"));
                    at.setNombre(rs.getString("nombre"));
                    LocalDate cierre = rs.getObject("fecha_cierre", LocalDate.class);
                    at.setFechaCierre(cierre);
                    at.setDiasAtraso(ChronoUnit.DAYS.between(cierre, fechaCorte));
                    atrasos.add(at);
                }
            }
            return atrasos;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar la lista de atrasos.", e);
        }
    }
}