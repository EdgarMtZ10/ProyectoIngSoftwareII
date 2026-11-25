# ui

Las ventanas del programa, hechas con **Swing** (viene dentro de Java, no hay que instalar nada). Cada pantalla es una clase.

## Clase de arranque

| Clase | Responsable | Qué hace |
|---|---|---|
| `Main` | Edgar (HU-E01) | Tiene el método `main`: abre la ventana de inicio de sesión. Es la clase que se ejecuta con el botón **Run**. |

## Pantallas de Edgar

| Clase | Historia | Pantalla |
|---|---|---|
| `VentanaLogin` | HU-E01 | Inicio de sesión |
| `VentanaPrincipal` | HU-E01 | Ventana con pestañas; muestra solo las opciones del rol |
| `PanelNuevoProyecto` | HU-E02 | Registrar idea y crear proyecto |
| `PanelProyectos` | HU-E03 | Lista, búsqueda, apertura y cancelación de proyectos |
| `PanelEtapas` | HU-E04 | Etapas por criterio |
| `PanelActividades` | HU-E05 | Actividades y entregables |
| `PanelCalendario` | HU-E06 | Calendario y cambio de fechas |
| `PanelAvance` | HU-E08 y E09 | Avance, atrasos y botón "Terminar actividad" |

## Pantallas de José Pablo

| Clase | Historia | Pantalla |
|---|---|---|
| `PanelDatosEstudio` | HU-J01 | Datos del estudio |
| `PanelRevision` | HU-J02 | Revisión de entregables (Aprobador) |
| `PanelEvaluacion` | HU-J03 y J04 | Puntajes y recomendación |
| `PanelResumen` | HU-J05 y J06 | Resumen, finalizar y exportar |
| `PanelHistorial` | HU-J07 | Consulta del historial |
| `PanelUsuarios` | HU-J08 | Administración de usuarios (Administrador) |

## Qué componentes de Swing usar

| Necesidad | Componente |
|---|---|
| Ventana | `JFrame` |
| Pantalla dentro de la ventana | `JPanel` dentro de un `JTabbedPane` |
| Textos y campos | `JLabel` y `JTextField` (contraseña: `JPasswordField`) |
| Listas desplegables | `JComboBox` |
| Tablas | `JTable` con `DefaultTableModel` |
| Mensajes, errores y confirmaciones | `JOptionPane` |
| Fechas | `JTextField` con formato `dd/MM/aaaa`; la ventana lo convierte a `LocalDate` y, si el formato está mal, muestra "Escriba la fecha como dd/mm/aaaa" |
| Calendario | `JPanel` con `GridLayout` de 7 columnas (lunes a domingo), una etiqueta por cada etapa o actividad de cada día |

## Reglas

- Una ventana solo hace tres cosas: lee los campos, llama a un servicio y muestra el resultado o el mensaje de error. **Sin SQL y sin reglas de negocio.**
- Los errores se atrapan con `catch (ValidacionException | BloqueoException | PermisoException e)` y se muestran con `JOptionPane` usando `e.getMessage()`.
- Los campos obligatorios llevan asterisco (*) en su etiqueta.
- Antes de cancelar, desactivar o finalizar algo se pide confirmación con `JOptionPane.showConfirmDialog`.
- Los estados (atrasada, terminada, bloqueada) se muestran con **texto además de color**, por ejemplo "[ATRASADA]".
- Los botones que no corresponden al rol o al estado del proyecto se ocultan o se deshabilitan, pero el permiso real lo comprueba el servicio.
- Cada pantalla debe abrir en menos de 3 segundos y el calendario en menos de 5.
- Cada archivo de pantalla lo edita solo su responsable, para que no haya conflictos en Git.
