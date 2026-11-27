# utilidades

Clases de apoyo que usan todas las capas. **Responsable: Edgar (HU-E00).**

| Clase | Qué hace |
|---|---|
| `ConexionBD` | Lee `config.properties` (que está en la raíz del proyecto) y entrega una conexión nueva a MySQL cada vez que se llama a `getConexion()`. Quien la pide la cierra al terminar. |
| `Validaciones` | `requiereRol`, `noVacio`, `maxLargo`, `rangoFechas`, `formatoCorreo` y `contrasenaSegura` |
| `Contrasenas` | `hash(clave)` y `verificar(clave, hash)` con jBCrypt |
| `Sesion` | Guarda el usuario y el proyecto con los que se está trabajando: `iniciar`, `getUsuario`, `getProyectoActual`, `setProyectoActual` y `cerrar` |
| `ProbarConexion` | Clase con `main` para comprobar que Java llega a MySQL. Muestra "Conexión correcta" o el error. Sirve para la primera prueba de cada integrante. |

## Reglas

- `config.properties` **nunca** se sube a Git.
- Todas las conexiones salen de `ConexionBD`. Ninguna otra clase abre conexiones por su cuenta.
- `Contrasenas.hash` usa un costo de 10. Las contraseñas jamás se guardan ni se imprimen en texto claro.
- `Validaciones` lanza `ValidacionException` o `PermisoException` con el mensaje listo para mostrar.
- Nada de esta carpeta usa ventanas (`javax.swing`).
