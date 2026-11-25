package servicio;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import datos.DatoEstudioDao;
import datos.HistorialDao;
import datos.IdeaNegocioDao;
import datos.ProyectoCriterioDao;
import datos.ProyectoDao;
import datos.RecomendacionDao;
import datos.UsuarioDao;
import excepciones.BloqueoException;
import excepciones.ErrorDatosException;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.DatoEstudio;
import modelo.EstadoProyecto;
import modelo.HistorialDetalle;
import modelo.Proyecto;
import modelo.Recomendacion;
import modelo.ResumenProyecto;
import modelo.ResumenProyecto.PuntajeCriterioDetalle;
import modelo.RolUsuario;
import modelo.Usuario;
import util.Sesion;
import util.Validaciones;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementa el Caso de Uso 08: consolidación del resumen ejecutivo, cierre definitivo
 * del proyecto en modo solo consulta, exportación a PDF y CSV, y consulta del historial inmutable.
 */
public class CierreServicio {

    private static final DateTimeFormatter FMT_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final IdeaNegocioDao ideaDao = new IdeaNegocioDao();
    private final UsuarioDao usuarioDao = new UsuarioDao();
    private final ProyectoCriterioDao proyectoCriterioDao = new ProyectoCriterioDao();
    private final DatoEstudioDao datoDao = new DatoEstudioDao();
    private final RecomendacionDao recomendacionDao = new RecomendacionDao();
    private final HistorialDao historialDao = new HistorialDao();
    private final SeguimientoServicio seguimientoServicio = new SeguimientoServicio();

    /**
     * Construye el objeto consolidado {@link ResumenProyecto} con todos los datos del estudio.
     */
    public ResumenProyecto obtenerResumenProyecto(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> construirResumenInterno(c, idProyecto));
    }

    /**
     * Marca el proyecto como {@code FINALIZADO} exigiendo que tenga una recomendación generada (CU-08).
     * A partir de este punto cualquier intento de modificación será rechazado.
     */
    public void finalizarProyecto(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);

        TransaccionHelper.ejecutarEnTransaccion(c -> {
            Proyecto proyecto = ProyectoServicio.verificarEditable(c, proyectoDao, idProyecto);
            Recomendacion rec = recomendacionDao.buscarPorProyecto(c, idProyecto);
            if (rec == null) {
                throw new BloqueoException(
                        "No se puede finalizar el proyecto porque aún no se ha generado la recomendación final.");
            }

            proyectoDao.actualizarEstado(c, idProyecto, EstadoProyecto.FINALIZADO);
            proyecto.setEstado(EstadoProyecto.FINALIZADO);
            if (Sesion.getProyectoActual() != null && Sesion.getProyectoActual().getIdProyecto() == idProyecto) {
                Sesion.setProyectoActual(proyecto);
            }

            TransaccionHelper.auditar(c, "proyecto", idProyecto, AccionHistorial.FINALIZAR);
            return null;
        });
    }

    /**
     * Consulta la bitácora inmutable de cambios (opcionalmente filtrada por nombre de tabla).
     */
    public List<HistorialDetalle> consultarHistorial(String tablaAfectada) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> historialDao.listar(c, tablaAfectada));
    }

    /**
     * Exporta el resumen ejecutivo completo del proyecto a un documento PDF usando {@code OpenPDF}.
     */
    public File exportarResumenPdf(int idProyecto, File archivoDestino) {
        ResumenProyecto resumen = obtenerResumenProyecto(idProyecto);
        if (archivoDestino == null) {
            throw new ValidacionException("Debe indicar la ruta del archivo PDF a exportar.");
        }

        Document doc = new Document(PageSize.LETTER, 40, 40, 40, 40);
        try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
            PdfWriter.getInstance(doc, fos);
            doc.open();

            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(25, 55, 109));
            Font fSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(40, 40, 40));
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

            Paragraph titulo = new Paragraph("RESUMEN EJECUTIVO DE FACTIBILIDAD", fTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(12f);
            doc.add(titulo);

            Proyecto p = resumen.getProyecto();
            doc.add(new Paragraph("Proyecto: " + p.getNombre(), fNegrita));
            doc.add(new Paragraph("Idea de negocio: " + resumen.getIdea().getTitulo(), fNormal));
            doc.add(new Paragraph("Analista responsable: " + resumen.getNombreAnalista(), fNormal));
            doc.add(new Paragraph("Fecha de creación: " + p.getFechaCreacion() + "   |   Estado: " + p.getEstado().name(), fNormal));
            doc.add(new Paragraph("Avance general: " + resumen.getAvance().getPorcentajeGeneral() + "% (" +
                    resumen.getAvance().getActividadesTerminadas() + "/" +
                    resumen.getAvance().getTotalActividades() + " actividades)", fNormal));
            doc.add(new Paragraph(" "));

            // Tabla de 6 criterios
            doc.add(new Paragraph("1. Evaluación de los 6 Criterios de Factibilidad", fSubtitulo));
            PdfPTable tablaCriterios = new PdfPTable(new float[]{4f, 1.5f});
            tablaCriterios.setWidthPercentage(100);
            tablaCriterios.setSpacingBefore(6f);
            tablaCriterios.setSpacingAfter(12f);
            agregarCeldaEncabezado(tablaCriterios, "Criterio");
            agregarCeldaEncabezado(tablaCriterios, "Puntaje (0-100)");

            for (PuntajeCriterioDetalle pc : resumen.getPuntajes()) {
                tablaCriterios.addCell(new Phrase(pc.getNombreCriterio(), fNormal));
                String nota = pc.getPuntaje() != null ? pc.getPuntaje().toPlainString() : "Sin evaluar";
                tablaCriterios.addCell(new Phrase(nota, fNormal));
            }
            doc.add(tablaCriterios);

            // Recomendación final
            doc.add(new Paragraph("2. Dictamen y Recomendación Final", fSubtitulo));
            if (resumen.getRecomendacion() != null) {
                Recomendacion r = resumen.getRecomendacion();
                doc.add(new Paragraph("Promedio general: " + r.getPromedio() + " / 100.00", fNegrita));
                doc.add(new Paragraph("Resultado: " + r.getResultado().name(), fNegrita));
                doc.add(new Paragraph("Fecha de generación: " + r.getFechaGeneracion().format(FMT_FECHA_HORA), fNormal));
            } else {
                doc.add(new Paragraph("Aún no se ha generado la recomendación final.", fNormal));
            }
            doc.add(new Paragraph(" "));

            // Datos del estudio
            doc.add(new Paragraph("3. Datos Registrados en el Estudio (" + resumen.getDatosEstudio().size() + ")", fSubtitulo));
            if (resumen.getDatosEstudio().isEmpty()) {
                doc.add(new Paragraph("No hay datos financieros o técnicos registrados.", fNormal));
            } else {
                PdfPTable tablaDatos = new PdfPTable(new float[]{1.5f, 3.5f, 1.5f, 1.5f});
                tablaDatos.setWidthPercentage(100);
                tablaDatos.setSpacingBefore(6f);
                agregarCeldaEncabezado(tablaDatos, "Categoría");
                agregarCeldaEncabezado(tablaDatos, "Descripción");
                agregarCeldaEncabezado(tablaDatos, "Monto");
                agregarCeldaEncabezado(tablaDatos, "Período");

                for (DatoEstudio d : resumen.getDatosEstudio()) {
                    tablaDatos.addCell(new Phrase(d.getCategoria().name(), fNormal));
                    tablaDatos.addCell(new Phrase(d.getDescripcion(), fNormal));
                    tablaDatos.addCell(new Phrase(d.getMonto() != null ? d.getMonto().toPlainString() : "—", fNormal));
                    tablaDatos.addCell(new Phrase(d.getPeriodo() != null ? d.getPeriodo() : "—", fNormal));
                }
                doc.add(tablaDatos);
            }

            doc.close();
            return archivoDestino;
        } catch (DocumentException | IOException e) {
            throw new ErrorDatosException("No se pudo generar el archivo PDF del resumen.", e);
        }
    }

    /**
     * Exporta el resumen ejecutivo a un archivo CSV compatible con Excel (UTF-8 con BOM).
     */
    public File exportarResumenCsv(int idProyecto, File archivoDestino) {
        ResumenProyecto resumen = obtenerResumenProyecto(idProyecto);
        if (archivoDestino == null) {
            throw new ValidacionException("Debe indicar la ruta del archivo CSV a exportar.");
        }

        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(archivoDestino), StandardCharsets.UTF_8))) {
            // Marca BOM UTF-8 para que Excel abra tildes correctamente
            bw.write('\uFEFF');
            Proyecto p = resumen.getProyecto();
            bw.write("PROYECTO;IDEA;ANALISTA;FECHA_CREACION;ESTADO;AVANCE_PORCENTAJE\n");
            bw.write(escaparCsv(p.getNombre()) + ";" +
                     escaparCsv(resumen.getIdea().getTitulo()) + ";" +
                     escaparCsv(resumen.getNombreAnalista()) + ";" +
                     p.getFechaCreacion() + ";" +
                     p.getEstado().name() + ";" +
                     resumen.getAvance().getPorcentajeGeneral() + "\n\n");

            bw.write("CRITERIO;TIPO;PUNTAJE\n");
            for (PuntajeCriterioDetalle pc : resumen.getPuntajes()) {
                bw.write(escaparCsv(pc.getNombreCriterio()) + ";" +
                         pc.getTipo().name() + ";" +
                         (pc.getPuntaje() != null ? pc.getPuntaje().toPlainString() : "PENDIENTE") + "\n");
            }
            bw.write("\n");

            bw.write("PROMEDIO_FINAL;RECOMENDACION;FECHA_GENERACION\n");
            if (resumen.getRecomendacion() != null) {
                Recomendacion r = resumen.getRecomendacion();
                bw.write(r.getPromedio() + ";" + r.getResultado().name() + ";" +
                         r.getFechaGeneracion().format(FMT_FECHA_HORA) + "\n\n");
            } else {
                bw.write("N/A;PENDIENTE;N/A\n\n");
            }

            bw.write("CATEGORIA;DESCRIPCION;MONTO;PERIODO\n");
            for (DatoEstudio d : resumen.getDatosEstudio()) {
                bw.write(d.getCategoria().name() + ";" +
                         escaparCsv(d.getDescripcion()) + ";" +
                         (d.getMonto() != null ? d.getMonto().toPlainString() : "") + ";" +
                         escaparCsv(d.getPeriodo() != null ? d.getPeriodo() : "") + "\n");
            }
            return archivoDestino;
        } catch (IOException e) {
            throw new ErrorDatosException("No se pudo exportar el archivo CSV del resumen.", e);
        }
    }

    private ResumenProyecto construirResumenInterno(Connection c, int idProyecto) {
        Proyecto proyecto = proyectoDao.buscarPorId(c, idProyecto);
        if (proyecto == null) {
            throw new ValidacionException("El proyecto solicitado no existe.");
        }
        ResumenProyecto res = new ResumenProyecto();
        res.setProyecto(proyecto);
        res.setIdea(ideaDao.buscarPorId(c, proyecto.getIdIdea()));
        Usuario analista = usuarioDao.buscarPorId(c, proyecto.getIdAnalista());
        res.setNombreAnalista(analista != null ? analista.getNombre() : "Desconocido");
        res.setAvance(seguimientoServicio.calcularAvanceInterno(c, idProyecto));
        res.setPuntajes(proyectoCriterioDao.listarDetallePorProyecto(c, idProyecto));
        res.setDatosEstudio(datoDao.listarPorProyecto(c, idProyecto));
        res.setRecomendacion(recomendacionDao.buscarPorProyecto(c, idProyecto));
        return res;
    }

    private void agregarCeldaEncabezado(PdfPTable tabla, String texto) {
        Font fEnc = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell celda = new PdfPCell(new Phrase(texto, fEnc));
        celda.setBackgroundColor(new Color(25, 55, 109));
        celda.setPadding(6f);
        tabla.addCell(celda);
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        String limpio = valor.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ");
        return "\"" + limpio + "\"";
    }
}