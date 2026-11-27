package servicio;

import datos.CriterioDao;
import datos.IdeaNegocioDao;
import datos.ProyectoCriterioDao;
import datos.ProyectoDao;
import excepciones.BloqueoException;
import excepciones.ValidacionException;
import modelo.AccionHistorial;
import modelo.Criterio;
import modelo.EstadoProyecto;
import modelo.IdeaNegocio;
import modelo.Proyecto;
import modelo.ResumenProyecto.PuntajeCriterioDetalle;
import modelo.RolUsuario;
import util.Sesion;
import util.Validaciones;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * Implementa el Caso de Uso 01 (registrar idea de negocio y crear proyecto con sus 6 criterios)
 * y las consultas/validaciones de estado de proyecto.
 */
public class ProyectoServicio {

    private final IdeaNegocioDao ideaDao = new IdeaNegocioDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final CriterioDao criterioDao = new CriterioDao();
    private final ProyectoCriterioDao proyectoCriterioDao = new ProyectoCriterioDao();

    /**
     * Registra una nueva idea de negocio validando título y descripción (CU-01).
     */
    public IdeaNegocio registrarIdea(String titulo, String descripcion) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        Validaciones.noVacio(titulo, "título de la idea");
        Validaciones.maxLargo(titulo, 150, "título de la idea");
        Validaciones.noVacio(descripcion, "descripción de la idea");

        return TransaccionHelper.ejecutarEnTransaccion(c -> {
            IdeaNegocio idea = new IdeaNegocio();
            idea.setIdUsuario(Sesion.getUsuario().getIdUsuario());
            idea.setTitulo(titulo.trim());
            idea.setDescripcion(descripcion.trim());
            idea.setFechaRegistro(LocalDate.now());

            int idIdea = ideaDao.insertar(c, idea);
            TransaccionHelper.auditar(c, "idea_negocio", idIdea, AccionHistorial.CREAR);
            return idea;
        });
    }

    /**
     * Crea un proyecto en estado {@code EN_PLANIFICACION} a partir de una idea existente,
     * asocia automáticamente los 6 criterios estándar y registra el evento en historial (CU-01).
     */
    public Proyecto crearProyectoDesdeIdea(int idIdea, String nombreProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA);
        Validaciones.noVacio(nombreProyecto, "nombre del proyecto");
        Validaciones.maxLargo(nombreProyecto, 150, "nombre del proyecto");

        return TransaccionHelper.ejecutarEnTransaccion(c -> {
            IdeaNegocio idea = ideaDao.buscarPorId(c, idIdea);
            if (idea == null) {
                throw new ValidacionException("La idea de negocio seleccionada no existe.");
            }

            List<Criterio> criterios = criterioDao.listarTodos(c);
            if (criterios.size() != 6) {
                throw new ValidacionException("El catálogo de criterios en base de datos está incompleto (se esperan 6 criterios).");
            }

            Proyecto proyecto = new Proyecto();
            proyecto.setIdIdea(idIdea);
            proyecto.setIdAnalista(Sesion.getUsuario().getIdUsuario());
            proyecto.setNombre(nombreProyecto.trim());
            proyecto.setFechaCreacion(LocalDate.now());
            proyecto.setEstado(EstadoProyecto.EN_PLANIFICACION);

            int idProyecto = proyectoDao.insertar(c, proyecto);
            for (Criterio criterio : criterios) {
                proyectoCriterioDao.insertar(c, idProyecto, criterio.getIdCriterio());
            }

            TransaccionHelper.auditar(c, "proyecto", idProyecto, AccionHistorial.CREAR);
            Sesion.setProyectoActual(proyecto);
            return proyecto;
        });
    }

    /**
     * Lista todas las ideas de negocio registradas.
     */
    public List<IdeaNegocio> listarIdeas() {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(ideaDao::listarTodas);
    }

    /**
     * Busca proyectos aplicando filtros opcionales por nombre y estado.
     */
    public List<Proyecto> buscarProyectos(String texto, EstadoProyecto estado) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> proyectoDao.buscar(c, texto, estado));
    }

    /**
     * Obtiene un proyecto por su identificador y lo establece como proyecto activo en sesión.
     */
    public Proyecto seleccionarProyecto(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        Proyecto proyecto = TransaccionHelper.ejecutarLectura(c -> proyectoDao.buscarPorId(c, idProyecto));
        if (proyecto == null) {
            throw new ValidacionException("El proyecto solicitado no existe.");
        }
        Sesion.setProyectoActual(proyecto);
        return proyecto;
    }

    /**
     * Lista los 6 criterios asociados a un proyecto junto con sus puntajes actuales.
     */
    public List<PuntajeCriterioDetalle> listarCriteriosDeProyecto(int idProyecto) {
        Validaciones.requiereRol(RolUsuario.ANALISTA, RolUsuario.APROBADOR, RolUsuario.ADMINISTRADOR);
        return TransaccionHelper.ejecutarLectura(c -> proyectoCriterioDao.listarDetallePorProyecto(c, idProyecto));
    }

    /**
     * Guarda reutilizable para impedir cualquier modificación sobre proyectos cerrados (CU-08).
     */
    static Proyecto verificarEditable(Connection c, ProyectoDao proyectoDao, int idProyecto) {
        Proyecto proyecto = proyectoDao.buscarPorId(c, idProyecto);
        if (proyecto == null) {
            throw new ValidacionException("El proyecto asociado no existe.");
        }
        if (proyecto.getEstado() == EstadoProyecto.FINALIZADO) {
            throw new BloqueoException("El proyecto '" + proyecto.getNombre() + "' ya está finalizado y es de solo consulta.");
        }
        if (proyecto.getEstado() == EstadoProyecto.CANCELADO) {
            throw new BloqueoException("El proyecto '" + proyecto.getNombre() + "' está cancelado y no admite modificaciones.");
        }
        return proyecto;
    }
}