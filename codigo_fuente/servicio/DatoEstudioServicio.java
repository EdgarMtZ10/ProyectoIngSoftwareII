package servicio;

import datos.DatoEstudioDao;
import datos.EtapaDao;
import datos.ProyectoDao;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.CategoriaDato;
import modelo.DatoEstudio;
import modelo.Etapa;
import modelo.RolUsuario;
import util.Validaciones;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Implementa el Caso de Uso 03: captura estructurada de datos del estudio por etapa
 * (mercado, costo, inversión, flujo, riesgo, supuesto) con redondeo exacto a 2 decimales (RNF3).
 */
public class DatoEstudioServicio {

    private final DatoEstudioDao datoDao = new DatoEstudioDao();
    private final EtapaDao etapaDao = new EtapaDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();

    /**
     * Registra un dato del estudio validando campos obligatorios y prohibiendo montos negativos
     * en las categorías {@code COSTO} e {@code INVERSION} (CU-03).
     */
    public DatoEstudio registrarDato(int idEtapa, CategoriaDato categoria, String descripcion,
                                     BigDecimal monto, String periodo) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        if (categoria == null) {
            throw new ValidacionException("Debe seleccionar una categoría para el dato del estudio.");
        }
        Validaciones.noVacio(descripcion, "descripción del dato");
        Validaciones.maxLargo(descripcion, 255, "descripción del dato");
        if (periodo != null && !periodo.trim().isEmpty()) {
            Validaciones.maxLargo(periodo, 20, "período");
        }

        BigDecimal montoNormalizado = null;
        if (monto != null) {
            montoNormalizado = monto.setScale(2, RoundingMode.HALF_UP);
            if ((categoria == CategoriaDato.COSTO || categoria == CategoriaDato.INVERSION)
                    && montoNormalizado.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidacionException(
                        "El monto no puede ser negativo para la categoría " + categoria.name() + ".");
            }
        } else if (categoria == CategoriaDato.COSTO || categoria == CategoriaDato.INVERSION) {
            throw new ValidacionException("Debe ingresar un monto numérico para la categoría " + categoria.name() + ".");
        }

        final BigDecimal montoFinal = montoNormalizado;
        return TransaccionHelper.ejecutarEnTransaccion(c -> {
            Etapa etapa = etapaDao.buscarPorId(c, idEtapa);
            if (etapa == null) {
                throw new ValidacionException("La etapa seleccionada no existe.");
            }
            int idProyecto = etapaDao.obtenerIdProyectoPorEtapa(c, idEtapa);
            ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);

            DatoEstudio dato = new DatoEstudio();
            dato.setIdEtapa(idEtapa);
            dato.setCategoria(categoria);
            dato.setDescripcion(descripcion.trim());
            dato.setMonto(montoFinal);
            dato.setPeriodo(periodo == null || periodo.trim().isEmpty() ? null : periodo.trim());

            int idDato = datoDao.insertar(c, dato);
            TransaccionHelper.auditar(c, "dato_estudio", idDato, AccionHistorial.CREAR);
            return dato;
        });
    }

    public List<DatoEstudio> listarPorEtapa(int idEtapa) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> datoDao.listarPorEtapa(c, idEtapa));
    }

    public List<DatoEstudio> listarPorProyecto(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> datoDao.listarPorProyecto(c, idProyecto));
    }
}