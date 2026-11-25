Acceso a datos: aquí vive **todo** el SQL del sistema.

## de Edgar

`UsuarioDao`, `IdeaNegocioDao`, `ProyectoDao`, `CriterioDao`, `ProyectoCriterioDao`, `EtapaDao`, `ActividadDao`, `EntregableDao`, `HistorialDao`, `CalendarioDao` (consulta del calendario) y `AvanceDao` (consultas de avance y atrasos).

## DAO de José Pablo

`DatoEstudioDao`, `EntregableRevisionDao`, `ProyectoCriterioPuntajeDao`, `RecomendacionDao`, `ResumenDao`, `HistorialConsultaDao` y `UsuarioAdminDao`.

Cada DAO lo escribe **una sola persona**. Si los dos necesitan consultas sobre la misma tabla, cada uno crea su propia clase (por eso existen `EntregableDao` de Edgar y `EntregableRevisionDao` de José).

## Reglas

- Todos los métodos reciben la `Connection` como primer parámetro: `insertar(Connection c, Actividad a)`. El DAO nunca abre, cierra ni confirma la conexión. Eso lo hace el servicio.
- Solo `PreparedStatement` con `?`. Si un filtro es opcional, el `WHERE` se arma con fragmentos fijos que escriben ustedes, y los valores siempre van como parámetros.
- Usar `try-with-resources` para el `PreparedStatement` y el `ResultSet`, para que se cierren solos.
- Al insertar, pedir el id generado con `Statement.RETURN_GENERATED_KEYS` y devolverlo.
- Leer fechas con `rs.getObject("columna", LocalDate.class)` y montos con `rs.getBigDecimal(...)`. Escribir fechas con `Date.valueOf(localDate)`.
- Los enums se guardan con `name()` y se leen con `Enum.valueOf(rs.getString(...))`.
- Los valores vacíos se escriben con `ps.setNull(...)`.
- **Sin reglas de negocio.** Un DAO no decide si algo está permitido, solo lee y escribe.
- Los nombres de tablas y columnas deben ser idénticos a los de `database/factibilidad.sql`.
