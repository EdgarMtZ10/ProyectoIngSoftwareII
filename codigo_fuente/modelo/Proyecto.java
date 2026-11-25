package modelo;

import java.time.LocalDate;

/**
 * Representa un registro de la tabla {@code proyecto}.
 * Vincula una idea de negocio con un analista responsable y su estado actual.
 */
public class Proyecto {
    private int idProyecto;
    private int idIdea;
    private int idAnalista;
    private String nombre;
    private LocalDate fechaCreacion;
    private EstadoProyecto estado;

    /** Constructor vacío requerido por convención de modelos. */
    public Proyecto() {
    }

    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public int getIdIdea() { return idIdea; }
    public void setIdIdea(int idIdea) { this.idIdea = idIdea; }

    public int getIdAnalista() { return idAnalista; }
    public void setIdAnalista(int idAnalista) { this.idAnalista = idAnalista; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public EstadoProyecto getEstado() { return estado; }
    public void setEstado(EstadoProyecto estado) { this.estado = estado; }
}