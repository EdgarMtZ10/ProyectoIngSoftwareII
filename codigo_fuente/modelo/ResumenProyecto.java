package modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO completo que consolida toda la información ejecutiva de un proyecto
 * para la pantalla resumen y la exportación a PDF o CSV.
 */
public class ResumenProyecto {
    private Proyecto proyecto;
    private IdeaNegocio idea;
    private String nombreAnalista;
    private Avance avance;
    private List<PuntajeCriterioDetalle> puntajes = new ArrayList<>();
    private List<DatoEstudio> datosEstudio = new ArrayList<>();
    private Recomendacion recomendacion;

    /** Constructor vacío requerido por convención de modelos. */
    public ResumenProyecto() {
    }

    public Proyecto getProyecto() { return proyecto; }
    public void setProyecto(Proyecto proyecto) { this.proyecto = proyecto; }

    public IdeaNegocio getIdea() { return idea; }
    public void setIdea(IdeaNegocio idea) { this.idea = idea; }

    public String getNombreAnalista() { return nombreAnalista; }
    public void setNombreAnalista(String nombreAnalista) { this.nombreAnalista = nombreAnalista; }

    public Avance getAvance() { return avance; }
    public void setAvance(Avance avance) { this.avance = avance; }

    public List<PuntajeCriterioDetalle> getPuntajes() { return puntajes; }
    public void setPuntajes(List<PuntajeCriterioDetalle> puntajes) { this.puntajes = puntajes; }

    public List<DatoEstudio> getDatosEstudio() { return datosEstudio; }
    public void setDatosEstudio(List<DatoEstudio> datosEstudio) { this.datosEstudio = datosEstudio; }

    public Recomendacion getRecomendacion() { return recomendacion; }
    public void setRecomendacion(Recomendacion recomendacion) { this.recomendacion = recomendacion; }

    /**
     * Detalle de puntaje asociado al nombre y tipo del criterio.
     */
    public static class PuntajeCriterioDetalle {
        private int idProyectoCriterio;
        private int idCriterio;
        private TipoCriterio tipo;
        private String nombreCriterio;
        private BigDecimal puntaje;

        public PuntajeCriterioDetalle() {
        }

        public int getIdProyectoCriterio() { return idProyectoCriterio; }
        public void setIdProyectoCriterio(int idProyectoCriterio) { this.idProyectoCriterio = idProyectoCriterio; }

        public int getIdCriterio() { return idCriterio; }
        public void setIdCriterio(int idCriterio) { this.idCriterio = idCriterio; }

        public TipoCriterio getTipo() { return tipo; }
        public void setTipo(TipoCriterio tipo) { this.tipo = tipo; }

        public String getNombreCriterio() { return nombreCriterio; }
        public void setNombreCriterio(String nombreCriterio) { this.nombreCriterio = nombreCriterio; }

        public BigDecimal getPuntaje() { return puntaje; }
        public void setPuntaje(BigDecimal puntaje) { this.puntaje = puntaje; }
    }
}