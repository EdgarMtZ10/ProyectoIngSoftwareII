# modelo

Las clases que representan los datos. **Responsable: Edgar (HU-E00).** Las clases de datos para pantallas (la última tabla) las hace cada quien según su historia.

## Una clase por tabla

`Usuario`, `IdeaNegocio`, `Proyecto`, `Criterio`, `ProyectoCriterio`, `Etapa`, `Actividad`, `Entregable`, `DatoEstudio`, `Recomendacion` e `Historial`.

## Valores fijos (enums)

Con los mismos valores que el script SQL. En la base se guardan como texto con `name()`.

| Enum | Valores |
|---|---|
| `RolUsuario` | ANALISTA, APROBADOR, ADMINISTRADOR |
| `EstadoProyecto` | EN_PLANIFICACION, EN_EJECUCION, FINALIZADO, CANCELADO |
| `TipoCriterio` | MERCADO, TECNICO, OPERATIVO, LEGAL, FINANCIERO, AMBIENTAL |
| `EstadoEtapa` | PENDIENTE, EN_PROGRESO, COMPLETADA, BLOQUEADA |
| `EstadoEntregable` | PENDIENTE, APROBADO, RECHAZADO |
| `CategoriaDato` | MERCADO, COSTO, INVERSION, FLUJO, RIESGO, SUPUESTO |
| `ResultadoRecomendacion` | VIABLE, VIABLE_CON_AJUSTES, NO_VIABLE |
| `AccionHistorial` | CREAR, MODIFICAR, APROBAR, RECHAZAR, FINALIZAR, DESACTIVAR, BLOQUEAR |

## Clases de datos para pantallas y reportes

No son tablas: llevan el resultado de una consulta a la ventana.

| Clase | Responsable | Historia | Contenido |
|---|---|---|---|
| `ItemCalendario` | Edgar | HU-E06 | tipo (etapa o actividad), id, nombre, inicio, fin, estado y criterio |
| `Avance` | Edgar | HU-E09 | avance de cada etapa y del proyecto |
| `Atraso` | Edgar | HU-E09 | tipo, id, nombre, fecha de cierre y días de atraso |
| `EntregablePendiente` | José Pablo | HU-J02 | id, nombre del entregable, actividad, etapa y proyecto |
| `ResumenProyecto` | José Pablo | HU-J05 | todos los datos de la pantalla resumen y del archivo exportado |

## Reglas

- Cada clase solo tiene atributos privados, un constructor vacío, y getters y setters. **Sin SQL, sin ventanas y sin reglas.** (VS Code genera los getters y setters solo: clic derecho, `Source Action`, `Generate Getters and Setters`.)
- Tipos: `int`, `String`, `LocalDate`, `LocalDateTime`, `BigDecimal` y `boolean`. Nunca `double`, `float` ni `java.util.Date`.
- Si una columna acepta NULL, el atributo usa el tipo con mayúscula (`Integer` en vez de `int`), porque `int` no puede quedar vacío. Por ejemplo `Entregable.idAprobador` es `Integer`.
- Los campos de texto restringidos usan su enum, no `String`.
- Si cambian un enum, cambien también la restricción `CHECK` del script SQL de `database/`.
