package ui;

import modelo.RolUsuario;
import modelo.Usuario;
import servicio.UsuarioServicio;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Pantalla de administración de usuarios para el rol {@link RolUsuario#ADMINISTRADOR}.
 * Utiliza una única superficie plana con separadores lineales en lugar de tarjetas anidadas.
 */
public class PanelUsuarios extends JPanel implements PanelModulo {

    private final UsuarioServicio usuarioServicio = new UsuarioServicio();

    private final JTextField txtNombre = new JTextField(20);
    private final JTextField txtCorreo = new JTextField(20);
    private final JPasswordField txtContrasena = new JPasswordField(20);
    private final JComboBox<RolUsuario> cmbRol = new JComboBox<>(RolUsuario.values());
    private final JCheckBox chkActivo = new JCheckBox("Cuenta activa", true);

    private final DefaultTableModel modeloTabla = EstilosUI.crearModeloTablaNoEditable(
            "ID", "Nombre Completo", "Correo Electrónico", "Rol", "Estado"
    );
    private final JTable tablaUsuarios = new JTable(modeloTabla);

    private Integer idUsuarioSeleccionado = null;

    public PanelUsuarios() {
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO_BASE);
        EstilosUI.fijarMargen(this, 12, 12, 12, 12);

        JPanel superficie = new JPanel(new BorderLayout(0, 14));
        EstilosUI.aplicarSuperficiePrincipal(superficie);

        // Sección superior: Formulario
        JPanel bloqueForm = new JPanel(new BorderLayout(0, 10));
        bloqueForm.setBackground(EstilosUI.BLANCO);
        bloqueForm.add(EstilosUI.crearEncabezadoSeccion("Registro y Edición de Usuarios"), BorderLayout.NORTH);

        JPanel gridForm = new JPanel(new GridBagLayout());
        gridForm.setBackground(EstilosUI.BLANCO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        gridForm.add(EstilosUI.crearEtiqueta("Nombre completo", true), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNombre.setFont(EstilosUI.FUENTE_NORMAL);
        gridForm.add(txtNombre, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        gridForm.add(EstilosUI.crearEtiqueta("Correo electrónico", true), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        txtCorreo.setFont(EstilosUI.FUENTE_NORMAL);
        gridForm.add(txtCorreo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        gridForm.add(EstilosUI.crearEtiqueta("Contraseña (al crear)", true), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtContrasena.setFont(EstilosUI.FUENTE_NORMAL);
        gridForm.add(txtContrasena, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        gridForm.add(EstilosUI.crearEtiqueta("Rol del sistema", true), gbc);
        gbc.gridx = 3; gbc.weightx = 1.0;
        cmbRol.setFont(EstilosUI.FUENTE_NORMAL);
        gridForm.add(cmbRol, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        chkActivo.setBackground(EstilosUI.BLANCO);
        chkActivo.setForeground(EstilosUI.TEXTO_PRIMARIO);
        chkActivo.setFont(EstilosUI.FUENTE_NORMAL);
        gridForm.add(chkActivo, gbc);

        JPanel barraBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        barraBotones.setBackground(EstilosUI.BLANCO);

        JButton btnNuevo = EstilosUI.crearBotonSecundario("Limpiar");
        JButton btnCrear = EstilosUI.crearBotonPrimario("Crear Usuario");
        JButton btnActualizar = EstilosUI.crearBotonExito("Guardar Cambios");
        JButton btnClave = EstilosUI.crearBotonSecundario("Cambiar Contraseña");

        barraBotones.add(btnNuevo);
        barraBotones.add(btnCrear);
        barraBotones.add(btnActualizar);
        barraBotones.add(btnClave);

        gbc.gridx = 1; gbc.gridwidth = 3;
        gridForm.add(barraBotones, gbc);

        bloqueForm.add(gridForm, BorderLayout.CENTER);
        bloqueForm.add(new JSeparator(), BorderLayout.SOUTH);

        superficie.add(bloqueForm, BorderLayout.NORTH);

        // Sección inferior: Tabla de usuarios
        JPanel bloqueTabla = new JPanel(new BorderLayout(0, 8));
        bloqueTabla.setBackground(EstilosUI.BLANCO);
        bloqueTabla.add(EstilosUI.crearEncabezadoSeccion("Usuarios Registrados (Seleccione una fila para editar)"), BorderLayout.NORTH);

        EstilosUI.configurarTabla(tablaUsuarios);
        tablaUsuarios.getColumnModel().getColumn(0).setMaxWidth(60);
        bloqueTabla.add(new JScrollPane(tablaUsuarios), BorderLayout.CENTER);

        superficie.add(bloqueTabla, BorderLayout.CENTER);
        add(superficie, BorderLayout.CENTER);

        // Eventos
        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnCrear.addActionListener(e -> crearUsuario());
        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnClave.addActionListener(e -> restablecerContrasena());

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() >= 0) {
                cargarSeleccion();
            }
        });
    }

    @Override
    public void refrescar() {
        try {
            List<Usuario> usuarios = usuarioServicio.listarUsuarios();
            modeloTabla.setRowCount(0);
            for (Usuario u : usuarios) {
                modeloTabla.addRow(new Object[]{
                        u.getIdUsuario(),
                        u.getNombre(),
                        u.getCorreo(),
                        u.getRol().name(),
                        u.isActivo() ? "ACTIVO" : "DESACTIVADO"
                });
            }
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }

    private void crearUsuario() {
        try {
            usuarioServicio.crearUsuario(
                    txtNombre.getText(),
                    txtCorreo.getText(),
                    new String(txtContrasena.getPassword()),
                    (RolUsuario) cmbRol.getSelectedItem()
            );
            EstilosUI.mostrarInfo(this, "Usuario creado correctamente.");
            limpiarFormulario();
            refrescar();
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }

    private void actualizarUsuario() {
        if (idUsuarioSeleccionado == null) {
            EstilosUI.mostrarInfo(this, "Seleccione primero un usuario de la tabla para editar.");
            return;
        }
        try {
            boolean activo = chkActivo.isSelected();
            if (!activo) {
                boolean seguro = EstilosUI.confirmar(this,
                        "¿Confirma que desea desactivar la cuenta de este usuario? No podrá iniciar sesión mientras esté inactiva.");
                if (!seguro) {
                    return;
                }
            }
            usuarioServicio.actualizarUsuario(
                    idUsuarioSeleccionado,
                    txtNombre.getText(),
                    txtCorreo.getText(),
                    (RolUsuario) cmbRol.getSelectedItem(),
                    activo
            );
            EstilosUI.mostrarInfo(this, "Datos del usuario actualizados.");
            limpiarFormulario();
            refrescar();
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }

    private void restablecerContrasena() {
        if (idUsuarioSeleccionado == null) {
            EstilosUI.mostrarInfo(this, "Seleccione un usuario de la tabla para cambiar su contraseña.");
            return;
        }
        JPasswordField pf = new JPasswordField();
        int opcion = JOptionPane.showConfirmDialog(
                this,
                pf,
                "Nueva contraseña (mínimo 8 caracteres, letras y números):",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                usuarioServicio.cambiarContrasena(idUsuarioSeleccionado, new String(pf.getPassword()));
                EstilosUI.mostrarInfo(this, "Contraseña actualizada correctamente.");
            } catch (Exception ex) {
                EstilosUI.mostrarError(this, ex);
            }
        }
    }

    private void cargarSeleccion() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            return;
        }
        idUsuarioSeleccionado = (Integer) modeloTabla.getValueAt(fila, 0);
        txtNombre.setText(String.valueOf(modeloTabla.getValueAt(fila, 1)));
        txtCorreo.setText(String.valueOf(modeloTabla.getValueAt(fila, 2)));
        cmbRol.setSelectedItem(RolUsuario.valueOf(String.valueOf(modeloTabla.getValueAt(fila, 3))));
        chkActivo.setSelected("ACTIVO".equals(modeloTabla.getValueAt(fila, 4)));
        txtContrasena.setText("");
    }

    private void limpiarFormulario() {
        idUsuarioSeleccionado = null;
        txtNombre.setText("");
        txtCorreo.setText("");
        txtContrasena.setText("");
        cmbRol.setSelectedIndex(0);
        chkActivo.setSelected(true);
        tablaUsuarios.clearSelection();
    }
}