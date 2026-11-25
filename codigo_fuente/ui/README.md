---


# Paquete `ui` — Guía Paso a Paso para Completar las Pantallas

Hola. Este documento explica qué pantallas ya están listas en `codigo_fuente/ui/` y cómo crear las 5 pantallas que faltan para completar el sistema.

---

## 1. Lo que ya está hecho y funcionando

No tiene que tocar estos archivos, ya funcionan:

1. `Main.java` — Inicia la aplicación y abre la pantalla de inicio de sesión.
2. `LoginFrame.java` — Pantalla de correo y contraseña. Al entrar, abre `VentanaPrincipal`.
3. `EstilosUI.java` — Contiene los colores, fuentes, botones, tablas y ventanas de mensaje listos para usar.
4. `PanelModulo.java` — Interfaz con un único método `void refrescar()` que todas las pantallas deben implementar.
5. `PanelUsuarios.java` — Pantalla completa para que el `ADMINISTRADOR` cree y edite usuarios.
6. `PanelProyectos.java` — Pantalla completa para que el `ANALISTA` registre ideas, cree proyectos y seleccione cuál proyecto está activo.
7. `VentanaPrincipal.java` — Ventana con el menú lateral izquierdo y la barra superior donde se muestra el usuario y el proyecto activo.

---

## 2. Reglas visuales del proyecto (Importante)

Para que todas las pantallas se vean iguales y sean fáciles de usar, siga esto:

1. Use un único fondo blanco principal (`EstilosUI.aplicarSuperficiePrincipal(panel)`) y separe las secciones con líneas simples (`new JSeparator()`) o títulos con línea inferior (`EstilosUI.crearEncabezadoSeccion("Título")`).
2. **Use los botones de `EstilosUI`:** No use `new JButton(...)` sin estilo; use `EstilosUI.crearBotonPrimario(...)`, `crearBotonSecundario(...)`, `crearBotonExito(...)` o `crearBotonPeligro(...)`.
3. **Texto legible con alto contraste:** Nunca ponga texto gris claro sobre fondos de color. Use `EstilosUI.TEXTO_PRIMARIO` sobre fondo blanco.
4. **Asterisco en campos obligatorios:** Cree las etiquetas con `EstilosUI.crearEtiqueta("Nombre", true)` para que aparezca el asterisco rojo `*`.
5. **Todo en español:** Botones, columnas de tablas y mensajes deben estar en español.

---

## 3. Las 5 pantallas que tiene que crear

Cree cada clase dentro de `codigo_fuente/ui/`. Todas deben extender `JPanel` e implementar `PanelModulo`.

### Pantalla 1: `PanelPlanificacion.java` (Caso de Uso 02)
* **Servicios que usa:** `ProyectoServicio` y `PlanificacionServicio`.
* **Qué debe tener:**
  1. Un `JComboBox` para elegir uno de los 6 criterios del proyecto activo (`proyectoServicio.listarCriteriosDeProyecto(idProyecto)`).
  2. Formulario para **crear etapa**: nombre, fecha inicio (`LocalDate.parse("2026-11-25")`), fecha cierre y botón guardar (`planificacionServicio.crearEtapa(...)`).
  3. Formulario para **crear actividad** dentro de la etapa seleccionada: nombre, responsable, fecha inicio, fecha cierre y botón guardar (`planificacionServicio.crearActividad(...)`).
  4. Formulario para **crear entregable** dentro de la actividad seleccionada: nombre y botón guardar (`planificacionServicio.crearEntregable(...)`).
  5. Una tabla **Calendario Visual** que cargue `planificacionServicio.obtenerItemsCalendario(idProyecto)` (columnas: Tipo, Criterio, Nombre, Inicio, Cierre, Estado) y botones para reprogramar fechas (`reprogramarEtapa` / `reprogramarActividad`).

### Pantalla 2: `PanelDatosEstudio.java` (Caso de Uso 03)
* **Servicios que usa:** `ProyectoServicio`, `PlanificacionServicio` y `DatoEstudioServicio`.
* **Qué debe tener:**
  1. Selectores (`JComboBox`) de Criterio y Etapa del proyecto activo.
  2. Formulario con:
     * Categoría (`JComboBox<CategoriaDato>` con `CategoriaDato.values()`).
     * Descripción (`JTextField`).
     * Monto (`JTextField`, convertido con `new BigDecimal(texto)`).
     * Período (`JTextField`, por ejemplo `"Mensual"` o `"Año 1"`).
     * Botón guardar que llama a `datoEstudioServicio.registrarDato(...)`.
  3. Una `JTable` debajo mostrando los datos guardados (`datoEstudioServicio.listarPorProyecto(idProyecto)`).

### Pantalla 3: `PanelEntregables.java` (Caso de Uso 04)
* **Servicios que usa:** `EntregableServicio`.
* **Qué debe tener:**
  1. Una `JTable` que cargue `entregableServicio.listarEntregables(idProyecto, false)` (si no hay proyecto activo, puedes enviar `null` en `idProyecto` para que el Aprobador vea todos).
  2. Un campo de texto (`JTextArea` o `JTextField`) para escribir el **motivo de rechazo**.
  3. Dos botones: **Aprobar Entregable** y **Rechazar Entregable**, que llaman a `entregableServicio.revisarEntregable(idEntregable, true/false, motivo)`.
  4. **Regla de rol:** Si `Sesion.getUsuario().getRol() != RolUsuario.APROBADOR`, deshabilita los botones de aprobar y rechazar (`btnAprobar.setEnabled(false)`).

### Pantalla 4: `PanelSeguimiento.java` (Casos de Uso 05 y 06)
* **Servicios que usa:** `ProyectoServicio`, `PlanificacionServicio` y `SeguimientoServicio`.
* **Qué debe tener:**
  1. Una etiqueta grande o `JProgressBar` mostrando el avance general del proyecto (`seguimientoServicio.calcularAvanceProyecto(idProyecto).getPorcentajeGeneral()`).
  2. Tabla 1: Avance por etapa (`avance.getEtapas()`).
  3. Tabla 2: Actividades de la etapa seleccionada con un botón **"Marcar Actividad Terminada"** que llama a `seguimientoServicio.marcarActividadTerminada(idActividad)`. (Si falta completar la etapa anterior, el servicio lanza el error y `EstilosUI.mostrarError` muestra el motivo).
  4. Tabla 3: Lista de atrasos (`seguimientoServicio.listarAtrasos(idProyecto)`).

### Pantalla 5: `PanelEvaluacionCierre.java` (Casos de Uso 07 y 08)
* **Servicios que usa:** `ProyectoServicio`, `EvaluacionServicio` y `CierreServicio`.
* **Qué debe tener:**
  1. Tabla con los 6 criterios del proyecto (`proyectoServicio.listarCriteriosDeProyecto(idProyecto)`), un campo para ingresar puntaje (`0` a `100`) y botón **"Guardar Puntaje"** (`evaluacionServicio.evaluarCriterio(...)`).
  2. Botón **"Calcular Recomendación Final"** (`evaluacionServicio.generarRecomendacion(idProyecto)`) y una etiqueta mostrando el promedio y el resultado (`VIABLE`, `VIABLE_CON_AJUSTES` o `NO_VIABLE`).
  3. Botón **"Finalizar Proyecto"** (`cierreServicio.finalizarProyecto(idProyecto)`). Antes de llamarlo, pide confirmación con `EstilosUI.confirmar(this, "¿Seguro que desea finalizar el proyecto? Ya no podrá editarse.")`.
  4. Botones **"Exportar PDF"** y **"Exportar CSV"** usando `JFileChooser` y llamando a `cierreServicio.exportarResumenPdf(...)` y `exportarResumenCsv(...)`.
  5. Tabla de **Historial de Cambios** cargada con `cierreServicio.consultarHistorial(null)`.

---

## 4. Plantilla base para empezar cada panel rápido

Puede copiar esta estructura inicial en cada archivo nuevo que haga:

```java
package ui;

import modelo.Proyecto;
import servicio.SeguimientoServicio;
import util.Sesion;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class PanelSeguimiento extends JPanel implements PanelModulo {

    private final SeguimientoServicio seguimientoServicio = new SeguimientoServicio();
    private final JLabel lblEstado = new JLabel();

    public PanelSeguimiento() {
        setLayout(new BorderLayout());
        setBackground(EstilosUI.FONDO_BASE);
        EstilosUI.fijarMargen(this, 12, 12, 12, 12);

        JPanel superficie = new JPanel(new BorderLayout(0, 12));
        EstilosUI.aplicarSuperficiePrincipal(superficie);

        superficie.add(EstilosUI.crearEncabezadoSeccion("Avance, Atrasos y Bloqueos"), BorderLayout.NORTH);
        superficie.add(lblEstado, BorderLayout.CENTER);

        add(superficie, BorderLayout.CENTER);
    }

    @Override
    public void refrescar() {
        Proyecto proyecto = Sesion.getProyectoActual();
        if (proyecto == null) {
            lblEstado.setText("Primero selecciona un proyecto activo en la pantalla '1. Ideas y Proyectos'.");
            return;
        }
        // Aquí hace las tablas usando proyecto.getIdProyecto()
    }
}