package modelo;

/**
 * Representa un registro del catálogo {@code criterio}.
 * Define cada una de las seis dimensiones de evaluación del sistema.
 */
public class Criterio {
    private int idCriterio;
    private TipoCriterio tipo;
    private String nombre;

    /** Constructor vacío requerido por convención de modelos. */
    public Criterio() {
    }

    public int getIdCriterio() { return idCriterio; }
    public void setIdCriterio(int idCriterio) { this.idCriterio = idCriterio; }

    public TipoCriterio getTipo() { return tipo; }
    public void setTipo(TipoCriterio tipo) { this.tipo = tipo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}