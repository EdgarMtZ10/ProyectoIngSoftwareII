# Paquete `servicio` — Reglas de Negocio del Sistema

Hola. Esta carpeta contiene toda la lógica y las reglas del sistema ya terminadas y probadas.

Cuando construyas una pantalla en `ui`, **no necesitas escribir consultas SQL ni validar reglas complicadas**. Solo creas una instancia del servicio que necesitas, llamas a su método y él se encarga de validar los datos, guardar en MySQL y registrar la acción en el historial.

---

## ¿Qué hace cada archivo de esta carpeta?

### 1. `AutenticacionServicio.java`
Controla la entrada y salida de los usuarios.
* `Usuario iniciarSesion(String correo, String contrasena)`
  Verifica que el correo y la contraseña sean correctos y que la cuenta esté activa. Si todo está bien, guarda al usuario en `Sesion`.
* `void cerrarSesion()`
  Cierra la sesión del usuario actual.

### 2. `UsuarioServicio.java`
Administra las cuentas de usuario (solo lo usa el rol `ADMINISTRADOR`).
* `List<Usuario> listarUsuarios()` — Devuelve todos los usuarios registrados.
* `Usuario crearUsuario(String nombre, String correo, String contrasenaPlana, RolUsuario rol)` — Crea un usuario nuevo con contraseña cifrada.
* `void actualizarUsuario(int idUsuario, String nombre, String correo, RolUsuario rol, boolean activo)` — Edita los datos de un usuario o desactiva su cuenta.
* `void cambiarContrasena(int idUsuario, String nuevaContrasena)` — Cambia la contraseña de un usuario.
* `void inicializarAdministradorSiVacio(String nombre, String correo, String contrasenaPlana)` — Crea un administrador inicial si la base de datos está vacía.

### 3. `ProyectoServicio.java`
Maneja las ideas de negocio y los proyectos (Caso de Uso 01).
* `IdeaNegocio registrarIdea(String titulo, String descripcion)` — Guarda una idea nueva.
* `Proyecto crearProyectoDesdeIdea(int idIdea, String nombreProyecto)` — Crea un proyecto nuevo en estado `EN_PLANIFICACION` y le asigna automáticamente los 6 criterios de factibilidad.
* `List<IdeaNegocio> listarIdeas()` — Devuelve todas las ideas guardadas.
* `List<Proyecto> buscarProyectos(String texto, EstadoProyecto estado)` — Busca proyectos por nombre y/o por estado (puedes enviar `null` si no quieres filtrar).
* `Proyecto seleccionarProyecto(int idProyecto)` — Carga un proyecto y lo deja guardado en `Sesion.getProyectoActual()` como el proyecto activo.
* `List<PuntajeCriterioDetalle> listarCriteriosDeProyecto(int idProyecto)` — Devuelve los 6 criterios de ese proyecto con su ID, nombre, tipo y puntaje.

### 4. `PlanificacionServicio.java`
Crea y reprograma etapas, actividades, entregables y consulta el calendario (Caso de Uso 02).
* `Etapa crearEtapa(int idProyectoCriterio, String nombre, LocalDate fechaInicio, LocalDate fechaCierre)` — Agrega una etapa a uno de los 6 criterios del proyecto.
* `Actividad crearActividad(int idEtapa, String nombre, String responsable, LocalDate fechaInicio, LocalDate fechaCierre)` — Agrega una actividad dentro de una etapa (valida que las fechas estén dentro de la etapa).
* `Entregable crearEntregable(int idActividad, String nombre)` — Agrega un entregable pendiente a una actividad.
* `void reprogramarEtapa(int idEtapa, LocalDate nuevoInicio, LocalDate nuevoCierre)` — Cambia las fechas de una etapa.
* `void reprogramarActividad(int idActividad, LocalDate nuevoInicio, LocalDate nuevoCierre)` — Cambia las fechas de una actividad.
* `List<Etapa> listarEtapasPorCriterio(int idProyectoCriterio)` — Lista las etapas de un criterio.
* `List<Actividad> listarActividadesPorEtapa(int idEtapa)` — Lista las actividades de una etapa.
* `List<Entregable> listarEntregablesPorActividad(int idActividad)` — Lista los entregables de una actividad.
* `List<ItemCalendario> obtenerItemsCalendario(int idProyecto)` — Devuelve todas las etapas y actividades del proyecto ordenadas por fecha para mostrarlas en el calendario.

### 5. `DatoEstudioServicio.java`
Guarda los datos económicos y técnicos de cada etapa (Caso de Uso 03).
* `DatoEstudio registrarDato(int idEtapa, CategoriaDato categoria, String descripcion, BigDecimal monto, String periodo)` — Guarda un dato (`MERCADO`, `COSTO`, `INVERSION`, `FLUJO`, `RIESGO` o `SUPUESTO`). Redondea el monto a 2 decimales y rechaza montos negativos en `COSTO` o `INVERSION`.
* `List<DatoEstudio> listarPorEtapa(int idEtapa)` — Lista los datos de una etapa.
* `List<DatoEstudio> listarPorProyecto(int idProyecto)` — Lista todos los datos del proyecto.

### 6. `EntregableServicio.java`
Permite revisar, aprobar o rechazar entregables (Caso de Uso 04).
* `List<EntregablePendiente> listarEntregables(Integer idProyecto, boolean soloPendientes)` — Lista los entregables junto con el nombre de su actividad, etapa, criterio y proyecto. Si envías `idProyecto = null`, trae los de todos los proyectos.
* `void revisarEntregable(int idEntregable, boolean aprobado, String motivoRechazo)` — Solo puede llamarlo un `APROBADOR`. Si `aprobado` es `false`, exige escribir `motivoRechazo`.

### 7. `SeguimientoServicio.java`
Calcula porcentajes de avance, detecta atrasos y controla bloqueos entre etapas (Casos de Uso 05 y 06).
* `Avance calcularAvanceProyecto(int idProyecto)` — Devuelve el porcentaje general del proyecto (`getPorcentajeGeneral()`) y el detalle por etapa (`getEtapas()`).
* `List<Atraso> listarAtrasos(int idProyecto)` — Devuelve las etapas y actividades cuya fecha de cierre ya pasó y siguen sin completarse.
* `void marcarActividadTerminada(int idActividad)` — Marca una actividad como terminada. Si la etapa anterior tiene actividades pendientes o entregables sin aprobar, lanza `BloqueoException` explicando exactamente qué falta.

### 8. `EvaluacionServicio.java`
Califica los 6 criterios y calcula la recomendación final (Caso de Uso 07).
* `void evaluarCriterio(int idProyectoCriterio, BigDecimal puntaje)` — Guarda un puntaje de `0.00` a `100.00` (solo si todas las etapas de ese criterio están en estado `COMPLETADA`).
* `Recomendacion generarRecomendacion(int idProyecto)` — Calcula el promedio de los 6 criterios y guarda el resultado (`VIABLE` si es >= 70, `VIABLE_CON_AJUSTES` entre 40 y 69.99, `NO_VIABLE` si es < 40).
* `Recomendacion obtenerRecomendacion(int idProyecto)` — Devuelve la recomendación ya calculada, o `null` si aún no existe.

### 9. `CierreServicio.java`
Genera el resumen ejecutivo, finaliza el proyecto, exporta archivos y consulta el historial (Caso de Uso 08).
* `ResumenProyecto obtenerResumenProyecto(int idProyecto)` — Reúne toda la información del proyecto en un solo objeto.
* `void finalizarProyecto(int idProyecto)` — Cambia el proyecto a estado `FINALIZADO` (desde ese momento nadie puede editarlo).
* `List<HistorialDetalle> consultarHistorial(String tablaAfectada)` — Devuelve la bitácora de auditoría (envía `null` para ver todos los registros).
* `File exportarResumenPdf(int idProyecto, File archivoDestino)` — Genera el reporte en PDF.
* `File exportarResumenCsv(int idProyecto, File archivoDestino)` — Genera el reporte en CSV (Excel).

### 10. Archivos internos y de prueba
* `TransaccionHelper.java` — Clase interna usada por los servicios para abrir conexión, hacer `commit()` o `rollback()` y guardar eventos