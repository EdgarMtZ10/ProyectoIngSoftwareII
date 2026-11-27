package util;

import modelo.Proyecto;
import modelo.Usuario;

/**
 * Mantiene en memoria el contexto de la sesión activa: el usuario autenticado
 * y el proyecto seleccionado actualmente para trabajar.
 */
public class Sesion {
    private static Usuario usuarioActual;
    private static Proyecto proyectoActual;

    /**
     * Inicia la sesión con el usuario autenticado y limpia cualquier proyecto previo.
     *
     * @param usuario Usuario que inició sesión correctamente.
     */
    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
        proyectoActual = null;
    }

    /**
     * Devuelve el usuario que tiene la sesión iniciada.
     *
     * @return Usuario actual o {@code null} si no hay sesión activa.
     */
    public static Usuario getUsuario() {
        return usuarioActual;
    }

    /**
     * Devuelve el proyecto abierto actualmente en el espacio de trabajo.
     *
     * @return Proyecto seleccionado o {@code null} si aún no se ha abierto ninguno.
     */
    public static Proyecto getProyectoActual() {
        return proyectoActual;
    }

    /**
     * Establece el proyecto activo sobre el cual operarán las pantallas y servicios.
     *
     * @param proyecto Proyecto seleccionado por el usuario.
     */
    public static void setProyectoActual(Proyecto proyecto) {
        proyectoActual = proyecto;
    }

    /**
     * Cierra la sesión actual limpiando tanto el usuario como el proyecto activo.
     */
    public static void cerrar() {
        usuarioActual = null;
        proyectoActual = null;
    }
}