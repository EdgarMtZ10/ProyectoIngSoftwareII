package modelo;

/**
 * Clasificación final de factibilidad calculada a partir del promedio de los seis criterios.
 */
public enum ResultadoRecomendacion {
    /** Promedio mayor o igual a 70.00 puntos. */
    VIABLE,
    /** Promedio entre 40.00 y 69.99 puntos. */
    VIABLE_CON_AJUSTES,
    /** Promedio menor a 40.00 puntos. */
    NO_VIABLE
}