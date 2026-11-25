package modelo;

import java.time.LocalDate;

/**
 * Objeto de transferencia de datos (DTO) que representa una etapa o actividad
 * cuya fecha de cierre ya venció sin haber sido completada o terminada.
 */
public class Atraso {
    /** Indica si el elemento atrasado es "ETAPA" o "ACTIVIDAD". */
    private String tipo;
    private int id;
    private String nombre;
    private LocalDate fechaCierre;
    /** Cantidad de días transcurridos desde {@code fechaCierre} hasta hoy. */
    private long diasAtraso;

    /** Constructor vacío requerido por convención de modelos. */
    public Atraso() {
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDate fechaCierre) { this.fechaCierre = fechaCierre; }

    public long getDiasAtraso() { return diasAtraso; }
    public void setDiasAtraso(long diasAtraso) { this.diasAtraso = diasAtraso; }
}