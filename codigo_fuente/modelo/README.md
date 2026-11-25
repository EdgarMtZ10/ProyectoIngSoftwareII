# paquete `modelo` — Entidades, Enums y DTOs

Define las estructuras de datos puras del dominio en Java. Representa tanto el mapeo directo de las 11 tablas de MySQL como los valores enumerados y objetos de transferencia de datos (DTO) usados en consultas, calendario, avance, resumen y auditoría.

## 1. Enumeraciones (`enum`)

Garantizan seguridad de tipos en compilación y coinciden exactamente con las restricciones `CHECK` de MySQL mediante `.name()` y `.valueOf(...)`:

| Enum | Valores permitidos | Uso |
| :--- | :--- | :--- |
| `RolUsuario` | `ANALISTA`, `APROBADOR`, `ADMINISTRADOR` | Control de permisos y menús por rol. |
| `EstadoProyecto` | `EN_PLANIFICACION`, `EN_EJECUCION`, `FINALIZADO`, `CANCELADO` | Ciclo de vida del proyecto y bloqueo de solo lectura. |
| `TipoCriterio` | `MERCADO`, `TECNICO`, `OPERATIVO`, `LEGAL`, `FINANCIERO`, `AMBIENTAL` | Los 6 criterios fijos de factibilidad. |
| `EstadoEtapa` | `PENDIENTE`, `EN_PROGRESO`, `COMPLETADA`, `BLOQUEADA` | Estado operativo y secuencial de cada etapa. |
| `EstadoEntregable` | `PENDIENTE`, `APROBADO`, `RECHAZADO` | Flujo de revisión de entregables. |
| `CategoriaDato` | `MERCADO`, `COSTO`, `INVERSION`, `FLUJO`, `RIESGO`, `SUPUESTO` | Clasificación de datos del estudio. |
| `ResultadoRecomendacion` | `VIABLE`, `VIABLE_CON_AJUSTES`, `NO_VIABLE` | Dictamen automático según el promedio. |
| `AccionHistorial` | `CREAR`, `MODIFICAR`, `APROBAR`, `RECHAZAR`, `FINALIZAR`, `DESACTIVAR`, `BLOQUEAR` | Eventos auditables en bitácora. |

## 2. Entidades de Tabla (11 clases)

Cada clase corresponde 1 a 1 con una tabla de `sistema_factibilidad` y sus columnas en formato `camelCase`:

* `Usuario` (`usuario`)
* `IdeaNegocio` (`idea_negocio`)
* `Proyecto` (`proyecto`)
* `Criterio` (`criterio`)
* `ProyectoCriterio` (`proyecto_criterio`)
* `Etapa` (`etapa`)
* `Actividad` (`actividad`)
* `Entregable` (`entregable`)
* `DatoEstudio` (`dato_estudio`)
* `Recomendacion` (`recomendacion`)
* `Historial` (`historial`)

## 3. Objetos de Transferencia de Datos — DTOs (6 clases)

Transportan resultados consolidados desde las consultas de `datos` hacia `servicio` y `ui`:

| Clase | Propósito |
| :--- | :--- |
| `ItemCalendario` | Unifica etapas y actividades (`tipo`, `id`, `nombre`, `inicio`, `fin`, `estado`, `criterio`) para la vista calendario. |
| `Avance` | Contiene el porcentaje global del proyecto, conteos de actividades y la lista interna `AvanceEtapa` por cada etapa. |
| `Atraso` | Representa una etapa o actividad vencida (`fechaCierre < hoy`) junto con el cálculo de `diasAtraso`. |
| `EntregablePendiente` | Enriquece un entregable con el nombre de su actividad, etapa, criterio y proyecto para la bandeja de revisión. |
| `ResumenProyecto` | Consolida proyecto, idea, analista, avance, detalle de puntajes (`PuntajeCriterioDetalle`), datos del estudio y recomendación para la vista ejecutiva y exportación PDF/CSV. |
| `HistorialDetalle` | Combina cada asiento de `historial` con el `nombreUsuario` y `correoUsuario` que ejecutó la acción. |

## Reglas de diseño

* **Sin lógica ni SQL:** Ninguna clase de `modelo` importa `java.sql.*` ni `javax.swing.*`.
* **Tipos exactos:** Se utiliza `BigDecimal` para montos y puntajes, `LocalDate` para fechas, `LocalDateTime` para marcas de tiempo y tipos envoltorio (`Integer`) en llaves foráneas que admiten `NULL` (como `Entregable.idAprobador`).