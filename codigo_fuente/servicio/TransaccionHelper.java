package servicio;

import datos.HistorialDao;
import excepciones.ErrorDatosException;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.Historial;
import util.ConexionBD;
import util.Sesion;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Utilidad interna del paquete {@code servicio} para ejecutar operaciones atómicas
 * con {@code COMMIT}/{@code ROLLBACK} automático (RNF2) y registrar eventos en {@code historial}.
 */
final class TransaccionHelper {

    private static final HistorialDao HISTORIAL_DAO = new HistorialDao();

    private TransaccionHelper() {
    }

    @FunctionalInterface
    interface TrabajoTransaccional<T> {
        T ejecutar(Connection c) throws Exception;
    }

    @FunctionalInterface
    interface LecturaConexion<T> {
        T ejecutar(Connection c) throws Exception;
    }

    /**
     * Ejecuta una operación de escritura dentro de una transacción JDBC explícita.
     * Si ocurre cualquier excepción, revierte todos los cambios ({@code rollback}).
     */
    static <T> T ejecutarEnTransaccion(TrabajoTransaccional<T> trabajo) {
        try (Connection c = ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                T resultado = trabajo.ejecutar(c);
                c.commit();
                return resultado;
            } catch (Exception ex) {
                try {
                    c.rollback();
                } catch (SQLException rbEx) {
                    ex.addSuppressed(rbEx);
                }
                if (ex instanceof RuntimeException) {
                    RuntimeException re = (RuntimeException) ex;
                    if (re instanceof ErrorDatosException && ((ErrorDatosException) re).esViolacionDeRestriccion()) {
                        throw new ValidacionException("Los datos incumplen una restricción de unicidad o integridad en la base de datos.", re);
                    }
                    throw re;
                }
                throw new ErrorDatosException("Error inesperado al procesar la operación.", ex);
            }
        } catch (SQLException e) {
            throw new ErrorDatosException("No se pudo establecer comunicación con MySQL.", e);
        }
    }

    /**
     * Ejecuta una consulta de solo lectura administrando el cierre seguro de la conexión.
     */
    static <T> T ejecutarLectura(LecturaConexion<T> lectura) {
        try (Connection c = ConexionBD.getConexion()) {
            return lectura.ejecutar(c);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception ex) {
            throw new ErrorDatosException("No se pudo consultar la información solicitada.", ex);
        }
    }

    /**
     * Inserta un asiento en la bitácora inmutable {@code historial} usando el usuario de la sesión activa.
     */
    static void auditar(Connection c, String tablaAfectada, int idRegistro, AccionHistorial accion) {
        int idUsuario = Sesion.getUsuario().getIdUsuario();
        auditarConUsuario(c, idUsuario, tablaAfectada, idRegistro, accion);
    }

    /**
     * Inserta un asiento en {@code historial} especificando explícitamente el {@code idUsuario}.
     */
    static void auditarConUsuario(Connection c, int idUsuario, String tablaAfectada,
                                  int idRegistro, AccionHistorial accion) {
        Historial h = new Historial();
        h.setIdUsuario(idUsuario);
        h.setTablaAfectada(tablaAfectada);
        h.setIdRegistro(idRegistro);
        h.setAccion(accion);
        h.setFechaHora(LocalDateTime.now());
        HISTORIAL_DAO.insertar(c, h);
    }
}