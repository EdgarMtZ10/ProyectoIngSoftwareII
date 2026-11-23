# servicio

Las reglas de negocio, los permisos por rol y las transacciones. Es la capa que decide.

## Servicios de Edgar

| Clase | Historia | Qué hace |
|---|---|---|
| `AutenticacionServicio` | HU-E01 | Inicio de sesión, rechaza usuarios con `activo = 0` |
| `ProyectoServicio` | HU-E02, E03, E07 | Crear idea y proyecto, listar, cancelar y pasar a EN_EJECUCION |
| `PlanificacionServicio` | HU-E04, E05, E06 | Etapas, actividades, calendario y cambio de fechas |
| `BloqueoServicio` | HU-E07, E08 | Etapa completa, criterio completo y marcar actividad terminada |
| `EstadoEtapaServicio` | HU-E07 | Recalcula y guarda el estado de una etapa |
| `AvanceServicio` | HU-E09 | Avance y atrasos |
| `HistorialServicio` | HU-E00 | `registrar(...)` en cada escritura |
| `Reglas` | HU-E00 | `exigirProyectoEditable(...)`: un proyecto finalizado o cancelado no se modifica |

## Servicios de José Pablo

| Clase | Historia | Qué hace |
|---|---|---|
| `DatoEstudioServicio` | HU-J01 | Guardar y listar los datos del estudio |
| `EntregableRevisionServicio` | HU-J02 | Aprobar o rechazar entregables |
| `EvaluacionServicio` | HU-J03, J04 | Puntajes y recomendación |
| `ResumenServicio` | HU-J05 | Resumen y finalizar el proyecto |
| `ExportacionServicio` | HU-J06 | Elige el exportador según el formato |
| `HistorialConsultaServicio` | HU-J07 | Consultar el historial |
| `UsuarioAdminServicio` | HU-J08 | Crear, editar y desactivar usuarios |

## Reglas de cálculo (clases sencillas, sin base de datos)

Reciben números y devuelven un resultado. Están separadas para que sean fáciles de probar a mano.

| Clase | Responsable | Historia | Qué hace |
|---|---|---|---|
| `ReglaBloqueo` | Edgar | HU-E07 | Recibe total de actividades, actividades pendientes y entregables no aprobados. Dice si la etapa está completa o da el motivo exacto, con singular y plural correctos. |
| `CalculadoraAvance` | Edgar | HU-E09 | Porcentaje de actividades terminadas, con 2 decimales. 0 si no hay terminadas. |
| `ValidadorDatoEstudio` | José Pablo | HU-J01 | Campos obligatorios, montos negativos en costo e inversión, monto obligatorio según categoría. |
| `ReglaRecomendacion` | José Pablo | HU-J04 | Promedio de los 6 puntajes y resultado: 70 o más Viable, de 40 a 69,99 Viable con ajustes, menos de 40 No viable. |

## Exportación (José Pablo, HU-J06)

| Clase | Qué hace |
|---|---|
| `ExportacionServicio` | Recibe el formato elegido y llama al exportador correcto |
| `ExportadorPdf` | Genera el PDF con la librería OpenPDF |
| `ExportadorCsv` | Genera un archivo CSV que Excel abre directamente |

El archivo se llama `Resumen_<proyecto>_<aaaammdd>` con su extensión, y trae los mismos datos de la pantalla resumen más la fecha, la hora y el usuario que lo generó. Se usa CSV en vez de `.xlsx` porque un Excel real necesita varias librerías más; es una decisión por confirmar con el profesor.

## Reglas

- Todo método público empieza validando los datos y el rol (`Validaciones.requiereRol(...)`). Aunque la ventana oculte un botón, el servicio vuelve a comprobar el permiso y lanza `PermisoException`.
- **Toda escritura va en una transacción**, con los pasos de la regla 3 del README de la raíz, y con su `HistorialServicio.registrar(...)` dentro de la misma transacción.
- Antes de modificar algo de un proyecto, llamar a `Reglas.exigirProyectoEditable(c, idProyecto)`.
- Los errores se lanzan como `ValidacionException`, `BloqueoException`, `PermisoException` o `ErrorDatosException`, con mensajes en palabras simples.
- Un servicio nunca usa clases de ventanas (`javax.swing`). Si necesita mostrar algo, lo devuelve y la ventana lo muestra.
- Los métodos que usa el otro integrante están en la sección 7 del plan. No cambien su firma sin avisar.
- Las consultas de avance y calendario se hacen con una sola consulta para todo el proyecto, no una por etapa, para cumplir los 3 segundos por pantalla.
