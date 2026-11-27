package modelo;

import java.math.BigDecimal;

/**
 * Representa un registro de la tabla {@code dato_estudio}.
 * Almacena hallazgos, costos, inversiones, flujos o supuestos asociados a una etapa.
 */
public class DatoEstudio {
    private int idDato;
    private int idEtapa;
    private CategoriaDato categoria;
    private String descripcion;
    /** Monto económico asociado; puede ser {@code null} en categorías cualitativas. */
    private BigDecimal monto;
    private String periodo;

    /** Constructor vacío requerido por convención de modelos. */
    public DatoEstudio() {
    }

    public int getIdDato() { return idDato; }
    public void setIdDato(int idDato) { this.idDato = idDato; }

    public int getIdEtapa() { return idEtapa; }
    public void setIdEtapa(int idEtapa) { this.idEtapa = idEtapa; }

    public CategoriaDato getCategoria() { return categoria; }
    public void setCategoria(CategoriaDato categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
}