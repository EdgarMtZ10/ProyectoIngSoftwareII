package modelo;

import java.time.LocalDate;

/**
 * Representa un registro de la tabla {@code actividad}.
 * Corresponde a una tarea planificada dentro de una etapa con responsable y fechas.
 */
public class Actividad {
    private int idActividad;
    private int idEtapa;
    private String nombre;
    private String responsable;
    private LocalDate fechaInicio;
    private LocalDate fechaCierre;
    private boolean terminada;

    /** Constructor vacío requerido por convención de modelos. */
    public Actividad() {
    }

    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }

    public int getIdEtapa() { return idEtapa; }
    public void setIdEtapa(int idEtapa) { this.idEtapa = idEtapa; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }

    public boolean isTerminada() { return terminada; }
    public void setTerminada(boolean terminada) { this.terminada = terminada; }
}