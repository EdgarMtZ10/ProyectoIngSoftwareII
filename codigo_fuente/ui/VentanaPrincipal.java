package ui;

import modelo.Proyecto;
import modelo.RolUsuario;
import modelo.Usuario;
import servicio.AutenticacionServicio;
import util.Sesion;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Ventana principal del sistema con barra superior de estado, menú lateral filtrado por rol
 * y área central intercambiada mediante {@link CardLayout} (RNF1, RNF6).
 */
public class VentanaPrincipal extends JFrame {

    private final AutenticacionServicio authServicio = new AutenticacionServicio();

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contenedorCentral = new JPanel(cardLayout);
    private final JPanel panelMenuLateral = new JPanel();

    private final JLabel lblUsuarioActivo = new JLabel();
    private final JLabel lblProyectoActivo = new JLabel();

    private final Map<String, JPanel> modulos = new LinkedHashMap<>();
    private final Map<String, JButton> botonesMenu = new LinkedHashMap<>();
    private String claveModuloActual = null;

    public VentanaPrincipal() {
        setTitle("Sistema Gestor de Factibilidad con Planificación Visual");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 720);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);

        Usuario usuario = Sesion.getUsuario();
        if (usuario == null) {
            dispose();
            new LoginFrame().setVisible(true);
            return;
        }

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(EstilosUI.FONDO_BASE);

        raiz.add(construirBarraSuperior(usuario), BorderLayout.NORTH);
        raiz.add(construirBarraLateral(), BorderLayout.WEST);

        contenedorCentral.setBackground(EstilosUI.FONDO_BASE);
        raiz.add(contenedorCentral, BorderLayout.CENTER);

        setContentPane(raiz);

        cargarModulosPorRol(usuario.getRol());
        actualizarBarraEstado();
    }

    private JPanel construirBarraSuperior(Usuario usuario) {
        JPanel barra = new JPanel(new BorderLayout(16, 0));
        barra.setBackground(EstilosUI.AZUL_PRIMARIO);
        barra.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        // Izquierda: Título del sistema y proyecto seleccionado
        JPanel bloqueIzq = new JPanel();
        bloqueIzq.setLayout(new BoxLayout(bloqueIzq, BoxLayout.Y_AXIS));
        bloqueIzq.setOpaque(false);

        JLabel lblTitulo = new JLabel("Sistema Gestor de Factibilidad");
        lblTitulo.setFont(EstilosUI.FUENTE_TITULO);
        lblTitulo.setForeground(EstilosUI.BLANCO);

        lblProyectoActivo.setFont(EstilosUI.FUENTE_PEQUENA);
        lblProyectoActivo.setForeground(EstilosUI.BLANCO);

        bloqueIzq.add(lblTitulo);
        bloqueIzq.add(Box.createVerticalStrut(2));
        bloqueIzq.add(lblProyectoActivo);

        // Derecha: Usuario en sesión y botón de salida
        JPanel bloqueDer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        bloqueDer.setOpaque(false);

        lblUsuarioActivo.setFont(EstilosUI.FUENTE_NEGRITA);
        lblUsuarioActivo.setForeground(EstilosUI.BLANCO);
        lblUsuarioActivo.setText(usuario.getNombre() + "  [" + usuario.getRol().name() + "]");

        JButton btnCerrarSesion = EstilosUI.crearBotonSecundario("Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());

        bloqueDer.add(lblUsuarioActivo);
        bloqueDer.add(btnCerrarSesion);

        barra.add(bloqueIzq, BorderLayout.WEST);
        barra.add(bloqueDer, BorderLayout.EAST);
        return barra;
    }

    private JPanel construirBarraLateral() {
        panelMenuLateral.setLayout(new BoxLayout(panelMenuLateral, BoxLayout.Y_AXIS));
        panelMenuLateral.setBackground(EstilosUI.BLANCO);
        panelMenuLateral.setPreferredSize(new Dimension(225, 0));
        panelMenuLateral.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, EstilosUI.BORDE),
                BorderFactory.createEmptyBorder(14, 10, 14, 10)
        ));
        return panelMenuLateral;
    }

    /**
     * Registra en el menú lateral únicamente los módulos autorizados para el rol activo (RNF1).
     * Los paneles marcados como "Módulo asignado a compañero" pueden sustituirse directamente
     * por las clases concretas cuando estén listas.
     */
    private void cargarModulosPorRol(RolUsuario rol) {
        JLabel lblNav = new JLabel("MÓDULOS DEL SISTEMA");
        lblNav.setFont(EstilosUI.FUENTE_PEQUENA);
        lblNav.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        lblNav.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNav.setBorder(BorderFactory.createEmptyBorder(0, 8, 10, 8));
        panelMenuLateral.add(lblNav);

        if (rol == RolUsuario.ADMINISTRADOR) {
            registrarModulo("USUARIOS", "Gestión de Usuarios", new PanelUsuarios());
            registrarModulo("PROYECTOS", "Consulta de Proyectos", new PanelProyectos(this::actualizarBarraEstado));
            registrarModulo("HISTORIAL", "Bitácora de Auditoría",
                    crearPanelEnConstruccion("Bitácora de Auditoría",
                            "Módulo en desarrollo por compañero (CierreServicio.consultarHistorial)."));
        } else if (rol == RolUsuario.ANALISTA) {
            registrarModulo("PROYECTOS", "1. Ideas y Proyectos", new PanelProyectos(this::actualizarBarraEstado));

            // TODO Compañero: reemplazar por new PanelPlanificacion()
            registrarModulo("PLANIFICACION", "2. Etapas y Calendario",
                    crearPanelEnConstruccion("Planificación de Etapas y Calendario Visual",
                            "Módulo en desarrollo por compañero (PlanificacionServicio)."));

            // TODO Compañero: reemplazar por new PanelDatosEstudio()
            registrarModulo("DATOS", "3. Datos del Estudio",
                    crearPanelEnConstruccion("Captura de Datos del Estudio",
                            "Módulo en desarrollo por compañero (DatoEstudioServicio)."));

            // TODO Compañero: reemplazar por new PanelEntregables()
            registrarModulo("ENTREGABLES", "4. Estado de Entregables",
                    crearPanelEnConstruccion("Consulta de Entregables",
                            "Módulo en desarrollo por compañero (EntregableServicio)."));

            // TODO Compañero: reemplazar por new PanelSeguimiento()
            registrarModulo("SEGUIMIENTO", "5. Avance y Bloqueos",
                    crearPanelEnConstruccion("Avance, Atrasos y Control de Bloqueos",
                            "Módulo en desarrollo por compañero (SeguimientoServicio)."));

            // TODO Compañero: reemplazar por new PanelEvaluacionCierre()
            registrarModulo("CIERRE", "6. Evaluación y Cierre",
                    crearPanelEnConstruccion("Evaluación de Criterios, Recomendación y Cierre",
                            "Módulo en desarrollo por compañero (EvaluacionServicio y CierreServicio)."));
        } else if (rol == RolUsuario.APROBADOR) {
            registrarModulo("PROYECTOS", "1. Proyectos (Consulta)", new PanelProyectos(this::actualizarBarraEstado));

            // TODO Compañero: reemplazar por new PanelEntregables()
            registrarModulo("ENTREGABLES", "2. Revisar Entregables",
                    crearPanelEnConstruccion("Bandeja de Revisión de Entregables",
                            "Módulo en desarrollo por compañero (EntregableServicio)."));

            // TODO Compañero: reemplazar por new PanelEvaluacionCierre()
            registrarModulo("RESUMEN", "3. Resumen e Historial",
                    crearPanelEnConstruccion("Resumen Ejecutivo y Bitácora",
                            "Módulo en desarrollo por compañero (CierreServicio)."));
        }

        panelMenuLateral.add(Box.createVerticalGlue());

        // Mostrar el primer módulo disponible al abrir la ventana
        if (!modulos.isEmpty()) {
            String primeraClave = modulos.keySet().iterator().next();
            mostrarModulo(primeraClave);
        }
    }

    private void registrarModulo(String clave, String etiquetaBoton, JPanel panel) {
        modulos.put(clave, panel);
        contenedorCentral.add(panel, clave);

        JButton btn = new JButton(etiquetaBoton);
        btn.setFont(EstilosUI.FUENTE_NEGRITA);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        estilizarBotonMenu(btn, false);

        btn.addActionListener(e -> mostrarModulo(clave));

        botonesMenu.put(clave, btn);
        panelMenuLateral.add(btn);
        panelMenuLateral.add(Box.createVerticalStrut(4));
    }

    /**
     * Cambia la vista activa en el panel central y dispara {@link PanelModulo#refrescar()}.
     */
    public void mostrarModulo(String clave) {
        if (!modulos.containsKey(clave)) {
            return;
        }
        claveModuloActual = clave;
        cardLayout.show(contenedorCentral, clave);

        for (Map.Entry<String, JButton> entry : botonesMenu.entrySet()) {
            estilizarBotonMenu(entry.getValue(), entry.getKey().equals(clave));
        }

        JPanel panelActivo = modulos.get(clave);
        if (panelActivo instanceof PanelModulo) {
            ((PanelModulo) panelActivo).refrescar();
        }
        actualizarBarraEstado();
    }

    private void estilizarBotonMenu(JButton btn, boolean activo) {
        if (activo) {
            btn.setBackground(EstilosUI.AZUL_ACCION);
            btn.setForeground(EstilosUI.BLANCO);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        } else {
            btn.setBackground(EstilosUI.BLANCO);
            btn.setForeground(EstilosUI.TEXTO_PRIMARIO);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        }
    }

    /**
     * Actualiza en la barra superior el nombre y estado del proyecto seleccionado en sesión.
     */
    public void actualizarBarraEstado() {
        Proyecto proyecto = Sesion.getProyectoActual();
        if (proyecto == null) {
            lblProyectoActivo.setText("Proyecto activo: Ninguno seleccionado (seleccione uno en Proyectos)");
        } else {
            lblProyectoActivo.setText("Proyecto activo: #" + proyecto.getIdProyecto() + " - " +
                    proyecto.getNombre() + "  [" + proyecto.getEstado().name() + "]");
        }
    }

    private void cerrarSesion() {
        authServicio.cerrarSesion();
        dispose();
        new LoginFrame().setVisible(true);
    }

    /**
     * Panel temporal limpio para los módulos que implementará el compañero de equipo.
     */
    private JPanel crearPanelEnConstruccion(String titulo, String detalle) {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(EstilosUI.FONDO_BASE);
        EstilosUI.fijarMargen(contenedor, 12, 12, 12, 12);

        JPanel superficie = new JPanel(new BorderLayout(0, 12));
        EstilosUI.aplicarSuperficiePrincipal(superficie);

        superficie.add(EstilosUI.crearEncabezadoSeccion(titulo), BorderLayout.NORTH);

        JLabel lblDetalle = new JLabel(detalle);
        lblDetalle.setFont(EstilosUI.FUENTE_NORMAL);
        lblDetalle.setForeground(EstilosUI.TEXTO_SECUNDARIO);
        lblDetalle.setVerticalAlignment(SwingConstants.TOP);
        superficie.add(lblDetalle, BorderLayout.CENTER);

        contenedor.add(superficie, BorderLayout.CENTER);
        return contenedor;
    }
}