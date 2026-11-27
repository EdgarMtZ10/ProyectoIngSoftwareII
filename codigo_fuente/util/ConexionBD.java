package util;

import excepciones.ErrorDatosException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centraliza la apertura de conexiones JDBC hacia MySQL leyendo los parámetros
 * locales desde {@code config.properties} en la raíz del proyecto.
 */
public class ConexionBD {
    private static final String RUTA_CONFIG = "config.properties";
    private static String url;
    private static String usuario;
    private static String contrasena;
    private static boolean cargado = false;

    /**
     * Carga una única vez las propiedades de conexión y registra el driver de MySQL.
     */
    private static synchronized void cargarConfiguracion() {
        if (cargado) {
            return;
        }
        Properties props = new Properties();
        // Se lee el archivo explícitamente en UTF-8 para soportar caracteres especiales en la clave
        try (InputStream is = new FileInputStream(RUTA_CONFIG);
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            props.load(reader);
            url = props.getProperty("db.url");
            usuario = props.getProperty("db.usuario");
            contrasena = props.getProperty("db.contrasena");

            if (url == null || url.isBlank() || usuario == null || usuario.isBlank()) {
                throw new ErrorDatosException(
                        "El archivo config.properties está incompleto. Verifique db.url, db.usuario y db.contrasena.",
                        null);
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
            cargado = true;
        } catch (IOException e) {
            throw new ErrorDatosException(
                    "No se encontró el archivo config.properties en la raíz del proyecto.", e);
        } catch (ClassNotFoundException e) {
            throw new ErrorDatosException(
                    "No se encontró el conector de MySQL en la carpeta lib/.", e);
        }
    }

    /**
     * Abre y devuelve una nueva conexión activa a la base de datos.
     * La clase que solicita la conexión es responsable de cerrarla al terminar.
     *
     * @return Conexión JDBC lista para operar.
     */
    public static Connection getConexion() {
        cargarConfiguracion();
        try {
            return DriverManager.getConnection(url, usuario, contrasena);
        } catch (SQLException e) {
            throw new ErrorDatosException(
                    "No se pudo conectar con la base de datos. Verifique que MySQL esté iniciado y las claves sean correctas.",
                    e);
        }
    }
}