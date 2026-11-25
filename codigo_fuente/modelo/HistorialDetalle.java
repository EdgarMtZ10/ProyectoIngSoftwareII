package modelo;

import java.time.LocalDateTime;

/**
 * DTO de consulta para mostrar la bitácora de auditoría con el nombre y correo
 * del usuario que ejecutó cada acción.
 */
public class HistorialDetalle {
    private int idHistorial;
    private int idUsuario;
    private String nombreUsuario;
    private String correoUsuario;
    private String tablaAfectada;
    private int idRegistro;
    private AccionHistorial accion;
    private LocalDateTime fechaHora;

    /** Constructor vacío requerido por convención de modelos. */
    public HistorialDetalle() {
    }

    public int getIdHistorial() { return idHistorial; }
    public void setIdHistorial(int idHistorial) { this.idHistorial = idHistorial; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getCorreoUsuario() { return correoUsuario; }
    public void setCorreoUsuario(String correoUsuario) { this.correoUsuario = correoUsuario; }

    public String getTablaAfectada() { return tablaAfectada; }
    public void setTablaAfectada(String tablaAfectada) { this.tablaAfectada = tablaAfectada; }

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public AccionHistorial getAccion() { return accion; }
    public void setAccion(AccionHistorial accion) { this.accion = accion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}