package ui;

import modelo.Usuario;
import servicio.AutenticacionServicio;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Pantalla de acceso al sistema con diseño limpio y alto contraste (RNF1, RNF6).
 */
public class LoginFrame extends JFrame {

    private final AutenticacionServicio authServicio = new AutenticacionServicio();
    private final JTextField txtCorreo = new JTextField(24);
    private final JPasswordField txtContrasena = new JPasswordField(24);
    private final JButton btnIngresar = EstilosUI.crearBotonPrimario("Iniciar Sesión");

    public LoginFrame() {
        setTitle("Sistema Gestor de Factibilidad — Inicio de Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(430, 345);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(EstilosUI.BLANCO);

        // Encabezado sobrio con texto blanco puro de máximo contraste
        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setBackground(EstilosUI.AZUL_PRIMARIO);
        encabezado.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel lblTitulo = new JLabel("Sistema Gestor de Factibilidad");
        lblTitulo.setFont(EstilosUI.FUENTE_TITULO);
        lblTitulo.setForeground(EstilosUI.BLANCO);

        JLabel lblSub = new JLabel("Planificación Visual y Trazabilidad");
        lblSub.setFont(EstilosUI.FUENTE_PEQUENA);
        lblSub.setForeground(EstilosUI.BLANCO);

        encabezado.add(lblTitulo);
        encabezado.add(Box.createVerticalStrut(3));
        encabezado.add(lblSub);
        raiz.add(encabezado, BorderLayout.NORTH);

        // Formulario plano directo sobre el cuerpo de la ventana
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBackground(EstilosUI.BLANCO);
        formulario.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        gbc.gridy = 0;
        formulario.add(EstilosUI.crearEtiqueta("Correo electrónico", true), gbc);

        gbc.gridy = 1;
        txtCorreo.setFont(EstilosUI.FUENTE_NORMAL);
        txtCorreo.setPreferredSize(new Dimension(280, 30));
        formulario.add(txtCorreo, gbc);

        gbc.gridy = 2;
        formulario.add(EstilosUI.crearEtiqueta("Contraseña", true), gbc);

        gbc.gridy = 3;
        txtContrasena.setFont(EstilosUI.FUENTE_NORMAL);
        txtContrasena.setPreferredSize(new Dimension(280, 30));
        formulario.add(txtContrasena, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(14, 0, 6, 0);
        btnIngresar.setPreferredSize(new Dimension(280, 34));
        formulario.add(btnIngresar, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(4, 0, 0, 0);
        JLabel lblNota = new JLabel("* Campos obligatorios");
        lblNota.setFont(EstilosUI.FUENTE_PEQUENA);
        lblNota.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        formulario.add(lblNota, gbc);

        raiz.add(formulario, BorderLayout.CENTER);
        setContentPane(raiz);

        getRootPane().setDefaultButton(btnIngresar);
        btnIngresar.addActionListener(e -> autenticar());
    }

    private void autenticar() {
        String correo = txtCorreo.getText();
        String clave = new String(txtContrasena.getPassword());

        try {
            Usuario usuario = authServicio.iniciarSesion(correo, clave);
            if (usuario != null) {
                VentanaPrincipal principal = new VentanaPrincipal();
                principal.setVisible(true);
                dispose();
            }
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
            txtContrasena.setText("");
            txtContrasena.requestFocusInWindow();
        }
    }
}