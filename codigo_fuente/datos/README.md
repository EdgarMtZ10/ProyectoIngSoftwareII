# paquete `datos` — Persistencia y Consultas SQL (DAOs)

Encapsula el 100 % de las sentencias SQL del sistema mediante JDBC y `PreparedStatement`. Su única responsabilidad es leer y escribir datos en MySQL sin contener reglas de negocio ni control de transacciones.

## Catálogo de DAOs implementados (13 clases)

| Clase DAO | Tabla(s) principal(es) | Operaciones clave |
| :--- | :--- | :--- |
| `UsuarioDao` | `usuario` | `buscarPorCorreo`, `buscarPorId`, `listarTodos`, `insertar`, `actualizar`, `actualizarContrasena`. |
| `IdeaNegocioDao` | `idea_negocio` | `insertar`, `buscarPorId`, `listarTodas`. |
| `ProyectoDao` | `proyecto` | `insertar`, `buscarPorId`, `buscar` (filtros opcionales por nombre y estado), `actualizarEstado`. |
| `CriterioDao` | `criterio` | `listarTodos`, `buscarPorId`. |
| `ProyectoCriterioDao` | `proyecto_criterio`, `criterio` | `insertar`, `buscarPorId`, `listarDetallePorProyecto`, `actualizarPuntaje`. |
| `EtapaDao` | `etapa`, `proyecto_criterio` | `insertar`, `buscarPorId`, `obtenerIdProyectoPorEtapa`, `listarPorProyectoCriterio`, `buscarEtapaAnterior`, `obtenerSiguienteOrden`, `actualizarFechas`, `actualizarEstado`. |
| `ActividadDao` | `actividad` | `insertar`, `buscarPorId`, `listarPorEtapa`, `actualizarFechas`, `marcarTerminada`, `contarTotalesYPendientesPorEtapa`. |
| `EntregableDao` | `entregable`, `actividad`, `etapa` | `insertar`, `buscarPorId`, `obtenerIdProyectoPorEntregable`, `listarPorActividad`, `listarConContexto`, `registrarRevision`, `contarNoAprobadosPorEtapa`. |
| `DatoEstudioDao` | `dato_estudio`, `etapa` | `insertar`, `listarPorEtapa`, `listarPorProyecto`. |
| `RecomendacionDao` | `recomendacion` | `buscarPorProyecto`, `guardarOActualizar`. |
| `HistorialDao` | `historial`, `usuario` | `insertar`, `listar` (solo inserción y consulta; sin `UPDATE` ni `DELETE`). |
| `CalendarioDao` | `etapa`, `actividad`, `criterio` | `listarItemsPorProyecto` (consulta única `UNION ALL` para rendimiento `< 5s`). |
| `AvanceDao` | `etapa`, `actividad`, `criterio` | `listarConteosPorEtapa` (consulta agregada) y `listarAtrasos` (`UNION ALL` por fecha vencida). |

## Reglas técnicas de esta capa

1. **Conexión delegada:** Todos los métodos reciben `Connection c` como primer parámetro. Ningún DAO abre, cierra, hace `commit()` ni `rollback()` por su cuenta; eso lo gobierna exclusivamente la capa `servicio`.
2. **Prevención de inyección SQL:** Todas las consultas utilizan `PreparedStatement` con parámetros `?` y bloques `try-with-resources`.
3. **Llaves autogeneradas:** Los métodos `insertar` utilizan `Statement.RETURN_GENERATED_KEYS`, asignan el ID al modelo y lo devuelven.
4. **Manejo de errores:** Cualquier `SQLException` se captura y se relanza envuelta en `ErrorDatosException` con un mensaje claro en español.