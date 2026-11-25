# lib

Aquí van las librerías que el programa necesita. Son tres archivos `.jar`. VS Code las carga desde esta carpeta con la configuración de `.vscode/settings.json`.

## Archivos que deben estar

| Archivo | Para qué sirve | Dónde descargarlo |
|---|---|---|
| `mysql-connector-j-8.4.0.jar` | Conectar Java con MySQL | https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar |
| `jbcrypt-0.4.jar` | Guardar las contraseñas de forma que nadie pueda leerlas | https://repo1.maven.org/maven2/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar |
| `openpdf-1.3.39.jar` | Generar el resumen en PDF (historia J06) | https://repo1.maven.org/maven2/com/github/librepdf/openpdf/1.3.39/openpdf-1.3.39.jar |

## Cómo agregarlos

1. Abre cada enlace en el navegador. La descarga empieza sola.
2. Copia los tres archivos dentro de esta carpeta, sin cambiarles el nombre.
3. En VS Code, abre el panel **Java Projects** (barra lateral). Debe aparecer **Referenced Libraries** con los tres archivos. El proyecto necesita la extensión **Extension Pack for Java** y al menos un archivo `.java`; si no aparece el panel, abre la carpeta raíz `sistema-factibilidad` y espera a que termine la carga de Java.

## Reglas

- Los `.jar` **sí se suben a Git**, para que los dos usen exactamente las mismas versiones. Son pocos y pesan poco.
- No agreguen otra librería sin avisar al otro integrante y anotarla en esta tabla.
- Hay un tercer `.jar` (OpenPDF) porque el documento pide exportar en PDF. Para Excel se usa un archivo CSV, que Excel abre directamente, porque un `.xlsx` real necesitaría una librería con muchos archivos más (ver `codigo_fuente/servicio/README.md`).
