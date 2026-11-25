# database

Scripts de la base de datos `sistema_factibilidad` (MySQL 8).

## Qué debe ir aquí

| Archivo | Qué es |
|---|---|
| `factibilidad.sql` | El script completo que entregó Gemini: crea la base, las 11 tablas, los índices, las restricciones, los triggers del historial y los datos de prueba. |
| `01_ajuste_contrasenas.sql` | Cambia las contraseñas de los 3 usuarios de prueba por claves conocidas. |
| `02_ajuste_historial_bloquear.sql` | Agrega la acción `BLOQUEAR` al historial (la pide el caso de uso 6). |
| `03_apoyo_recomendacion.sql` | Solo para pruebas: pone puntaje a los 4 criterios que faltan para probar la recomendación. |

El texto de los tres scripts de ajuste está en la **sección 5 del plan** (`docs/plan/PLAN_DESARROLLO.md`). Cópienlo en archivos con esos nombres.

## Orden de ejecución (en MySQL Workbench)

1. `factibilidad.sql`
2. `01_ajuste_contrasenas.sql`
3. `02_ajuste_historial_bloquear.sql`
4. `03_apoyo_recomendacion.sql` (opcional, solo cuando quieran probar la recomendación)

Cada integrante lo ejecuta en su propio MySQL. Para volver a empezar de cero basta con ejecutar `factibilidad.sql` otra vez, porque borra y recrea las tablas.

## Usuarios de prueba (después del ajuste 01)

| Correo | Rol | Contraseña |
|---|---|---|
| admin@factibilidad.edu | ADMINISTRADOR | Admin1234 |
| analista@factibilidad.edu | ANALISTA | Analista1234 |
| aprobador@factibilidad.edu | APROBADOR | Aprobador1234 |

## Reglas

- Si alguien cambia la estructura de la base, lo cambia en `factibilidad.sql` y avisa al otro. Si no, las bases de los dos dejarán de coincidir.
- Los scripts nuevos se numeran en orden (`04_...`, `05_...`).
- Las contraseñas guardadas deben empezar con `$2a$`. La librería jBCrypt no lee las que empiezan con `$2b$`.
- Los usuarios nunca se borran, solo se desactivan (`activo = 0`), porque el historial apunta a ellos.
- La acción `BLOQUEAR` del ajuste 02 también debe existir en el enum `AccionHistorial` de Java.
