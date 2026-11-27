package modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa el porcentaje de avance global de un proyecto
 * y el desglose individual por cada etapa.
 */
public class Avance {
    private int idProyecto;
    private int totalActividades;
    private int actividadesTerminadas;
    private BigDecimal porcentajeGeneral = BigDecimal.ZERO;
    private List<AvanceEtapa> etapas = new ArrayList<>();

    public Avance() {
    }

    public int getIdProyecto() {
        return idProyecto;
    }

    public void setIdProyecto(int idProyecto) {
        this.idProyecto = idProyecto;
    }

    public int getTotalActividades() {
        return totalActividades;
    }

    public void setTotalActividades(int totalActividades) {
        this.totalActividades = totalActividades;
    }

    public int getActividadesTerminadas() {
        return actividadesTerminadas;
    }

    public void setActividadesTerminadas(int actividadesTerminadas) {
        this.actividadesTerminadas = actividadesTerminadas;
    }

    public BigDecimal getPorcentajeGeneral() {
        return porcentajeGeneral;
    }

    public void setPorcentajeGeneral(BigDecimal porcentajeGeneral) {
        this.porcentajeGeneral = porcentajeGeneral;
    }

    public List<AvanceEtapa> getEtapas() {
        return etapas;
    }

    public void setEtapas(List<AvanceEtapa> etapas) {
        this.etapas = etapas;
    }

    /**
     * Desglose del avance de una etapa específica.
     */
    public static class AvanceEtapa {
        private int idEtapa;
        private String nombreEtapa;
        private String criterio;
        private EstadoEtapa estado;
        private int totalActividades;
        private int actividadesTerminadas;
        private BigDecimal porcentaje = BigDecimal.ZERO;

        public AvanceEtapa() {
        }

        public int getIdEtapa() {
            return idEtapa;
        }

        public void setIdEtapa(int idEtapa) {
            this.idEtapa = idEtapa;
        }

        public String getNombreEtapa() {
            return nombreEtapa;
        }

        public void setNombreEtapa(String nombreEtapa) {
            this.nombreEtapa = nombreEtapa;
        }

        public String getCriterio() {
            return criterio;
        }

        public void setCriterio(String criterio) {
            this.criterio = criterio;
        }

        public EstadoEtapa getEstado() {
            return estado;
        }

        public void setEstado(EstadoEtapa estado) {
            this.estado = estado;
        }

        public int getTotalActividades() {
            return totalActividades;
        }

        public void setTotalActividades(int totalActividades) {
            this.totalActividades = totalActividades;
        }

        public int getActividadesTerminadas() {
            return actividadesTerminadas;
        }

        public void setActividadesTerminadas(int actividadesTerminadas) {
            this.actividadesTerminadas = actividadesTerminadas;
        }

        public BigDecimal getPorcentaje() {
            return porcentaje;
        }

        public void setPorcentaje(BigDecimal porcentaje) {
            this.porcentaje = porcentaje;
        }
    }
}