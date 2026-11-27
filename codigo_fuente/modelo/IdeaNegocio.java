package modelo;

import java.time.LocalDate;

/**
 * Representa un registro de la tabla {@code idea_negocio}.
 * Contiene la propuesta inicial de negocio a partir de la cual se origina un proyecto.
 */
public class IdeaNegocio {
    private int idIdea;
    private int idUsuario;
    private String titulo;
    private String descripcion;
    private LocalDate fechaRegistro;

    /** Constructor vacío requerido por convención de modelos. */
    public IdeaNegocio() {
    }

    public int getIdIdea() { return idIdea; }
    public void setIdIdea(int idIdea) { this.idIdea = idIdea; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}