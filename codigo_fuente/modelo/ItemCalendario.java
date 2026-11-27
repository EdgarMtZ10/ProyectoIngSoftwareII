package modelo;

import java.time.LocalDate;

/**
 * Objeto de transferencia de datos (DTO) utilizado para alimentar la vista
 * del calendario del proyecto ({@code PanelCalendario}) en una sola consulta.
 */
public class ItemCalendario {
    /** Indica si el elemento corresponde a "ETAPA" o "ACTIVIDAD". */
    private String tipo;
    private int id;
    private String nombre;
    private LocalDate inicio;
    private LocalDate fin;
    private String estado;
    private String criterio;

    /** Constructor vacío requerido por convención de modelos. */
    public ItemCalendario() {
    }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDate getInicio() { return inicio; }
    public void setInicio(LocalDate inicio) { this.inicio = inicio; }

    public LocalDate getFin() { return fin; }
    public void setFin(LocalDate fin) { this.fin = fin; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCriterio() { return criterio; }
    public void setCriterio(String criterio) { this.criterio = criterio; }
}