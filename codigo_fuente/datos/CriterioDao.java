package datos;

import excepciones.ErrorDatosException;
import modelo.Criterio;
import modelo.TipoCriterio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de solo lectura para el catálogo fijo de los 6 criterios de factibilidad ({@code criterio}).
 */
public class CriterioDao {

    /**
     * Devuelve todos los criterios del catálogo ordenados por su identificador.
     *
     * @param c Conexión activa.
     * @return Lista con los 6 criterios estándar.
     */
    public List<Criterio> listarTodos(Connection c) {
        String sql = "SELECT id_criterio, tipo, nombre FROM criterio ORDER BY id_criterio ASC";
        List<Criterio> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo cargar el catálogo de criterios.", e);
        }
    }

    /**
     * Busca un criterio por su identificador.
     *
     * @param c          Conexión activa.
     * @param idCriterio Identificador del criterio.
     * @return Criterio encontrado o {@code null}.
     */
    public Criterio buscarPorId(Connection c, int idCriterio) {
        String sql = "SELECT id_criterio, tipo, nombre FROM criterio WHERE id_criterio = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCriterio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo consultar el criterio.", e);
        }
    }

    /**
     * Mapea una fila SQL a un objeto {@link Criterio}.
     */
    private Criterio mapear(ResultSet rs) throws SQLException {
        Criterio cr = new Criterio();
        cr.setIdCriterio(rs.getInt("id_criterio"));
        cr.setTipo(TipoCriterio.valueOf(rs.getString("tipo")));
        cr.setNombre(rs.getString("nombre"));
        return cr;
    }
}