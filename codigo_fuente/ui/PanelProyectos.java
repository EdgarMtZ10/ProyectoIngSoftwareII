package ui;

import modelo.EstadoProyecto;
import modelo.IdeaNegocio;
import modelo.Proyecto;
import modelo.ResumenProyecto.PuntajeCriterioDetalle;
import modelo.RolUsuario;
import servicio.ProyectoServicio;
import util.Sesion;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Pantalla del Caso de Uso 01: registro de ideas de negocio, creación de proyectos
 * con sus 6 criterios fijos y selección del proyecto activo, sin contenedores anidados.
 */
public class PanelProyectos extends JPanel implements PanelModulo {

    private final ProyectoServicio proyectoServicio = new ProyectoServicio();
    private final Runnable alCambiarProyecto;

    // Formulario Idea
    private final JTextField txtTituloIdea = new JTextField(22);
    private final JTextArea txtDescripcionIdea = new JTextArea(3, 22);
    private final JButton btnGuardarIdea = EstilosUI.crearBotonPrimario("Guardar Idea");

    // Formulario Proyecto
    private final JComboBox<IdeaNegocio> cmbIdeas = new JComboBox<>();
    private final JTextField txtNombreProyecto = new JTextField(22);
    private final JButton btnCrearProyecto = EstilosUI.crearBotonExito("Crear Proyecto (6 Criterios)");

    // Búsqueda y tablas
    private final JTextField txtFiltroNombre = new JTextField(16);
    private final JComboBox<String> cmbFiltroEstado = new JComboBox<>();

    private final DefaultTableModel modeloProyectos = EstilosUI.crearModeloTablaNoEditable(
            "ID", "Nombre del Proyecto", "Fecha Creación", "Estado"
    );
    private final JTable tablaProyectos = new JTable(modeloProyectos);

    private final DefaultTableModel modeloCriterios = EstilosUI.crearModeloTablaNoEditable(
            "ID", "Criterio de Factibilidad", "Clave", "Puntaje"
    );
    private final JTable tablaCriterios = new JTable(modeloCriterios);

    public PanelProyectos(Runnable alCambiarProyecto) {
        this.alCambiarProyecto = alCambiarProyecto;
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO_BASE);
        EstilosUI.fijarMargen(this, 12, 12, 12, 12);

        JPanel superficie = new JPanel(new BorderLayout(0, 14));
        EstilosUI.aplicarSuperficiePrincipal(superficie);

        cmbIdeas.setFont(EstilosUI.FUENTE_NORMAL);
        cmbIdeas.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof IdeaNegocio) {
                    IdeaNegocio idea = (IdeaNegocio) value;
                    setText("#" + idea.getIdIdea() + " - " + idea.getTitulo());
                }
                return this;
            }
        });

        // Sección superior: 2 columnas separadas por una línea vertical simple
        JPanel bloqueSuperior = new JPanel(new BorderLayout(0, 12));
        bloqueSuperior.setBackground(EstilosUI.BLANCO);

        JPanel gridFormularios = new JPanel(new GridBagLayout());
        gridFormularios.setBackground(EstilosUI.BLANCO);

        GridBagConstraints topGbc = new GridBagConstraints();
        topGbc.fill = GridBagConstraints.BOTH;
        topGbc.weighty = 1.0;

        topGbc.gridx = 0;
        topGbc.weightx = 0.5;
        topGbc.insets = new Insets(0, 0, 0, 14);
        gridFormularios.add(construirSeccionIdea(), topGbc);

        topGbc.gridx = 1;
        topGbc.weightx = 0.0;
        topGbc.insets = new Insets(0, 0, 0, 0);
        gridFormularios.add(new JSeparator(SwingConstants.VERTICAL), topGbc);

        topGbc.gridx = 2;
        topGbc.weightx = 0.5;
        topGbc.insets = new Insets(0, 14, 0, 0);
        gridFormularios.add(construirSeccionProyecto(), topGbc);

        bloqueSuperior.add(gridFormularios, BorderLayout.CENTER);
        bloqueSuperior.add(new JSeparator(), BorderLayout.SOUTH);

        superficie.add(bloqueSuperior, BorderLayout.NORTH);

        // Sección inferior: Tabla de proyectos y tabla de sus 6 criterios
        JPanel bloqueListado = new JPanel(new BorderLayout(0, 8));
        bloqueListado.setBackground(EstilosUI.BLANCO);

        JPanel barraFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        barraFiltros.setBackground(EstilosUI.BLANCO);
        barraFiltros.add(EstilosUI.crearEtiqueta("Buscar proyecto:", false));
        txtFiltroNombre.setFont(EstilosUI.FUENTE_NORMAL);
        barraFiltros.add(txtFiltroNombre);

        barraFiltros.add(EstilosUI.crearEtiqueta("Estado:", false));
        cmbFiltroEstado.setFont(EstilosUI.FUENTE_NORMAL);
        cmbFiltroEstado.addItem("TODOS");
        for (EstadoProyecto ep : EstadoProyecto.values()) {
            cmbFiltroEstado.addItem(ep.name());
        }
        barraFiltros.add(cmbFiltroEstado);

        JButton btnBuscar = EstilosUI.crearBotonSecundario("Filtrar");
        JButton btnSeleccionar = EstilosUI.crearBotonPrimario("Activar Proyecto Seleccionado");
        barraFiltros.add(btnBuscar);
        barraFiltros.add(btnSeleccionar);

        bloqueListado.add(barraFiltros, BorderLayout.NORTH);

        EstilosUI.configurarTabla(tablaProyectos);
        tablaProyectos.getColumnModel().getColumn(0).setMaxWidth(55);

        EstilosUI.configurarTabla(tablaCriterios);
        tablaCriterios.getColumnModel().getColumn(0).setMaxWidth(55);

        JPanel colProyectos = new JPanel(new BorderLayout(0, 6));
        colProyectos.setBackground(EstilosUI.BLANCO);
        colProyectos.add(EstilosUI.crearEncabezadoSeccion("Proyectos Registrados"), BorderLayout.NORTH);
        colProyectos.add(new JScrollPane(tablaProyectos), BorderLayout.CENTER);

        JPanel colCriterios = new JPanel(new BorderLayout(0, 6));
        colCriterios.setBackground(EstilosUI.BLANCO);
        colCriterios.add(EstilosUI.crearEncabezadoSeccion("6 Criterios del Proyecto"), BorderLayout.NORTH);
        colCriterios.add(new JScrollPane(tablaCriterios), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, colProyectos, colCriterios);
        split.setResizeWeight(0.58);
        split.setDividerSize(8);
        split.setBorder(null);
        split.setBackground(EstilosUI.BLANCO);
        bloqueListado.add(split, BorderLayout.CENTER);

        superficie.add(bloqueListado, BorderLayout.CENTER);
        add(superficie, BorderLayout.CENTER);

        // Eventos
        btnGuardarIdea.addActionListener(e -> guardarIdea());
        btnCrearProyecto.addActionListener(e -> crearProyecto());
        btnBuscar.addActionListener(e -> cargarTablaProyectos());
        btnSeleccionar.addActionListener(e -> activarProyectoSeleccionado());

        tablaProyectos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaProyectos.getSelectedRow() >= 0) {
                int idProyecto = (Integer) modeloProyectos.getValueAt(tablaProyectos.getSelectedRow(), 0);
                cargarCriterios(idProyecto);
            }
        });
    }

    private JPanel construirSeccionIdea() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(EstilosUI.BLANCO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        gbc.gridy = 0;
        p.add(EstilosUI.crearEncabezadoSeccion("1. Registrar Idea de Negocio"), gbc);

        gbc.gridy = 1;
        p.add(EstilosUI.crearEtiqueta("Título de la idea", true), gbc);

        gbc.gridy = 2;
        txtTituloIdea.setFont(EstilosUI.FUENTE_NORMAL);
        p.add(txtTituloIdea, gbc);

        gbc.gridy = 3;
        p.add(EstilosUI.crearEtiqueta("Descripción detallada", true), gbc);

        gbc.gridy = 4;
        txtDescripcionIdea.setFont(EstilosUI.FUENTE_NORMAL);
        txtDescripcionIdea.setLineWrap(true);
        txtDescripcionIdea.setWrapStyleWord(true);
        p.add(new JScrollPane(txtDescripcionIdea), gbc);

        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(8, 0, 0, 0);
        p.add(btnGuardarIdea, gbc);

        return p;
    }

    private JPanel construirSeccionProyecto() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(EstilosUI.BLANCO);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        gbc.gridy = 0;
        p.add(EstilosUI.crearEncabezadoSeccion("2. Crear Proyecto de Factibilidad"), gbc);

        gbc.gridy = 1;
        p.add(EstilosUI.crearEtiqueta("Idea de negocio base", true), gbc);

        gbc.gridy = 2;
        p.add(cmbIdeas, gbc);

        gbc.gridy = 3;
        p.add(EstilosUI.crearEtiqueta("Nombre del proyecto", true), gbc);

        gbc.gridy = 4;
        txtNombreProyecto.setFont(EstilosUI.FUENTE_NORMAL);
        p.add(txtNombreProyecto, gbc);

        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(18, 0, 0, 0);
        p.add(btnCrearProyecto, gbc);

        return p;
    }

    @Override
    public void refrescar() {
        boolean esAnalista = Sesion.getUsuario() != null && Sesion.getUsuario().getRol() == RolUsuario.ANALISTA;
        txtTituloIdea.setEnabled(esAnalista);
        txtDescripcionIdea.setEnabled(esAnalista);
        btnGuardarIdea.setEnabled(esAnalista);
        cmbIdeas.setEnabled(esAnalista);
        txtNombreProyecto.setEnabled(esAnalista);
        btnCrearProyecto.setEnabled(esAnalista);

        cargarComboIdeas();
        cargarTablaProyectos();
        if (Sesion.getProyectoActual() != null) {
            cargarCriterios(Sesion.getProyectoActual().getIdProyecto());
        }
    }

    private void cargarComboIdeas() {
        try {
            List<IdeaNegocio> ideas = proyectoServicio.listarIdeas();
            cmbIdeas.removeAllItems();
            for (IdeaNegocio idea : ideas) {
                cmbIdeas.addItem(idea);
            }
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }

    private void cargarTablaProyectos() {
        try {
            String texto = txtFiltroNombre.getText();
            String estadoStr = (String) cmbFiltroEstado.getSelectedItem();
            EstadoProyecto estado = (estadoStr == null || "TODOS".equals(estadoStr))
                    ? null
                    : EstadoProyecto.valueOf(estadoStr);

            List<Proyecto> proyectos = proyectoServicio.buscarProyectos(texto, estado);
            modeloProyectos.setRowCount(0);
            for (Proyecto p : proyectos) {
                modeloProyectos.addRow(new Object[]{
                        p.getIdProyecto(),
                        p.getNombre(),
                        p.getFechaCreacion(),
                        p.getEstado().name()
                });
            }
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }

    private void cargarCriterios(int idProyecto) {
        try {
            List<PuntajeCriterioDetalle> criterios = proyectoServicio.listarCriteriosDeProyecto(idProyecto);
            modeloCriterios.setRowCount(0);
            for (PuntajeCriterioDetalle pc : criterios) {
                modeloCriterios.addRow(new Object[]{
                        pc.getIdProyectoCriterio(),
                        pc.getNombreCriterio(),
                        pc.getTipo().name(),
                        pc.getPuntaje() != null ? pc.getPuntaje().toPlainString() : "Sin evaluar"
                });
            }
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }

    private void guardarIdea() {
        try {
            IdeaNegocio creada = proyectoServicio.registrarIdea(
                    txtTituloIdea.getText(),
                    txtDescripcionIdea.getText()
            );
            EstilosUI.mostrarInfo(this, "Idea de negocio #" + creada.getIdIdea() + " registrada correctamente.");
            txtTituloIdea.setText("");
            txtDescripcionIdea.setText("");
            cargarComboIdeas();
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }

    private void crearProyecto() {
        IdeaNegocio ideaSeleccionada = (IdeaNegocio) cmbIdeas.getSelectedItem();
        if (ideaSeleccionada == null) {
            EstilosUI.mostrarInfo(this, "Debe registrar o seleccionar una idea de negocio primero.");
            return;
        }
        try {
            Proyecto nuevo = proyectoServicio.crearProyectoDesdeIdea(
                    ideaSeleccionada.getIdIdea(),
                    txtNombreProyecto.getText()
            );
            EstilosUI.mostrarInfo(this,
                    "Proyecto '" + nuevo.getNombre() + "' creado en estado EN_PLANIFICACION con sus 6 criterios.");
            txtNombreProyecto.setText("");
            cargarTablaProyectos();
            cargarCriterios(nuevo.getIdProyecto());
            if (alCambiarProyecto != null) {
                alCambiarProyecto.run();
            }
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }

    private void activarProyectoSeleccionado() {
        int fila = tablaProyectos.getSelectedRow();
        if (fila < 0) {
            EstilosUI.mostrarInfo(this, "Seleccione un proyecto de la tabla para activarlo.");
            return;
        }
        int idProyecto = (Integer) modeloProyectos.getValueAt(fila, 0);
        try {
            Proyecto p = proyectoServicio.seleccionarProyecto(idProyecto);
            cargarCriterios(idProyecto);
            if (alCambiarProyecto != null) {
                alCambiarProyecto.run();
            }
            EstilosUI.mostrarInfo(this, "Proyecto activo: " + p.getNombre() + " (" + p.getEstado().name() + ")");
        } catch (Exception ex) {
            EstilosUI.mostrarError(this, ex);
        }
    }
}