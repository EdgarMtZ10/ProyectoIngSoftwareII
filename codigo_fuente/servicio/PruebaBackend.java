package servicio;

import excepciones.BloqueoException;
import modelo.Actividad;
import modelo.Avance;
import modelo.CategoriaDato;
import modelo.Entregable;
import modelo.Etapa;
import modelo.HistorialDetalle;
import modelo.IdeaNegocio;
import modelo.Proyecto;
import modelo.Recomendacion;
import modelo.ResumenProyecto.PuntajeCriterioDetalle;
import modelo.RolUsuario;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Prueba de integración por consola que recorre los 8 Casos de Uso completos
 * contra la base de datos {@code sistema_factibilidad}.
 */
public class PruebaBackend {

    public static void main(String[] args) {
        AutenticacionServicio auth = new AutenticacionServicio();
        UsuarioServicio usuarioSrv = new UsuarioServicio();
        ProyectoServicio proyectoSrv = new ProyectoServicio();
        PlanificacionServicio planSrv = new PlanificacionServicio();
        DatoEstudioServicio datoSrv = new DatoEstudioServicio();
        EntregableServicio entregableSrv = new EntregableServicio();
        SeguimientoServicio seguimientoSrv = new SeguimientoServicio();
        EvaluacionServicio evaluacionSrv = new EvaluacionServicio();
        CierreServicio cierreSrv = new CierreServicio();

        System.out.println("=== 1. INICIALIZANDO USUARIOS DE PRUEBA ===");
        usuarioSrv.inicializarAdministradorSiVacio("Administrador General", "admin@cuc.ac.cr", "Admin1234");
        auth.iniciarSesion("admin@cuc.ac.cr", "Admin1234");

        if (usuarioSrv.listarUsuarios().size() == 1) {
            usuarioSrv.crearUsuario("Edgar Analista", "analista@cuc.ac.cr", "Analista1234", RolUsuario.ANALISTA);
            usuarioSrv.crearUsuario("José Pablo Aprobador", "aprobador@cuc.ac.cr", "Aprobador1234", RolUsuario.APROBADOR);
        }
        auth.cerrarSesion();

        System.out.println("=== 2. CU-01: IDEA Y PROYECTO CON 6 CRITERIOS ===");
        auth.iniciarSesion("analista@cuc.ac.cr", "Analista1234");
        IdeaNegocio idea = proyectoSrv.registrarIdea(
                "Cafetería de Especialidad en Cartago",
                "Apertura de cafetería con enfoque universitario cerca del CUC.");
        Proyecto proyecto = proyectoSrv.crearProyectoDesdeIdea(
                idea.getIdIdea(), "Estudio Factibilidad Cafetería CUC");
        List<PuntajeCriterioDetalle> criterios = proyectoSrv.listarCriteriosDeProyecto(proyecto.getIdProyecto());
        System.out.println("Proyecto creado ID=" + proyecto.getIdProyecto() + " con " + criterios.size() + " criterios.");

        System.out.println("=== 3. CU-02 & CU-03: ETAPAS, ACTIVIDADES, ENTREGABLES Y DATOS ===");
        LocalDate hoy = LocalDate.now();
        // En el primer criterio creamos 2 etapas secuenciales para probar el bloqueo (CU-06)
        PuntajeCriterioDetalle cMercado = criterios.get(0);
        Etapa etapa1 = planSrv.crearEtapa(cMercado.getIdProyectoCriterio(), "Levantamiento de Demanda", hoy, hoy.plusDays(10));
        Actividad act1 = planSrv.crearActividad(etapa1.getIdEtapa(), "Encuesta a estudiantes", "Edgar MV", hoy, hoy.plusDays(5));
        Entregable ent1 = planSrv.crearEntregable(act1.getIdActividad(), "Informe de Encuestas.pdf");

        Etapa etapa2 = planSrv.crearEtapa(cMercado.getIdProyectoCriterio(), "Proyección de Ventas", hoy.plusDays(11), hoy.plusDays(20));
        Actividad act2 = planSrv.crearActividad(etapa2.getIdEtapa(), "Modelo de demanda anual", "Edgar MV", hoy.plusDays(11), hoy.plusDays(18));

        datoSrv.registrarDato(etapa1.getIdEtapa(), CategoriaDato.MERCADO, "Población estudiantil objetivo", new BigDecimal("3500.00"), "Semestral");
        datoSrv.registrarDato(etapa1.getIdEtapa(), CategoriaDato.INVERSION, "Equipamiento inicial de barra", new BigDecimal("12500.50"), "Inicial");

        // En los otros 5 criterios creamos 1 etapa y 1 actividad cada uno
        for (int i = 1; i < criterios.size(); i++) {
            PuntajeCriterioDetalle crit = criterios.get(i);
            Etapa et = planSrv.crearEtapa(crit.getIdProyectoCriterio(), "Análisis " + crit.getNombreCriterio(), hoy, hoy.plusDays(10));
            Actividad ac = planSrv.crearActividad(et.getIdEtapa(), "Validación " + crit.getTipo().name(), "Edgar MV", hoy, hoy.plusDays(8));
            seguimientoSrv.marcarActividadTerminada(ac.getIdActividad());
        }

        System.out.println("=== 4. CU-06: PRUEBA DE BLOQUEO SECUENCIAL ===");
        try {
            seguimientoSrv.marcarActividadTerminada(act2.getIdActividad());
        } catch (BloqueoException ex) {
            System.out.println("Bloqueo detectado correctamente -> " + ex.getMessage());
        }

        System.out.println("=== 5. CU-04: APROBACIÓN DE ENTREGABLE Y DESBLOQUEO ===");
        seguimientoSrv.marcarActividadTerminada(act1.getIdActividad());
        auth.cerrarSesion();

        auth.iniciarSesion("aprobador@cuc.ac.cr", "Aprobador1234");
        entregableSrv.revisarEntregable(ent1.getIdEntregable(), true, null);
        auth.cerrarSesion();

        auth.iniciarSesion("analista@cuc.ac.cr", "Analista1234");
        seguimientoSrv.marcarActividadTerminada(act2.getIdActividad());

        Avance avance = seguimientoSrv.calcularAvanceProyecto(proyecto.getIdProyecto());
        System.out.println("Avance global actual: " + avance.getPorcentajeGeneral() + "%");

        System.out.println("=== 6. CU-07: EVALUACIÓN DE 6 CRITERIOS Y RECOMENDACIÓN ===");
        BigDecimal[] notas = {
                new BigDecimal("85.00"), new BigDecimal("78.50"), new BigDecimal("80.00"),
                new BigDecimal("90.00"), new BigDecimal("74.00"), new BigDecimal("88.50")
        };
        for (int i = 0; i < criterios.size(); i++) {
            evaluacionSrv.evaluarCriterio(criterios.get(i).getIdProyectoCriterio(), notas[i]);
        }
        Recomendacion rec = evaluacionSrv.generarRecomendacion(proyecto.getIdProyecto());
        System.out.println("Promedio: " + rec.getPromedio() + " -> Dictamen: " + rec.getResultado());

        System.out.println("=== 7. CU-08: FINALIZAR, EXPORTAR PDF/CSV E HISTORIAL ===");
        cierreSrv.finalizarProyecto(proyecto.getIdProyecto());
        File pdf = cierreSrv.exportarResumenPdf(proyecto.getIdProyecto(), new File("Resumen_Prueba.pdf"));
        File csv = cierreSrv.exportarResumenCsv(proyecto.getIdProyecto(), new File("Resumen_Prueba.csv"));
        System.out.println("Archivos exportados: " + pdf.getName() + " y " + csv.getName());

        List<HistorialDetalle> historial = cierreSrv.consultarHistorial(null);
        System.out.println("Total eventos en bitácora inmutable: " + historial.size());
        auth.cerrarSesion();
        System.out.println("=== PRUEBA INTEGRAL FINALIZADA CON ÉXITO ===");
    }
}