package datos;

import excepciones.ErrorDatosException;
import modelo.EstadoEtapa;
import modelo.Etapa;

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
 * DAO encargado de la persistencia de etapas en la tabla {@code etapa}.
 */
public class EtapaDao {

    /**
     * Inserta una nueva etapa dentro de un criterio de proyecto y retorna su ID generado.
     *
     * @param c     Conexión activa.
     * @param etapa Etapa a registrar.
     * @return ID autogenerado en {@code id_etapa}.
     */
    public int insertar(Connection c, Etapa etapa) {
        String sql = "INSERT INTO etapa (id_proyecto_criterio, nombre, orden, fecha_inicio, fecha_cierre, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, etapa.getIdProyectoCriterio());
            ps.setString(2, etapa.getNombre().trim());
            ps.setInt(3, etapa.getOrden());
            ps.setDate(4, Date.valueOf(etapa.getFechaInicio()));
            ps.setDate(5, Date.valueOf(etapa.getFechaCierre()));
            ps.setString(6, etapa.getEstado().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    etapa.setIdEtapa(id);
                    return id;
                }
                throw new ErrorDatosException("No se obtuvo el identificador de la etapa creada.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo guardar la etapa.", e);
        }
    }

    /**
     * Busca una etapa por su identificador único.
     *
     * @param c       Conexión activa.
     * @param idEtapa Identificador de la etapa.
     * @return Etapa encontrada o {@code null}.
     */
    public Etapa buscarPorId(Connection c, int idEtapa) {
        String sql = "SELECT id_etapa, id_proyecto_criterio, nombre, orden, fecha_inicio, fecha_cierre, estado " +
                     "FROM etapa WHERE id_etapa = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEtapa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar la etapa.", e);
        }
    }

    /**
     * Obtiene el identificador del proyecto al que pertenece una etapa.
     *
     * @param c       Conexión activa.
     * @param idEtapa Identificador de la etapa.
     * @return {@code id_proyecto} dueño de la etapa, o {@code -1} si no existe.
     */
    public int obtenerIdProyectoPorEtapa(Connection c, int idEtapa) {
        String sql = "SELECT pc.id_proyecto " +
                     "FROM etapa e " +
                     "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                     "WHERE e.id_etapa = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEtapa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_proyecto");
                }
                return -1;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo determinar el proyecto de la etapa.", e);
        }
    }

    /**
     * Lista todas las etapas asociadas a un criterio específico de un proyecto, ordenadas por secuencia.
     *
     * @param c                  Conexión activa.
     * @param idProyectoCriterio Identificador de {@code proyecto_criterio}.
     * @return Lista de etapas ordenadas por {@code orden ASC}.
     */
    public List<Etapa> listarPorProyectoCriterio(Connection c, int idProyectoCriterio) {
        String sql = "SELECT id_etapa, id_proyecto_criterio, nombre, orden, fecha_inicio, fecha_cierre, estado " +
                     "FROM etapa WHERE id_proyecto_criterio = ? ORDER BY orden ASC, id_etapa ASC";
        List<Etapa> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyectoCriterio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar las etapas del criterio.", e);
        }
    }

    /**
     * Busca la etapa inmediatamente anterior dentro del mismo criterio (según el campo {@code orden}).
     *
     * @param c                  Conexión activa.
     * @param idProyectoCriterio Identificador de {@code proyecto_criterio}.
     * @param ordenActual        Número de orden de la etapa actual.
     * @return La etapa anterior, o {@code null} si la etapa actual es la primera.
     */
    public Etapa buscarEtapaAnterior(Connection c, int idProyectoCriterio, int ordenActual) {
        String sql = "SELECT id_etapa, id_proyecto_criterio, nombre, orden, fecha_inicio, fecha_cierre, estado " +
                     "FROM etapa WHERE id_proyecto_criterio = ? AND orden < ? " +
                     "ORDER BY orden DESC, id_etapa DESC LIMIT 1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyectoCriterio);
            ps.setInt(2, ordenActual);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar la etapa anterior.", e);
        }
    }

    /**
     * Calcula el siguiente número de orden disponible para crear una etapa en un criterio.
     *
     * @param c                  Conexión activa.
     * @param idProyectoCriterio Identificador de {@code proyecto_criterio}.
     * @return Siguiente número correlativo (1 si aún no hay etapas).
     */
    public int obtenerSiguienteOrden(Connection c, int idProyectoCriterio) {
        String sql = "SELECT COALESCE(MAX(orden), 0) + 1 AS siguiente FROM etapa WHERE id_proyecto_criterio = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyectoCriterio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("siguiente");
                }
                return 1;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo calcular el orden de la etapa.", e);
        }
    }

    /**
     * Actualiza las fechas de inicio y cierre de una etapa (reprogramación desde calendario).
     *
     * @param c      Conexión activa.
     * @param idEtapa Identificador de la etapa.
     * @param inicio  Nueva fecha de inicio.
     * @param cierre  Nueva fecha de cierre.
     */
    public void actualizarFechas(Connection c, int idEtapa, LocalDate inicio, LocalDate cierre) {
        String sql = "UPDATE etapa SET fecha_inicio = ?, fecha_cierre = ? WHERE id_etapa = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(cierre));
            ps.setInt(3, idEtapa);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudieron actualizar las fechas de la etapa.", e);
        }
    }

    /**
     * Actualiza el estado operativo de una etapa.
     *
     * @param c           Conexión activa.
     * @param idEtapa     Identificador de la etapa.
     * @param nuevoEstado Nuevo estado calculado.
     */
    public void actualizarEstado(Connection c, int idEtapa, EstadoEtapa nuevoEstado) {
        String sql = "UPDATE etapa SET estado = ? WHERE id_etapa = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idEtapa);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo actualizar el estado de la etapa.", e);
        }
    }

    /**
     * Mapea una fila SQL a un objeto {@link Etapa}.
     */
    private Etapa mapear(ResultSet rs) throws SQLException {
        Etapa e = new Etapa();
        e.setIdEtapa(rs.getInt("id_etapa"));
        e.setIdProyectoCriterio(rs.getInt("id_proyecto_criterio"));
        e.setNombre(rs.getString("nombre"));
        e.setOrden(rs.getInt("orden"));
        e.setFechaInicio(rs.getObject("fecha_inicio", LocalDate.class));
        e.setFechaCierre(rs.getObject("fecha_cierre", LocalDate.class));
        e.setEstado(EstadoEtapa.valueOf(rs.getString("estado")));
        return e;
    }
}