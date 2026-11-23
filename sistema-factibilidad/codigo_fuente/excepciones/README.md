# excepciones

Los errores propios del sistema. **Responsable: Edgar (HU-E00).** Todos extienden `RuntimeException` y llevan un mensaje en palabras simples que la ventana muestra tal cual.

| Clase | Cuándo se lanza | Ejemplo de mensaje |
|---|---|---|
| `ValidacionException` | Datos mal ingresados | "La fecha de cierre no puede ser anterior a la de inicio" |
| `BloqueoException` | La etapa anterior está incompleta | "Faltan 2 entregables por aprobar en la etapa anterior" |
| `PermisoException` | El rol no puede hacer la acción | "Su rol no tiene permiso para realizar esta acción" |
| `ErrorDatosException` | Falla de la base de datos | "No se pudo guardar. Intente de nuevo." |

## Reglas

- Mensajes en español y sin códigos técnicos. El detalle técnico va en la causa (`super(mensaje, causa)`), no en el mensaje.
- Un error de SQL nunca llega a la ventana: el servicio lo atrapa y lo convierte en una de estas excepciones. Por ejemplo, un correo repetido (`SQLIntegrityConstraintViolationException`) se convierte en `ValidacionException("Ese correo ya está registrado")`.
- `ErrorDatosException` tiene el método `esViolacionDeRestriccion()` para reconocer ese caso.
