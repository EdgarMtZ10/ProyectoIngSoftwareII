package modelo;

import java.time.LocalDate;

/**
 * Representa un registro de la tabla {@code etapa}.
 * Cada etapa pertenece a un criterio de un proyecto y posee un orden secuencial.
 */
public class Etapa {
    private int idEtapa;
    private int idProyectoCriterio;
    private String nombre;
    private int orden;
    private LocalDate fechaInicio;
    private LocalDate fechaCierre;
    private EstadoEtapa estado;

    /** Constructor vacío requerido por convención de modelos. */
    public Etapa() {
    }

    public int getIdEtapa() { return idEtapa; }
    public void setIdEtapa(int idEtapa) { this.idEtapa = idEtapa; }

    public int getIdProyectoCriterio() { return idProyectoCriterio; }
    public void setIdProyectoCriterio(int idProyectoCriterio) { this.idProyectoCriterio = idProyectoCriterio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }

    public EstadoEtapa getEstado() { return estado; }
    public void setEstado(EstadoEtapa estado) { this.estado = estado; }
}