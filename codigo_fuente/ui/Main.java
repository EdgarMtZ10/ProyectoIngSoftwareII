package ui;

import servicio.UsuarioServicio;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada principal del Sistema Gestor de Factibilidad con Planificación Visual.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Si no está disponible el Look & Feel nativo, continúa con Metal por defecto
        }

        // Asegura que exista al menos un administrador inicial si la base está recién creada
        try {
            new UsuarioServicio().inicializarAdministradorSiVacio(
                    "Administrador General",
                    "admin@cuc.ac.cr",
                    "Admin1234"
            );
        } catch (Exception ex) {
            // Si MySQL aún no está iniciado, el error se mostrará al intentar iniciar sesión
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}