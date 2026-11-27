package modelo;

/**
 * DTO utilizado para listar entregables junto con su contexto jerárquico
 * (nombre de la actividad, etapa, criterio y proyecto) en la pantalla de revisión.
 */
public class EntregablePendiente {
    private int idEntregable;
    private int idActividad;
    private int idProyecto;
    private String nombreEntregable;
    private String nombreActividad;
    private String nombreEtapa;
    private String nombreCriterio;
    private String nombreProyecto;
    private EstadoEntregable estado;
    private String motivoRechazo;

    /** Constructor vacío requerido por convención de modelos. */
    public EntregablePendiente() {
    }

    public int getIdEntregable() { return idEntregable; }
    public void setIdEntregable(int idEntregable) { this.idEntregable = idEntregable; }

    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }

    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public String getNombreEntregable() { return nombreEntregable; }
    public void setNombreEntregable(String nombreEntregable) { this.nombreEntregable = nombreEntregable; }

    public String getNombreActividad() { return nombreActividad; }
    public void setNombreActividad(String nombreActividad) { this.nombreActividad = nombreActividad; }

    public String getNombreEtapa() { return nombreEtapa; }
    public void setNombreEtapa(String nombreEtapa) { this.nombreEtapa = nombreEtapa; }

    public String getNombreCriterio() { return nombreCriterio; }
    public void setNombreCriterio(String nombreCriterio) { this.nombreCriterio = nombreCriterio; }

    public String getNombreProyecto() { return nombreProyecto; }
    public void setNombreProyecto(String nombreProyecto) { this.nombreProyecto = nombreProyecto; }

    public EstadoEntregable getEstado() { return estado; }
    public void setEstado(EstadoEntregable estado) { this.estado = estado; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}