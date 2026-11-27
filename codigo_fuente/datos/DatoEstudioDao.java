package datos;

import excepciones.ErrorDatosException;
import modelo.CategoriaDato;
import modelo.DatoEstudio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO encargado de gestionar los datos del estudio de factibilidad en {@code dato_estudio}.
 */
public class DatoEstudioDao {

    /**
     * Inserta un dato de estudio asociado a una etapa y devuelve su ID autogenerado.
     *
     * @param c    Conexión activa.
     * @param dato Dato de estudio a guardar.
     * @return ID generado en {@code id_dato}.
     */
    public int insertar(Connection c, DatoEstudio dato) {
        String sql = "INSERT INTO dato_estudio (id_etapa, categoria, descripcion, monto, periodo) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, dato.getIdEtapa());
            ps.setString(2, dato.getCategoria().name());
            ps.setString(3, dato.getDescripcion().trim());
            if (dato.getMonto() == null) {
                ps.setNull(4, Types.DECIMAL);
            } else {
                ps.setBigDecimal(4, dato.getMonto());
            }
            if (dato.getPeriodo() == null || dato.getPeriodo().trim().isEmpty()) {
                ps.setNull(5, Types.VARCHAR);
            } else {
                ps.setString(5, dato.getPeriodo().trim());
            }
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    dato.setIdDato(id);
                    return id;
                }
                throw new ErrorDatosException("No se obtuvo el identificador del dato registrado.", null);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo guardar el dato del estudio.", e);
        }
    }

    /**
     * Lista todos los datos registrados dentro de una etapa específica.
     *
     * @param c       Conexión activa.
     * @param idEtapa Identificador de la etapa.
     * @return Lista de datos de esa etapa.
     */
    public List<DatoEstudio> listarPorEtapa(Connection c, int idEtapa) {
        String sql = "SELECT id_dato, id_etapa, categoria, descripcion, monto, periodo " +
                     "FROM dato_estudio WHERE id_etapa = ? ORDER BY id_dato ASC";
        List<DatoEstudio> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEtapa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar los datos de la etapa.", e);
        }
    }

    /**
     * Lista todos los datos de estudio recopilados a lo largo de todo un proyecto.
     *
     * @param c          Conexión activa.
     * @param idProyecto Identificador del proyecto.
     * @return Lista completa de datos del proyecto.
     */
    public List<DatoEstudio> listarPorProyecto(Connection c, int idProyecto) {
        String sql = "SELECT d.id_dato, d.id_etapa, d.categoria, d.descripcion, d.monto, d.periodo " +
                     "FROM dato_estudio d " +
                     "INNER JOIN etapa e ON e.id_etapa = d.id_etapa " +
                     "INNER JOIN proyecto_criterio pc ON pc.id_proyecto_criterio = e.id_proyecto_criterio " +
                     "WHERE pc.id_proyecto = ? " +
                     "ORDER BY pc.id_criterio ASC, e.orden ASC, d.id_dato ASC";
        List<DatoEstudio> lista = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idProyecto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            return lista;
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo listar los datos del proyecto.", e);
        }
    }

    /**
     * Mapea una fila SQL a un objeto {@link DatoEstudio}.
     */
    private DatoEstudio mapear(ResultSet rs) throws SQLException {
        DatoEstudio d = new DatoEstudio();
        d.setIdDato(rs.getInt("id_dato"));
        d.setIdEtapa(rs.getInt("id_etapa"));
        d.setCategoria(CategoriaDato.valueOf(rs.getString("categoria")));
        d.setDescripcion(rs.getString("descripcion"));
        d.setMonto(rs.getBigDecimal("monto"));
        d.setPeriodo(rs.getString("periodo"));
        return d;
    }
}