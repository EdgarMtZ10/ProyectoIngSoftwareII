## Qué instalar (una sola vez)

| MySQL Server y MySQL Workbench | 8.0 o superior | Base de datos |


## Primeros pasos

1. En una terminal nueva escribe `java -version` y `javac -version`. Las dos deben mostrar la versión 17 o mayor. Si `javac` no existe, instalaste solo el JRE y falta el JDK.
2. Abre VS Code con `Archivo > Abrir carpeta` y elige la carpeta `sistema-factibilidad`, la que contiene `codigo_fuente` y `lib`. Acepta la recomendación de instalar **Extension Pack for Java** si VS Code la muestra.
3. Descarga los tres archivos `.jar` y colócalos en `lib/` (ver `lib/README.md`).
4. Espera a que VS Code termine de cargar (ícono de carga abajo a la derecha). En el panel **Java Projects** de la barra lateral debe aparecer **Referenced Libraries** con los tres `.jar`. La configuración está en `.vscode/settings.json`.
5. Ejecuta en MySQL Workbench los scripts de `database/` (ver `database/README.md`).
6. Crea en la raíz del proyecto (junto a `codigo_fuente`) un archivo llamado `config.properties` con tus datos, como se muestra más abajo.
7. Abre `codigo_fuente/ui/Main.java` y pulsa **Run** para comprobar que Java compila y ejecuta. Esta clase es solo una prueba inicial; la interfaz del sistema aún está pendiente.

## Archivo de conexión: config.properties

Se crea a mano en la raíz del proyecto. **No se sube a Git**, porque cada uno tiene su propia contraseña de MySQL. Sus cuatro líneas son:

```text
db.url=jdbc:mysql://localhost:3306/sistema_factibilidad?useUnicode=true&characterEncoding=utf8&serverTimezone=America/Costa_Rica&allowPublicKeyRetrieval=true&useSSL=false
db.usuario=root
db.contrasena=ESCRIBA_AQUI_SU_CLAVE_DE_MYSQL
```

## Archivo .gitignore

Se crea a mano en la raíz, con estas dos líneas, para que Git no suba lo que no debe:

```text
config.properties
bin/
```

`bin/` es la carpeta donde VS Code guarda el código ya compilado. Se crea sola.

## Estructura de carpetas

Cada carpeta tiene su propio `README.md` que dice qué debe ir en ella, quién lo hace y con qué historia de usuario se relaciona.

```text
sistema-factibilidad/
├── README.md
├── lib/                              los 3 archivos .jar
├── database/                         scripts SQL
├── docs/
│   ├── plan/                         plan de desarrollo e historias de usuario
│   ├── diagramas/                    DFD, ER, secuencia, base de datos (.drawio)
│   ├── informe/                      informes entregados
│   ├── gantt/                        Gantt de avance
│   ├── pruebas/                      tabla de casos de prueba
│   └── manuales/                     manual de usuario y de instalación
└── codigo_fuente/
    ├── datos/                        acceso a datos y SQL
    ├── modelo/                       modelos y enums
    ├── servicio/                     reglas de negocio
    ├── ui/                           ventanas (Swing)
    ├── util/                         conexión y utilidades
    └── excepciones/                  errores propios
```

## Reglas del equipo

1. **Cada capa hace una sola cosa.** La ventana (`ui`) llama al servicio, el servicio llama al DAO y el DAO ejecuta el SQL. Una ventana nunca escribe SQL y un DAO nunca decide reglas.
2. **Todo el SQL usa `PreparedStatement`** con signos `?`. Nunca se pega texto del usuario dentro del SQL.
3. **Toda escritura en la base va dentro de una transacción**, con estos pasos en el método del servicio: abrir la conexión con `ConexionBD.getConexion()`; `setAutoCommit(false)`; hacer todos los cambios, incluido `HistorialServicio.registrar(...)`; `commit()`; si algo falla, `rollback()` en el `catch`; y cerrar la conexión en el `finally`. Así se cumple "o se guarda todo o no se guarda nada".
4. **Tipos de datos:** montos con `BigDecimal`, fechas con `LocalDate` o `LocalDateTime`. Nunca `double` ni `java.util.Date`.
5. **Mensajes de error en español y en palabras simples.** Los campos obligatorios llevan asterisco (*) en su etiqueta, y antes de cancelar, desactivar o finalizar algo se pide confirmación.
6. **Git:** una rama por historia (`edgar/E04-etapas`, `jose/J02-revision`) y un pull request que revisa el otro. Nunca se sube directo a `main`.
7. **Cada DAO lo escribe una sola persona**, para evitar conflictos al unir el trabajo.
8. **Todo el proyecto es UTF-8.** No cambien la codificación de los archivos, o se dañarán las tildes y la ñ.
