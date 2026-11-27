package datos;

import excepciones.ErrorDatosException;
import modelo.Actividad;

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
 * DAO encargado de gestionar las actividades de cada etapa en la tabla {@code actividad}.
 */
public class ActividadDao {

    /**
     * Inserta una nueva actividad y devuelve su ID autogenerado.
     *
     * @param c         Conexión activa.
     * @param actividad Actividad a registrar.
     * @return ID generado en {@code id_actividad}.
     */
    public int insertar(Connection c, Actividad actividad) {
        String sql = "INSERT INTO actividad (id_etapa, nombre, responsable, fecha_inicio, fecha_cierre, terminada) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, actividad.getIdEtapa());
            ps.setString(2, actividad.getNombre().trim());
            ps.setString(3, actividad.getResponsable().trim());
            ps.setDate(4, Date.valueOf(actividad.getFechaInicio()));
            ps.setDate(5, Date.valueOf(actividad.getFechaCierre()));
            ps.setBoolean(6, actividad.isTerminada());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    actividad.setIdActividad(id);
                    return id;
                }
                throw new ErrorDatosException("No se obtuvo el identificador de la actividad creada.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo guardar la actividad.", e);
        }
    }

    /**
     * Busca una actividad por su identificador único.
     *
     * @param c           Conexión activa.
     * @param idActividad Identificador de la actividad.
     * @return Actividad encontrada o {@code null}.
     */
    public Actividad buscarPorId(Connection c, int idActividad) {
        String sql = "SELECT id_actividad, id_etapa, nombre, responsable, fecha_inicio, fecha_cierre, terminada " +
                     "FROM actividad WHERE id_actividad = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar la actividad.", e);
        }
    }

    /**
     * Lista todas las actividades pertenecientes a una etapa, ordenadas cronológicamente.
     *
     * @param c       Conexión activa.
     * @param idEtapa Identificador de la etapa.
     * @return Lista de actividades de la etapa.
     */
    public List<Actividad> listarPorEtapa(Connection c, int idEtapa) {
        String sql = "SELECT id_actividad, id_etapa, nombre, responsable, fecha_inicio, fecha_cierre, terminada " +
                     "FROM actividad WHERE id_etapa = ? ORDER BY fecha_inicio ASC, id_actividad ASC";
        List<Actividad> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEtapa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar las actividades de la etapa.", e);
        }
    }

    /**
     * Reprograma las fechas de inicio y cierre de una actividad.
     *
     * @param c           Conexión activa.
     * @param idActividad Identificador de la actividad.
     * @param inicio      Nueva fecha de inicio.
     * @param cierre      Nueva fecha de cierre.
     */
    public void actualizarFechas(Connection c, int idActividad, LocalDate inicio, LocalDate cierre) {
        String sql = "UPDATE actividad SET fecha_inicio = ?, fecha_cierre = ? WHERE id_actividad = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(cierre));
            ps.setInt(3, idActividad);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudieron actualizar las fechas de la actividad.", e);
        }
    }

    /**
     * Marca una actividad como terminada ({@code terminada = 1}).
     *
     * @param c           Conexión activa.
     * @param idActividad Identificador de la actividad.
     */
    public void marcarTerminada(Connection c, int idActividad) {
        String sql = "UPDATE actividad SET terminada = 1 WHERE id_actividad = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo marcar la actividad como terminada.", e);
        }
    }

    /**
     * Devuelve el conteo total de actividades y cuántas siguen pendientes en una etapa.
     * Índice 0 = total de actividades; Índice 1 = actividades pendientes ({@code terminada = 0}).
     *
     * @param c       Conexión activa.
     * @param idEtapa Identificador de la etapa.
     * @return Arreglo {@code int[2]} con {@code [total, pendientes]}.
     */
    public int[] contarTotalesYPendientesPorEtapa(Connection c, int idEtapa) {
        String sql = "SELECT COUNT(*) AS total, " +
                     "COALESCE(SUM(CASE WHEN terminada = 0 THEN 1 ELSE 0 END), 0) AS pendientes " +
                     "FROM actividad WHERE id_etapa = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEtapa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("total"), rs.getInt("pendientes")};
                }
                return new int[]{0, 0};
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo verificar el estado de las actividades de la etapa.", e);
        }
    }

    /**
     * Mapea una fila SQL a un objeto {@link Actividad}.
     */
    private Actividad mapear(ResultSet rs) throws SQLException {
        Actividad a = new Actividad();
        a.setIdActividad(rs.getInt("id_actividad"));
        a.setIdEtapa(rs.getInt("id_etapa"));
        a.setNombre(rs.getString("nombre"));
        a.setResponsable(rs.getString("responsable"));
        a.setFechaInicio(rs.getObject("fecha_inicio", LocalDate.class));
        a.setFechaCierre(rs.getObject("fecha_cierre", LocalDate.class));
        a.setTerminada(rs.getBoolean("terminada"));
        return a;
    }
}