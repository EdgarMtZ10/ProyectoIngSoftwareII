package datos;

import excepciones.ErrorDatosException;
import modelo.ProyectoCriterio;
import modelo.ResumenProyecto.PuntajeCriterioDetalle;
import modelo.TipoCriterio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla {@code proyecto_criterio}, que vincula cada proyecto con sus 6 criterios
 * y almacena las calificaciones individuales (0.00 a 100.00).
 */
public class ProyectoCriterioDao {

    /**
     * Crea la asociación entre un proyecto y un criterio con puntaje inicial {@code NULL}.
     *
     * @param c          Conexión activa.
     * @param idProyecto Identificador del proyecto.
     * @param idCriterio Identificador del criterio.
     * @return ID autogenerado en {@code id_proyecto_criterio}.
     */
    public int insertar(Connection c, int idProyecto, int idCriterio) {
        String sql = "INSERT INTO proyecto_criterio (id_proyecto, id_criterio, puntaje) VALUES (?, ?, NULL)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idProyecto);
            ps.setInt(2, idCriterio);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
                throw new ErrorDatosException("No se obtuvo el identificador de proyecto_criterio.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo asociar el criterio al proyecto.", e);
        }
    }

    /**
     * Busca un registro {@link ProyectoCriterio} por su llave primaria.
     *
     * @param c                  Conexión activa.
     * @param idProyectoCriterio Identificador de la relación proyecto-criterio.
     * @return Objeto encontrado o {@code null}.
     */
    public ProyectoCriterio buscarPorId(Connection c, int idProyectoCriterio) {
        String sql = "SELECT id_proyecto_criterio, id_proyecto, id_criterio, puntaje " +
                     "FROM proyecto_criterio WHERE id_proyecto_criterio = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyectoCriterio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ProyectoCriterio pc = new ProyectoCriterio();
                    pc.setIdProyectoCriterio(rs.getInt("id_proyecto_criterio"));
                    pc.setIdProyecto(rs.getInt("id_proyecto"));
                    pc.setIdCriterio(rs.getInt("id_criterio"));
                    pc.setPuntaje(rs.getBigDecimal("puntaje"));
                    return pc;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar el criterio del proyecto.", e);
        }
    }

    /**
     * Lista los 6 criterios de un proyecto junto con su nombre, tipo y puntaje actual.
     *
     * @param c          Conexión activa.
     * @param idProyecto Identificador del proyecto.
     * @return Lista detallada de los 6 criterios del proyecto.
     */
    public List<PuntajeCriterioDetalle> listarDetallePorProyecto(Connection c, int idProyecto) {
        String sql = "SELECT pc.id_proyecto_criterio, pc.id_criterio, c.tipo, c.nombre, pc.puntaje " +
                     "FROM proyecto_criterio pc " +
                     "INNER JOIN criterio c ON c.id_criterio = pc.id_criterio " +
                     "WHERE pc.id_proyecto = ? " +
                     "ORDER BY pc.id_criterio ASC";
        List<PuntajeCriterioDetalle> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PuntajeCriterioDetalle det = new PuntajeCriterioDetalle();
                    det.setIdProyectoCriterio(rs.getInt("id_proyecto_criterio"));
                    det.setIdCriterio(rs.getInt("id_criterio"));
                    det.setTipo(TipoCriterio.valueOf(rs.getString("tipo")));
                    det.setNombreCriterio(rs.getString("nombre"));
                    det.setPuntaje(rs.getBigDecimal("puntaje"));
                    lista.add(det);
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar los criterios del proyecto.", e);
        }
    }

    /**
     * Guarda o actualiza el puntaje (0.00 a 100.00) de un criterio evaluado.
     *
     * @param c                  Conexión activa.
     * @param idProyectoCriterio Identificador de {@code proyecto_criterio}.
     * @param puntaje            Calificación con 2 decimales exactos.
     */
    public void actualizarPuntaje(Connection c, int idProyectoCriterio, BigDecimal puntaje) {
        String sql = "UPDATE proyecto_criterio SET puntaje = ? WHERE id_proyecto_criterio = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBigDecimal(1, puntaje);
            ps.setInt(2, idProyectoCriterio);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo guardar el puntaje del criterio.", e);
        }
    }
}