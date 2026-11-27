package modelo;

import java.time.LocalDateTime;

/**
 * Representa un registro de la tabla {@code entregable}.
 * Es la evidencia asociada a una actividad que debe ser revisada por un aprobador.
 */
public class Entregable {
    private int idEntregable;
    private int idActividad;
    /** Identificador del aprobador que revisó; es {@code null} mientras siga PENDIENTE. */
    private Integer idAprobador;
    private String nombre;
    private EstadoEntregable estado;
    /** Obligatorio cuando el estado es RECHAZADO; {@code null} en otros estados. */
    private String motivoRechazo;
    /** Fecha y hora de revisión; {@code null} mientras siga PENDIENTE. */
    private LocalDateTime fechaRevision;

    /** Constructor vacío requerido por convención de modelos. */
    public Entregable() {
    }

    public int getIdEntregable() { return idEntregable; }
    public void setIdEntregable(int idEntregable) { this.idEntregable = idEntregable; }

    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }

    public Integer getIdAprobador() { return idAprobador; }
    public void setIdAprobador(Integer idAprobador) { this.idAprobador = idAprobador; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public EstadoEntregable getEstado() { return estado; }
    public void setEstado(EstadoEntregable estado) { this.estado = estado; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
}