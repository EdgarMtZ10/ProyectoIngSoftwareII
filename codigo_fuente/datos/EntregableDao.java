package datos;

import excepciones.ErrorDatosException;
import modelo.Entregable;
import modelo.EntregablePendiente;
import modelo.EstadoEntregable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar los entregables y sus revisiones en la tabla {@code entregable}.
 */
public class EntregableDao {

    /**
     * Crea un nuevo entregable asociado a una actividad en estado {@code PENDIENTE}.
     *
     * @param c          Conexión activa.
     * @param entregable Entregable a registrar.
     * @return ID autogenerado en {@code id_entregable}.
     */
    public int insertar(Connection c, Entregable entregable) {
        String sql = "INSERT INTO entregable (id_actividad, id_aprobador, nombre, estado, motivo_rechazo, fecha_revision) " +
                     "VALUES (?, NULL, ?, ?, NULL, NULL)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entregable.getIdActividad());
            ps.setString(2, entregable.getNombre().trim());
            ps.setString(3, EstadoEntregable.PENDIENTE.name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    entregable.setIdEntregable(id);
                    return id;
                }
                throw new ErrorDatosException("No se obtuvo el identificador del entregable creado.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo guardar el entregable.", e);
        }
    }

    /**
     * Busca un entregable por su identificador único.
     *
     * @param c            Conexión activa.
     * @param idEntregable Identificador del entregable.
     * @return Entregable encontrado o {@code null}.
     */
    public Entregable buscarPorId(Connection c, int idEntregable) {
        String sql = "SELECT id_entregable, id_actividad, id_aprobador, nombre, estado, motivo_rechazo, fecha_revision " +
                     "FROM entregable WHERE id_entregable = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEntregable);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar el entregable.", e);
        }
    }

    /**
     * Obtiene el identificador del proyecto al que pertenece un entregable.
     *
     * @param c            Conexión activa.
     * @param idEntregable Identificador del entregable.
     * @return {@code id_proyecto} dueño del entregable, o {@code -1} si no existe.
     */
    public int obtenerIdProyectoPorEntregable(Connection c, int idEntregable) {
        String sql = "SELECT pc.id_proyecto " +
                     "FROM entregable en " +
                     "INNER JOIN actividad a ON a.id_actividad = en.id_actividad " +
                     "INNER JOIN etapa e ON e.id_etapa = a.id_etapa " +
                     "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                     "WHERE en.id_entregable = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEntregable);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_proyecto");
                }
                return -1;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo determinar el proyecto del entregable.", e);
        }
    }

    /**
     * Lista todos los entregables asociados a una actividad.
     *
     * @param c           Conexión activa.
     * @param idActividad Identificador de la actividad.
     * @return Lista de entregables de la actividad.
     */
    public List<Entregable> listarPorActividad(Connection c, int idActividad) {
        String sql = "SELECT id_entregable, id_actividad, id_aprobador, nombre, estado, motivo_rechazo, fecha_revision " +
                     "FROM entregable WHERE id_actividad = ? ORDER BY id_entregable ASC";
        List<Entregable> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar los entregables de la actividad.", e);
        }
    }

    /**
     * Lista los entregables de un proyecto (o de todos los proyectos si {@code idProyecto == null})
     * filtrando opcionalmente solo los pendientes de revisión.
     *
     * @param c              Conexión activa.
     * @param idProyecto     Filtro opcional por proyecto.
     * @param soloPendientes {@code true} para traer únicamente los de estado {@code PENDIENTE}.
     * @return Lista enriquecida con actividad, etapa, criterio y proyecto.
     */
    public List<EntregablePendiente> listarConContexto(Connection c, Integer idProyecto, boolean soloPendientes) {
        StringBuilder sql = new StringBuilder(
                "SELECT en.id_entregable, en.id_actividad, p.id_proyecto, " +
                "en.nombre AS nombre_entregable, a.nombre AS nombre_actividad, " +
                "e.nombre AS nombre_etapa, cr.nombre AS nombre_criterio, p.nombre AS nombre_proyecto, " +
                "en.estado, en.motivo_rechazo " +
                "FROM entregable en " +
                "INNER JOIN actividad a ON a.id_actividad = en.id_actividad " +
                "INNER JOIN etapa e ON e.id_etapa = a.id_etapa " +
                "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                "INNER JOIN criterio cr ON cr.id_criterio = pc.id_criterio " +
                "INNER JOIN proyecto p ON p.id_proyecto = pc.id_proyecto " +
                "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (idProyecto != null) {
            sql.append(" AND p.id_proyecto = ?");
            params.add(idProyecto);
        }
        if (soloPendientes) {
            sql.append(" AND en.estado = ?");
            params.add(EstadoEntregable.PENDIENTE.name());
        }
        sql.append(" ORDER BY p.id_proyecto DESC, e.orden ASC, en.id_entregable ASC");

        List<EntregablePendiente> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EntregablePendiente item = new EntregablePendiente();
                    item.setIdEntregable(rs.getInt("id_entregable"));
                    item.setIdActividad(rs.getInt("id_actividad"));
                    item.setIdProyecto(rs.getInt("id_proyecto"));
                    item.setNombreEntregable(rs.getString("nombre_entregable"));
                    item.setNombreActividad(rs.getString("nombre_actividad"));
                    item.setNombreEtapa(rs.getString("nombre_etapa"));
                    item.setNombreCriterio(rs.getString("nombre_criterio"));
                    item.setNombreProyecto(rs.getString("nombre_proyecto"));
                    item.setEstado(EstadoEntregable.valueOf(rs.getString("estado")));
                    item.setMotivoRechazo(rs.getString("motivo_rechazo"));
                    lista.add(item);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar los entregables para revisión.", e);
        }
    }

    /**
     * Registra la decisión de revisión (APROBADO o RECHAZADO) emitida por un Aprobador.
     *
     * @param c             Conexión activa.
     * @param idEntregable  Identificador del entregable.
     * @param idAprobador   Identificador del usuario aprobador.
     * @param nuevoEstado   {@code APROBADO} o {@code RECHAZADO}.
     * @param motivoRechazo Justificación obligatoria si se rechaza, o {@code null} si se aprueba.
     * @param fechaRevision Marca de fecha y hora exacta de la revisión.
     */
    public void registrarRevision(Connection c, int idEntregable, int idAprobador,
                                  EstadoEntregable nuevoEstado, String motivoRechazo,
                                  LocalDateTime fechaRevision) {
        String sql = "UPDATE entregable SET id_aprobador = ?, estado = ?, motivo_rechazo = ?, fecha_revision = ? " +
                     "WHERE id_entregable = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idAprobador);
            ps.setString(2, nuevoEstado.name());
            if (motivoRechazo == null || motivoRechazo.trim().isEmpty()) {
                ps.setNull(3, Types.VARCHAR);
            } else {
                ps.setString(3, motivoRechazo.trim());
            }
            ps.setTimestamp(4, Timestamp.valueOf(fechaRevision));
            ps.setInt(5, idEntregable);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo guardar la revisión del entregable.", e);
        }
    }

    /**
     * Cuenta cuántos entregables de una etapa aún no tienen estado {@code APROBADO}
     * (incluye tanto {@code PENDIENTE} como {@code RECHAZADO}).
     *
     * @param c       Conexión activa.
     * @param idEtapa Identificador de la etapa.
     * @return Cantidad de entregables sin aprobar en la etapa.
     */
    public int contarNoAprobadosPorEtapa(Connection c, int idEtapa) {
        String sql = "SELECT COUNT(*) AS no_aprobados " +
                     "FROM entregable en " +
                     "INNER JOIN actividad a ON a.id_actividad = en.id_actividad " +
                     "WHERE a.id_etapa = ? AND en.estado <> 'APROBADO'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEtapa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("no_aprobados");
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo verificar la aprobación de entregables.", e);
        }
    }

    /**
     * Mapea una fila SQL a un objeto {@link Entregable}.
     */
    private Entregable mapear(ResultSet rs) throws SQLException {
        Entregable en = new Entregable();
        en.setIdEntregable(rs.getInt("id_entregable"));
        en.setIdActividad(rs.getInt("id_actividad"));
        int idApr = rs.getInt("id_aprobador");
        en.setIdAprobador(rs.wasNull() ? null : idApr);
        en.setNombre(rs.getString("nombre"));
        en.setEstado(EstadoEntregable.valueOf(rs.getString("estado")));
        en.setMotivoRechazo(rs.getString("motivo_rechazo"));
        Timestamp ts = rs.getTimestamp("fecha_revision");
        en.setFechaRevision(ts != null ? ts.toLocalDateTime() : null);
        return en;
    }
}