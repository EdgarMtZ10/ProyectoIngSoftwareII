# paquete `excepciones` — Manejo Estructurado de Errores

Define la jerarquía de excepciones personalizadas del sistema. Todas heredan de `RuntimeException` y transportan mensajes redactados en español sencillo para ser mostrados directamente al usuario final sin exponer trazas técnicas ni códigos SQL.

## Clases incluidas (4 clases)

| Clase | Cuándo se lanza | Ejemplo de mensaje al usuario |
| :--- | :--- | :--- |
| `ValidacionException` | Cuando un dato ingresado es inválido, está vacío, excede el largo permitido, tiene formato incorrecto o incumple rangos de fecha/puntaje. | *"La fecha de cierre no puede ser anterior a la de inicio."* |
| `BloqueoException` | Cuando una regla de secuencia o estado impide continuar el flujo (etapa anterior incompleta o proyecto finalizado/cancelado). | *"Faltan 2 entregables por aprobar en la etapa anterior."* |
| `PermisoException` | Cuando no hay sesión activa o el rol del usuario no está autorizado para ejecutar la acción solicitada. | *"Su rol no tiene permiso para realizar esta acción."* |
| `ErrorDatosException` | Envuelve fallos técnicos de JDBC/MySQL o de lectura de configuración (`config.properties`). | *"No se pudo conectar con la base de datos. Verifique que MySQL esté iniciado."* |

## Reglas de manejo de errores

* **Diagnóstico de restricciones SQL:** `ErrorDatosException` incluye el método auxiliar `esViolacionDeRestriccion()` que inspecciona la cadena de causas buscando `SQLIntegrityConstraintViolationException` o códigos `SQLState` de clase `"23"` (como correos duplicados), permitiendo que la capa `servicio` los traduzca en una `ValidacionException` amigable.
* **Preservación de causa:** Siempre que se captura una excepción técnica se conserva mediante `super(mensaje, causa)` para facilitar la depuración sin contaminar el mensaje visible.