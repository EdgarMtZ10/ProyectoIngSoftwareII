package ui;

import excepciones.BloqueoException;
import excepciones.ErrorDatosException;
import excepciones.PermisoException;
import excepciones.ValidacionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;

/**
 * Define una estética de escritorio sobria, plana y de alto contraste:
 * sin tarjetas anidadas, sin sombras artificiales y con controles rectangulares claros (RNF6).
 */
public final class EstilosUI {

    // Paleta de alto contraste y superficies limpias
    public static final Color AZUL_PRIMARIO = new Color(27, 54, 93);
    public static final Color AZUL_ACCION = new Color(33, 82, 168);
    public static final Color SELECCION_TABLA = new Color(224, 235, 250);
    public static final Color FONDO_BASE = new Color(245, 247, 250);
    public static final Color BLANCO = Color.WHITE;
    public static final Color BORDE = new Color(204, 211, 220);

    // Textos de lectura nítida (nunca gris claro sobre color)
    public static final Color TEXTO_PRIMARIO = new Color(28, 33, 39);
    public static final Color TEXTO_SECUNDARIO = new Color(66, 74, 83);

    // Estados semánticos
    public static final Color VERDE_EXITO = new Color(26, 117, 48);
    public static final Color AMBAR_ALERTA = new Color(180, 95, 6);
    public static final Color ROJO_PELIGRO = new Color(178, 34, 34);

    // Tipografía estándar de escritorio
    public static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FUENTE_SECCION = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_NEGRITA = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FUENTE_PEQUENA = new Font("Segoe UI", Font.PLAIN, 12);

    private EstilosUI() {
    }

    /**
     * Etiqueta de campo con asterisco rojo cuando es obligatorio (RNF6).
     */
    public static JLabel crearEtiqueta(String texto, boolean obligatorio) {
        JLabel lbl = obligatorio
                ? new JLabel("<html>" + texto + " <font color='#B22222'>*</font></html>")
                : new JLabel(texto);
        lbl.setFont(FUENTE_NEGRITA);
        lbl.setForeground(TEXTO_PRIMARIO);
        return lbl;
    }

    /**
     * Encabezado de sección plano con línea divisoria inferior de 1 px (evita cajas anidadas).
     */
    public static JLabel crearEncabezadoSeccion(String titulo) {
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(FUENTE_SECCION);
        lbl.setForeground(AZUL_PRIMARIO);
        Border lineaInferior = BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                lineaInferior,
                BorderFactory.createEmptyBorder(0, 0, 6, 0)
        ));
        return lbl;
    }

    /**
     * Contenedor plano único con borde fino de 1 px para delimitar el área de trabajo principal.
     */
    public static void aplicarSuperficiePrincipal(JPanel panel) {
        panel.setBackground(BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
    }

    public static JButton crearBotonPrimario(String texto) {
        return crearBoton(texto, AZUL_ACCION, Color.WHITE, AZUL_PRIMARIO);
    }

    public static JButton crearBotonSecundario(String texto) {
        return crearBoton(texto, new Color(238, 241, 245), TEXTO_PRIMARIO, BORDE);
    }

    public static JButton crearBotonExito(String texto) {
        return crearBoton(texto, VERDE_EXITO, Color.WHITE, new Color(20, 92, 37));
    }

    public static JButton crearBotonPeligro(String texto) {
        return crearBoton(texto, ROJO_PELIGRO, Color.WHITE, new Color(140, 25, 25));
    }

    private static JButton crearBoton(String texto, Color fondo, Color textoColor, Color colorBorde) {
        JButton btn = new JButton(texto);
        btn.setFont(FUENTE_NEGRITA);
        btn.setBackground(fondo);
        btn.setForeground(textoColor);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBorde, 1),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static DefaultTableModel crearModeloTablaNoEditable(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void configurarTabla(JTable tabla) {
        tabla.setFont(FUENTE_NORMAL);
        tabla.setForeground(TEXTO_PRIMARIO);
        tabla.setBackground(BLANCO);
        tabla.setSelectionBackground(SELECCION_TABLA);
        tabla.setSelectionForeground(TEXTO_PRIMARIO);
        tabla.setRowHeight(26);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setGridColor(BORDE);
        tabla.setShowGrid(true);
        tabla.getTableHeader().setFont(FUENTE_NEGRITA);
        tabla.getTableHeader().setBackground(FONDO_BASE);
        tabla.getTableHeader().setForeground(TEXTO_PRIMARIO);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    public static void mostrarInfo(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Información del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void mostrarError(Component padre, Exception ex) {
        String titulo = "Aviso";
        int tipo = JOptionPane.WARNING_MESSAGE;

        if (ex instanceof BloqueoException) {
            titulo = "Acción Bloqueada";
        } else if (ex instanceof PermisoException) {
            titulo = "Acceso No Permitido";
            tipo = JOptionPane.ERROR_MESSAGE;
        } else if (ex instanceof ValidacionException) {
            titulo = "Verifique los Datos";
        } else if (ex instanceof ErrorDatosException) {
            titulo = "Error de Comunicación con Datos";
            tipo = JOptionPane.ERROR_MESSAGE;
        }

        String mensaje = (ex.getMessage() != null && !ex.getMessage().trim().isEmpty())
                ? ex.getMessage()
                : "Ocurrió un problema inesperado al procesar la solicitud.";
        JOptionPane.showMessageDialog(padre, mensaje, titulo, tipo);
    }

    public static boolean confirmar(Component padre, String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(
                padre,
                mensaje,
                "Confirmar Acción",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        return opcion == JOptionPane.YES_OPTION;
    }

    public static void fijarMargen(JComponent comp, int top, int left, int bottom, int right) {
        comp.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
    }
}