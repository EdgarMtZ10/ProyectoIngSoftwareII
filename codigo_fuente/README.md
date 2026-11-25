# codigo_fuente

Esta carpeta es la raíz del código Java. Para mantener la estructura simple, las clases van directamente en las carpetas de paquete existentes: por ejemplo, `ui/Main.java` declara `package ui;`. Los README de esas carpetas explican su responsabilidad.

Tiene seis paquetes:

| Paquete | Qué contiene |
|---|---|
| `modelo` | Una clase por tabla de la base de datos, los valores fijos (enums) y las clases de datos para pantallas |
| `datos` | Acceso a datos y todo el SQL del sistema |
| `servicio` | Las reglas de negocio, los permisos y las transacciones |
| `ui` | Las ventanas del programa (Swing) |
| `util` | Conexión a la base, sesión, validaciones y contraseñas |
| `excepciones` | Los errores propios del sistema, con mensajes en español |

## Flujo obligatorio

`ui` llama a `servicio`, `servicio` llama a `dao`, y `dao` llama a MySQL. Una capa nunca se salta a la otra: una ventana no ejecuta SQL y un DAO no tiene reglas de negocio.

## Cómo se nombran las cosas

- Clases en `PascalCase` (`EtapaDao`, `BloqueoServicio`), atributos y métodos en `camelCase` (`fechaInicio`, `calcularAvance`).
- Los atributos de las clases de `modelo` tienen el nombre de la columna en camelCase: `fecha_inicio` es `fechaInicio`.
- Cada archivo `.java` empieza con la línea `package <paquete>;` que corresponde a su carpeta. Por ejemplo, las clases dentro de `ui` usan `package ui;`.
