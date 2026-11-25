package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa un registro de la tabla {@code recomendacion}.
 * Guarda el promedio calculado de los seis criterios y el dictamen final del proyecto.
 */
public class Recomendacion {
    private int idRecomendacion;
    private int idProyecto;
    private BigDecimal promedio;
    private ResultadoRecomendacion resultado;
    private LocalDateTime fechaGeneracion;

    /** Constructor vacío requerido por convención de modelos. */
    public Recomendacion() {
    }

    public int getIdRecomendacion() { return idRecomendacion; }
    public void setIdRecomendacion(int idRecomendacion) { this.idRecomendacion = idRecomendacion; }

    public int getIdProyecto() { return idProyecto; }
    public void setIdProyecto(int idProyecto) { this.idProyecto = idProyecto; }

    public BigDecimal getPromedio() { return promedio; }
    public void setPromedio(BigDecimal promedio) { this.promedio = promedio; }

    public ResultadoRecomendacion getResultado() { return resultado; }
    public void setResultado(ResultadoRecomendacion resultado) { this.resultado = resultado; }

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
}