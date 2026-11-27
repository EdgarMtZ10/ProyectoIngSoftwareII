# `database` — Documentación Técnica y Diccionario de Datos (`sistema_factibilidad`)


## Contenido de la carpeta

| Archivo | Descripción |
| :--- | :--- |
| `factibilidad.sql` | Script maestro de creación de la base de datos `sistema_factibilidad`. Incluye la creación de las **11 tablas**, llaves primarias y foráneas, restricciones `CHECK` y `UNIQUE`, **9 índices de optimización**, **2 triggers de inmutabilidad** y la carga inicial del catálogo de los **6 criterios de factibilidad**. |
| `README.md` | Documentación técnica del modelo de datos, diccionario detallado de columnas, reglas de integridad, índices y funcionamiento de triggers y procedimientos. |

---

## Especificaciones Técnicas del Motor
---

## Mapa de Relaciones (Cardinalidad)

```text
usuario (1) ──────────< (N) idea_negocio (1) ──────────< (N) proyecto
   │                                                            │
   ├──────────────────< (N) proyecto (como id_analista)         │ (1:6)
   ├──────────────────< (N) entregable (como id_aprobador)      ▼
   └──────────────────< (N) historial                   proyecto_criterio >── (1) criterio
                                                                │
                                                                │ (1:N)
                                                                ▼
                                                              etapa
                                                             /     \
                                                      (1:N) /       \ (1:N)
                                                           ▼         ▼
                                                    actividad     dato_estudio
                                                       │
                                                       │ (1:N)
                                                       ▼
                                                   entregable

proyecto (1) ─────────── (1) recomendacion
```

---

## Diccionario de Datos Completo (11 Tablas)

### 1. Tabla `usuario`
Almacena las credenciales, el rol operativo y el estado de activación de los usuarios del sistema. Los usuarios nunca se eliminan físicamente para no romper la integridad referencial del historial de auditoría.

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_usuario` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único autogenerado del usuario. |
| `nombre` | `VARCHAR(100)` | NO | — | — | Nombre completo del usuario mostrado en pantallas y reportes. |
| `correo` | `VARCHAR(100)` | NO | `UNIQUE` | — | Correo electrónico único utilizado como identificador de inicio de sesión. |
| `contrasena` | `VARCHAR(255)` | NO | — | — | Contraseña cifrada mediante algoritmo **BCrypt** (`$2a$`, costo 10). Nunca se guarda en texto plano (RNF1). |
| `rol` | `VARCHAR(20)` | NO | `CHECK (chk_usuario_rol)` | — | Perfil de acceso del usuario. Valores permitidos: `ANALISTA`, `APROBADOR`, `ADMINISTRADOR`. |
| `activo` | `TINYINT(1)` | NO | `CHECK (chk_usuario_activo)` | `1` | Bandera de estado de la cuenta (`1` = habilitada para iniciar sesión, `0` = desactivada). |

---

### 2. Tabla `idea_negocio`
Registra las propuestas comerciales iniciales capturadas por un Analista antes de formalizar un estudio de factibilidad.

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_idea` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único de la idea de negocio. |
| `id_usuario` | `INT` | NO | `FK` → `usuario(id_usuario)` | — | Identificador del Analista que registró la propuesta. |
| `titulo` | `VARCHAR(150)` | NO | — | — | Título corto y representativo de la idea de negocio. |
| `descripcion` | `TEXT` | NO | — | — | Explicación detallada del concepto de negocio, problema a resolver y alcance inicial. |
| `fecha_registro` | `DATE` | NO | — | — | Fecha calendario en la que se ingresó la idea al sistema. |

---

### 3. Tabla `proyecto`
Representa un estudio de factibilidad formal asociado a una idea de negocio y asignado a un Analista responsable.

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_proyecto` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único del proyecto de factibilidad. |
| `id_idea` | `INT` | NO | `FK` → `idea_negocio(id_idea)` | — | Idea de negocio a partir de la cual se creó el estudio. |
| `id_analista` | `INT` | NO | `FK` → `usuario(id_usuario)` | — | Usuario con rol `ANALISTA` encargado de formular y conducir el estudio. |
| `nombre` | `VARCHAR(150)` | NO | — | — | Nombre oficial del proyecto de estudio de factibilidad. |
| `fecha_creacion` | `DATE` | NO | — | — | Fecha en que se generó el proyecto. |
| `estado` | `VARCHAR(20)` | NO | `CHECK (chk_proyecto_estado)` | `'EN_PLANIFICACION'` | Estado operativo del proyecto: `EN_PLANIFICACION`, `EN_EJECUCION`, `FINALIZADO` (bloqueado a solo lectura) o `CANCELADO`. |

---

### 4. Tabla `criterio`
Catálogo maestro e invariable de las **6 dimensiones estándar** de evaluación de factibilidad. Todas poseen exactamente el mismo peso (1/6) en el cálculo final.

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_criterio` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único del criterio en el catálogo. |
| `tipo` | `VARCHAR(20)` | NO | `UNIQUE`, `CHECK (chk_criterio_tipo)` | — | Clave técnica fija del criterio: `MERCADO`, `TECNICO`, `OPERATIVO`, `LEGAL`, `FINANCIERO` o `AMBIENTAL`. |
| `nombre` | `VARCHAR(50)` | NO | — | — | Nombre descriptivo en español mostrado al usuario (ej. *Factibilidad de Mercado*). |

---

### 5. Tabla `proyecto_criterio`
Tabla asociativa (`N:M`) que vincula automáticamente cada proyecto creado con los 6 criterios del catálogo y almacena la calificación obtenida en cada dimensión.

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_proyecto_criterio` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único de la instancia de un criterio dentro de un proyecto específico. |
| `id_proyecto` | `INT` | NO | `FK` → `proyecto(id_proyecto)`, `UNIQUE` (`uq_proyecto_criterio`) | — | Proyecto al que pertenece la evaluación del criterio. |
| `id_criterio` | `INT` | NO | `FK` → `criterio(id_criterio)`, `UNIQUE` (`uq_proyecto_criterio`) | — | Referencia al criterio del catálogo. La restricción compuesta impide repetir un criterio en el mismo proyecto. |
| `puntaje` | `DECIMAL(5,2)` | SÍ | `CHECK (chk_pc_puntaje)` | `NULL` | Nota asignada al criterio entre `0.00` y `100.00` una vez completadas todas sus etapas. Permanece `NULL` antes de ser evaluado. |

---

### 6. Tabla `etapa`
Define las fases secuenciales planificadas dentro de cada criterio de un proyecto. El campo `orden` gobierna los bloqueos secuenciales (CU-06).

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_etapa` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único de la etapa. |
| `id_proyecto_criterio` | `INT` | NO | `FK` → `proyecto_criterio(id_proyecto_criterio)` | — | Criterio del proyecto al que pertenece esta etapa. |
| `nombre` | `VARCHAR(150)` | NO | — | — | Nombre descriptivo de la etapa (ej. *Encuestas de demanda*). |
| `orden` | `INT` | NO | `CHECK (chk_etapa_orden)` | — | Número correlativo (`>= 1`) que establece la secuencia obligatoria dentro del criterio. |
| `fecha_inicio` | `DATE` | NO | — | — | Fecha programada de inicio de la etapa. |
| `fecha_cierre` | `DATE` | NO | `CHECK (chk_etapa_fechas)` | — | Fecha programada de finalización (`fecha_cierre >= fecha_inicio`). |
| `estado` | `VARCHAR(20)` | NO | `CHECK (chk_etapa_estado)` | `'PENDIENTE'` | Estado actual de la etapa: `PENDIENTE`, `EN_PROGRESO`, `COMPLETADA` o `BLOQUEADA`. |

---

### 7. Tabla `actividad`
Almacena las tareas operativas concretas que componen una etapa, su responsable y su estado de ejecución.

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_actividad` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único de la actividad. |
| `id_etapa` | `INT` | NO | `FK` → `etapa(id_etapa)` | — | Etapa padre dentro de la cual se planifica la actividad. |
| `nombre` | `VARCHAR(150)` | NO | — | — | Nombre o descripción corta de la tarea a realizar. |
| `responsable` | `VARCHAR(100)` | NO | — | — | Nombre de la persona asignada para ejecutar la actividad. |
| `fecha_inicio` | `DATE` | NO | — | — | Fecha de inicio de la actividad (debe estar dentro del rango de su etapa). |
| `fecha_cierre` | `DATE` | NO | `CHECK (chk_actividad_fechas)` | — | Fecha límite de cumplimiento (`fecha_cierre >= fecha_inicio` y `<= etapa.fecha_cierre`). |
| `terminada` | `TINYINT(1)` | NO | `CHECK (chk_actividad_terminada)` | `0` | Indicador de finalización (`0` = pendiente, `1` = terminada). Sirve de base para calcular el porcentaje de avance (CU-05). |

---

### 8. Tabla `entregable`
Registra los productos o evidencias vinculados a cada actividad que requieren revisión formal por parte de un usuario con rol `APROBADOR` (CU-04).

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_entregable` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único del entregable. |
| `id_actividad` | `INT` | NO | `FK` → `actividad(id_actividad)` | — | Actividad a la que está asociado el entregable. |
| `id_aprobador` | `INT` | SÍ | `FK` → `usuario(id_usuario)` | `NULL` | Usuario con rol `APROBADOR` que emitió el dictamen de revisión (`NULL` mientras está `PENDIENTE`). |
| `nombre` | `VARCHAR(150)` | NO | — | — | Nombre del documento, informe o evidencia esperada. |
| `estado` | `VARCHAR(20)` | NO | `CHECK (chk_entregable_estado)` | `'PENDIENTE'` | Estado de control de calidad: `PENDIENTE`, `APROBADO` o `RECHAZADO`. |
| `motivo_rechazo` | `TEXT` | SÍ | — | `NULL` | Justificación obligatoria registrada por el Aprobador cuando `estado = 'RECHAZADO'`. |
| `fecha_revision` | `DATETIME` | SÍ | — | `NULL` | Marca exacta de fecha y hora en la que el Aprobador aprobó o rechazó el entregable. |

---

### 9. Tabla `dato_estudio`
Almacena la información estructurada levantada durante cada etapa del estudio (hallazgos de mercado, costos, inversiones, flujos, riesgos y supuestos).

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_dato` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único del dato del estudio. |
| `id_etapa` | `INT` | NO | `FK` → `etapa(id_etapa)` | — | Etapa en la que se recopiló el dato. |
| `categoria` | `VARCHAR(20)` | NO | `CHECK (chk_dato_categoria)` | — | Clasificación del registro: `MERCADO`, `COSTO`, `INVERSION`, `FLUJO`, `RIESGO` o `SUPUESTO`. |
| `descripcion` | `VARCHAR(255)` | NO | — | — | Descripción detallada del hallazgo, partida financiera, riesgo o supuesto. |
| `monto` | `DECIMAL(12,2)` | SÍ | — | `NULL` | Valor monetario o numérico con **2 decimales exactos** (RNF3). No permite valores negativos en `COSTO` ni `INVERSION`. |
| `periodo` | `VARCHAR(20)` | SÍ | — | `NULL` | Referencia temporal del dato (ej. `Mensual`, `Anual`, `Año 1`, `Trimestre 2`). |

---

### 10. Tabla `recomendacion`
Guarda el dictamen final de viabilidad calculado automáticamente a partir del promedio aritmético de los 6 criterios del proyecto (CU-07).

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_recomendacion` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador único de la recomendación generada. |
| `id_proyecto` | `INT` | NO | `FK` → `proyecto(id_proyecto)`, `UNIQUE` | — | Proyecto evaluado. La restricción `UNIQUE` garantiza una única recomendación vigente por proyecto (`1:1`). |
| `promedio` | `DECIMAL(5,2)` | NO | — | — | Promedio exacto (a 2 decimales) de los 6 puntajes registrados en `proyecto_criterio`. |
| `resultado` | `VARCHAR(25)` | NO | `CHECK (chk_recomendacion_resultado)` | — | Dictamen calculado por regla fija: `VIABLE` (`>= 70.00`), `VIABLE_CON_AJUSTES` (`40.00`–`69.99`) o `NO_VIABLE` (`< 40.00`). |
| `fecha_generacion` | `DATETIME` | NO | — | — | Fecha y hora exacta en que se generó o recalculó la recomendación. |

---

### 11. Tabla `historial`
Bitácora central e **inmutable** de auditoría del sistema. Registra quién realizó cada operación, sobre qué tabla y registro ocurrió, qué tipo de acción fue y en qué fecha/hora exacta (RNF7).

| Columna | Tipo de dato | Nulo | Clave / Restricción | Por defecto | Descripción funcional |
| :--- | :--- | :-: | :--- | :--- | :--- |
| `id_historial` | `INT` | NO | `PK`, `AUTO_INCREMENT` | — | Identificador correlativo único del asiento de auditoría. |
| `id_usuario` | `INT` | NO | `FK` → `usuario(id_usuario)` | — | Usuario autenticado que ejecutó o disparó el evento. |
| `tabla_afectada` | `VARCHAR(50)` | NO | — | — | Nombre de la entidad afectada (ej. `usuario`, `idea_negocio`, `proyecto`, `etapa`, `actividad`, `entregable`, `dato_estudio`, `proyecto_criterio`, `recomendacion`). |
| `id_registro` | `INT` | NO | — | — | Valor de la llave primaria del registro afectado en `tabla_afectada`. |
| `accion` | `VARCHAR(20)` | NO | `CHECK (chk_historial_accion)` | — | Tipo de evento auditado: `CREAR`, `MODIFICAR`, `APROBAR`, `RECHAZAR`, `FINALIZAR`, `DESACTIVAR` o `BLOQUEAR`. |
| `fecha_hora` | `DATETIME` | NO | — | `CURRENT_TIMESTAMP` | Fecha y hora exacta en que se registró la acción. |

---

## Índices de Rendimiento

Para cumplir los tiempos máximos de respuesta establecidos en los requerimientos no funcionales (**RNF4**: pantallas en `< 3 s` y calendario en `< 5 s`; **RNF5**: operación fluida con hasta 10 usuarios concurrentes), el esquema incluye **9 índices explícitos**:

| Nombre del Índice | Tabla y Columnas | Consulta que acelera |
| :--- | :--- | :--- |
| `idx_proyecto_analista` | `proyecto(id_analista)` | Filtrado de proyectos asignados a un Analista. |
| `idx_proyecto_estado` | `proyecto(estado)` | Búsqueda de proyectos por estado (`EN_PLANIFICACION`, `FINALIZADO`, etc.). |
| `idx_etapa_pc_orden` | `etapa(id_proyecto_criterio, orden)` | Carga secuencial de etapas por criterio y verificación de etapa anterior (`orden < actual`). |
| `idx_actividad_etapa` | `actividad(id_etapa)` | Cálculo de avance por etapa, carga de calendario y conteo de actividades pendientes. |
| `idx_entregable_actividad` | `entregable(id_actividad)` | Consulta de entregables por actividad y verificación de bloqueos por etapa. |
| `idx_entregable_estado` | `entregable(estado)` | Bandeja del Aprobador filtrada por entregables en estado `PENDIENTE`. |
| `idx_dato_etapa` | `dato_estudio(id_etapa)` | Listado de datos del estudio por etapa y consolidación del resumen ejecutivo. |
| `idx_historial_tabla_reg` | `historial(tabla_afectada, id_registro)` | Trazabilidad específica por entidad y registro. |
| `idx_historial_fecha` | `historial(fecha_hora)` | Ordenamiento cronológico descendente de la bitácora de auditoría. |

---

## Funcionalidad de los Triggers (Disparadores)

El esquema implementa **2 triggers de seguridad e integridad** sobre la tabla `historial` para garantizar el cumplimiento estricto del requerimiento **RNF7** (*"El historial de cambios no debe poder modificarse ni borrarse"*), protegiendo la bitácora incluso si alguien intenta alterarla mediante una herramienta externa como MySQL Workbench o consola SQL.

### 1. `trg_historial_no_update`
* **Tabla objetivo:** `historial`
* **Momento y evento:** `BEFORE UPDATE` (se dispara antes de ejecutar cualquier sentencia `UPDATE` sobre cualquier fila de `historial`).
* **Comportamiento:** Interrumpe inmediatamente la operación lanzando una excepción controlada del motor mediante `SIGNAL SQLSTATE '45000'` con el mensaje:
  > `'El historial de cambios es inmutable y no puede modificarse.'`
* **Efecto práctico:** Ningún registro de auditoría puede ser alterado retrospectivamente (ni usuario, ni acción, ni fecha/hora, ni ID de registro).

### 2. `trg_historial_no_delete`
* **Tabla objetivo:** `historial`
* **Momento y evento:** `BEFORE DELETE` (se dispara antes de ejecutar cualquier sentencia `DELETE` sobre cualquier fila de `historial`).
* **Comportamiento:** Cancela la eliminación mediante `SIGNAL SQLSTATE '45000'` con el mensaje:
  > `'El historial de cambios es inmutable y no puede eliminarse.'`
* **Efecto práctico:** Ningún asiento de auditoría puede ser borrado para ocultar cambios, bloqueos, rechazos o cierres de proyecto.

> **Nota sobre auditoría de eventos:** La inserción en `historial` (`INSERT INTO historial ...`) se ejecuta desde la capa `servicio` de Java dentro de la **misma transacción JDBC** que realiza cada operación de negocio (`CREAR`, `MODIFICAR`, `APROBAR`, `RECHAZAR`, `FINALIZAR`, `DESACTIVAR` o `BLOQUEAR`). De este modo, si ocurre un fallo al guardar el evento en `historial`, se revierte toda la operación (`ROLLBACK`), cumpliendo **RNF2** (*atomicidad: se guarda todo o no se guarda nada*).

---

## Procedimientos Almacenados (Stored Procedures — SP) y Lógica Transaccional

En la arquitectura actual de `sistema_factibilidad`, **no se definen Stored Procedures (`CREATE PROCEDURE`) dentro de `factibilidad.sql`**. Esta decisión responde al diseño multicapa de la aplicación Java:

1. **Control transaccional explícito en Java (`RNF2`):** La capa `codigo_fuente/servicio/` abre la conexión, desactiva `autoCommit` (`c.setAutoCommit(false)`), coordina las llamadas a múltiples DAOs en `codigo_fuente/datos/` y ejecuta `c.commit()` o `c.rollback()` de forma atómica.
2. **Mantenibilidad y control de versiones:** Todas las reglas de negocio (validación de fechas, cálculo de porcentajes de avance, detección de atrasos, verificación de bloqueos secuenciales, promedio de criterios y reglas de recomendación) residen en clases Java tipadas y probables sin duplicar lógica entre SQL y Java.
3. **Consultas agregadas optimizadas:** Operaciones de lectura compleja —como el calendario unificado (`CalendarioDao`) o el cálculo de avance y atrasos (`AvanceDao`)— se resuelven mediante consultas SQL parametrizadas (`UNION ALL`, `LEFT JOIN`, `GROUP BY`) apoyadas por los índices del esquema.

### Equivalencia entre Operaciones Críticas y la Capa `servicio` (Java)

Para referencia de trazabilidad y exposición técnica, la siguiente tabla resume cómo se resuelven en Java los flujos que en otros arquitecturas suelen encapsularse en Stored Procedures:

| Flujo de Negocio | Clase / Método en `servicio` | Tablas involucradas en la transacción | Acción en `historial` |
| :--- | :--- | :--- | :--- |
| **Crear proyecto con 6 criterios** (CU-01) | `ProyectoServicio.crearProyectoDesdeIdea(...)` | `proyecto`, `criterio`, `proyecto_criterio`, `historial` | `CREAR` (`proyecto`) |
| **Reprogramar etapa / actividad** (CU-02) | `PlanificacionServicio.reprogramarEtapa(...)` / `reprogramarActividad(...)` | `etapa`, `actividad`, `historial` | `MODIFICAR` (`etapa` / `actividad`) |
| **Aprobar o rechazar entregable** (CU-04) | `EntregableServicio.revisarEntregable(...)` | `entregable`, `etapa`, `historial` | `APROBAR` / `RECHAZAR` (`entregable`) |
| **Marcar actividad terminada y validar bloqueos** (CU-06) | `SeguimientoServicio.marcarActividadTerminada(...)` | `actividad`, `etapa`, `entregable`, `historial` | `MODIFICAR` o `BLOQUEAR` (`actividad`) |
| **Evaluar criterio y generar recomendación** (CU-07) | `EvaluacionServicio.evaluarCriterio(...)` / `generarRecomendacion(...)` | `proyecto_criterio`, `etapa`, `recomendacion`, `historial` | `MODIFICAR` / `CREAR` (`recomendacion`) |
| **Finalizar proyecto (solo lectura)** (CU-08) | `CierreServicio.finalizarProyecto(...)` | `recomendacion`, `proyecto`, `historial` | `FINALIZAR` (`proyecto`) |