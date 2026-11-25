package util;

import java.sql.Connection;

/**
 * Clase de diagnóstico ejecutable por consola para verificar que {@code config.properties}
 * y el conector JDBC de MySQL funcionan correctamente.
 */
public class ProbarConexion {
    public static void main(String[] args) {
        // Abre y cierra automáticamente una conexión de prueba usando try-with-resources
        try (Connection c = ConexionBD.getConexion()) {
            System.out.println("Conexión correcta a la base de datos: " + c.getCatalog());
        } catch (Exception e) {
            System.err.println("Error al conectar: " + e.getMessage());
        }
    }
}