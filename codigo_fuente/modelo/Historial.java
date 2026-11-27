package modelo;

import java.time.LocalDateTime;

/**
 * Representa un registro de auditoría en la tabla {@code historial}.
 * Registra quién realizó una acción, sobre qué tabla/registro y en qué fecha y hora.
 */
public class Historial {
    private int idHistorial;
    private int idUsuario;
    private String tablaAfectada;
    private int idRegistro;
    private AccionHistorial accion;
    private LocalDateTime fechaHora;

    /** Constructor vacío requerido por convención de modelos. */
    public Historial() {
    }

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public AccionHistorial getAccion() { return accion; }
    public void setAccion(AccionHistorial accion) { this.accion = accion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}