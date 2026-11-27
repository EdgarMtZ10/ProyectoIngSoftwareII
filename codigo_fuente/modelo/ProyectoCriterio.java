package modelo;

import java.math.BigDecimal;

/**
 * Representa la tabla intermedia {@code proyecto_criterio}.
 * Asocia cada proyecto con uno de los seis criterios y almacena el puntaje asignado (0 a 100).
 */
public class ProyectoCriterio {
    private int idProyectoCriterio;
    private int idProyecto;
    private int idCriterio;
    /** Puntaje asignado entre 0.00 y 100.00; puede ser {@code null} mientras no se evalúe. */
    private BigDecimal puntaje;

    /** Constructor vacío requerido por convención de modelos. */
    public ProyectoCriterio() {
    }

    public int getIdProyectoCriterio() { return idProyectoCriterio; }
    public void setIdProyectoCriterio(int idProyectoCriterio) { this.idProyectoCriterio = idProyectoCriterio; }

    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public int getIdCriterio() { return idCriterio; }
    public void setIdCriterio(int idCriterio) { this.idCriterio = idCriterio; }

    public BigDecimal getPuntaje() { return puntaje; }
    public void setPuntaje(BigDecimal puntaje) { this.puntaje = puntaje; }
}