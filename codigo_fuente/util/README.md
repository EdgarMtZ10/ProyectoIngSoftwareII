# paquete `util` — Infraestructura, Sesión, Validaciones y Seguridad

Provee componentes transversales reutilizados por el resto de paquetes para conexión a base de datos, estado de sesión en memoria, validaciones estándar y cifrado de contraseñas.

## Clases incluidas (5 clases)

| Clase | Responsabilidad | Métodos principales |
| :--- | :--- | :--- |
| `ConexionBD` | Lee una única vez `config.properties` (en UTF-8) desde la raíz del proyecto, carga el driver de MySQL y entrega conexiones JDBC nuevas. | `getConexion()` |
| `Sesion` | Mantiene en memoria el usuario autenticado y el proyecto seleccionado actualmente durante la ejecución del programa. | `iniciar(usuario)`, `getUsuario()`, `getProyectoActual()`, `setProyectoActual(proyecto)`, `cerrar()` |
| `Validaciones` | Centraliza las validaciones de entrada y verificación de permisos por rol lanzando `ValidacionException` o `PermisoException`. | `requiereRol(...)`, `noVacio(...)`, `maxLargo(...)`, `rangoFechas(...)`, `formatoCorreo(...)`, `contrasenaSegura(...)` |
| `Contrasenas` | Gestiona el hashing y verificación segura de contraseñas usando `jBCrypt` con factor de costo `10` (prefijo `$2a$`). | `hash(clave)`, `verificar(clave, hashGuardado)` |
| `ProbarConexion` | Clase ejecutable por consola (`main`) para comprobar rápidamente que `config.properties` y MySQL responden sin errores. | `main(String[] args)` |

## Reglas de uso

* **Credenciales locales:** `config.properties` nunca se sube al repositorio Git; `ConexionBD` lo lee en tiempo de ejecución desde la raíz del proyecto.
* **Punto único de conexión:** Ninguna otra clase del sistema debe llamar a `DriverManager.getConnection(...)`; todas deben solicitar su conexión a `ConexionBD.getConexion()`.
* **Seguridad de claves:** Las contraseñas jamás se almacenan ni registran en texto plano.