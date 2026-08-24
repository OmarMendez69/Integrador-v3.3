package utng.gtid2.jome;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utng.gtid2.dao.DashboardDAO;
import utng.gtid2.dao.DesechoDAO;
import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.dao.PrestamoDAO;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Desecho;
import utng.gtid2.modelo.Material;
import utng.gtid2.modelo.Prestamo;
import utng.gtid2.modelo.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador de la pantalla de Reportes del sistema de inventario.
 * <p>
 * Esta pantalla cumple dos funciones principales:
 * <ol>
 *   <li><b>Vista previa en la interfaz:</b> al inicializarse, consulta los
 *       datos actuales (resumen general, catálogo, préstamos, desecho y
 *       usuarios) y los muestra en tablas construidas dinámicamente dentro
 *       de los contenedores {@link VBox} definidos en el FXML.</li>
 *   <li><b>Exportación a PDF:</b> por cada sección existe un método
 *       {@code descargarXxx()} que genera un archivo PDF independiente
 *       usando la librería iText, y un método adicional
 *       ({@link #descargarReporte()}) que genera un único PDF con todas
 *       las secciones combinadas ("Reporte General").</li>
 * </ol>
 * <p>
 * Para evitar duplicar código entre los reportes en pantalla y los PDF,
 * la clase se apoya en un conjunto de métodos auxiliares privados:
 * <ul>
 *   <li>{@link #headerFila(String[], double[])} y
 *       {@link #dataFila(String[], double[], int, String)} construyen las
 *       filas de las tablas mostradas en la interfaz (JavaFX).</li>
 *   <li>{@link #encabezado(Document, String)}, {@link #titulo(Document, String)},
 *       {@link #hCell(PdfPTable, String)} y {@link #dCell(PdfPTable, String, boolean)}
 *       construyen los elementos visuales de los documentos PDF (iText).</li>
 *   <li>{@link #pedirRuta(String)} abre el diálogo para elegir dónde
 *       guardar el PDF, y {@link #ok(String)} / {@link #err(Exception)}
 *       muestran el resultado de la operación al usuario.</li>
 * </ul>
 */
public class ReportesController {

    @FXML private Button btnVolver;
    @FXML private VBox vboxResumen;
    @FXML private VBox vboxCatalogo;
    @FXML private VBox vboxAsignaciones;
    @FXML private VBox vboxDesecho;
    @FXML private VBox vboxUsuarios;

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final MaterialDAO  materialDAO  = new MaterialDAO();
    private final PrestamoDAO  prestamoDAO  = new PrestamoDAO();
    private final DesechoDAO   desechoDAO   = new DesechoDAO();
    private final UsuarioDAO   usuarioDAO   = new UsuarioDAO();

    private List<Material> materiales;
    private List<Prestamo> prestamos;
    private List<Desecho>  desechos;
    private List<Usuario>  usuarios;
    private int    totalMateriales;
    private int    criticos;
    private int    tecnicos;
    private double pesoMes;

    private static final BaseColor C_HEADER_BG  = new BaseColor(243, 244, 246);
    private static final BaseColor C_HEADER_TEXT = new BaseColor(107, 114, 128);
    private static final BaseColor C_TEXT        = new BaseColor(55,  65,  81);
    private static final BaseColor C_ROJO        = new BaseColor(239, 68,  68);
    private static final BaseColor C_MORADO      = new BaseColor(183, 33, 255);
    private static final BaseColor C_GRIS        = new BaseColor(156, 163, 175);

    /**
     * Punto de entrada de JavaFX: se invoca automáticamente al cargar el
     * FXML y dispara la carga de todos los datos necesarios para la
     * vista previa de los reportes.
     */
    @FXML
    public void initialize() {
        cargarDatos();
    }

    /**
     * Consulta en la base de datos los indicadores del dashboard
     * (total de materiales, críticos, técnicos activos y peso de
     * desecho del mes) y las listas completas de materiales, préstamos,
     * desechos y usuarios. Con esos datos, puebla las cinco secciones
     * de la vista previa ({@link #poblarResumen()},
     * {@link #poblarCatalogo()}, {@link #poblarAsignaciones()},
     * {@link #poblarDesecho()} y {@link #poblarUsuarios()}).
     * <p>
     * Si ocurre un error de base de datos, se registra en consola con
     * {@code printStackTrace()} y las secciones quedan vacías.
     */
    private void cargarDatos() {
        try {
            totalMateriales = dashboardDAO.contarMateriales();
            criticos        = dashboardDAO.contarCriticos();
            tecnicos        = dashboardDAO.contarTecnicos();
            pesoMes         = dashboardDAO.pesoDesechoMes();
            materiales      = materialDAO.listarTodos();
            prestamos       = prestamoDAO.listarTodos();
            desechos        = desechoDAO.listarTodos();
            usuarios        = usuarioDAO.listarTodos();

            poblarResumen();
            poblarCatalogo();
            poblarAsignaciones();
            poblarDesecho();
            poblarUsuarios();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Poblar FXML ───────────────────────────────────────────────────────────

    /**
     * Llena {@code vboxResumen} con las filas clave-valor del resumen
     * general (insumos registrados, críticos, técnicos activos y
     * desecho del mes).
     */
    private void poblarResumen() {
        vboxResumen.getChildren().clear();
        agregarFilaResumen("Insumos registrados:",       String.valueOf(totalMateriales), false);
        agregarFilaResumen("Insumos en estado crítico:", String.valueOf(criticos),        true);
        agregarFilaResumen("Técnicos activos:",          String.valueOf(tecnicos),        false);
        agregarFilaResumen("Desecho del mes:",           String.format("%.1f kg", pesoMes), false);
    }

    /**
     * Construye y agrega a {@code vboxResumen} una fila con una etiqueta
     * de clave a la izquierda y su valor a la derecha.
     *
     * @param clave texto descriptivo del indicador
     * @param valor valor del indicador ya formateado como texto
     * @param rojo  si es {@code true}, resalta el valor en color rojo
     *              (usado para indicadores de alerta como "críticos")
     */
    private void agregarFilaResumen(String clave, String valor, boolean rojo) {
        HBox fila = new HBox();
        Label lClave = new Label(clave);
        lClave.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        HBox.setHgrow(lClave, Priority.ALWAYS);
        Label lValor = new Label(valor);
        lValor.setStyle(rojo
                ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #EF4444;"
                : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        fila.getChildren().addAll(lClave, lValor);
        vboxResumen.getChildren().add(fila);
    }

    /**
     * Llena {@code vboxCatalogo} con el encabezado de columnas y una fila
     * por cada material registrado, resaltando en rojo los que están en
     * estado "Crítico".
     */
    private void poblarCatalogo() {
        vboxCatalogo.getChildren().clear();
        vboxCatalogo.getChildren().add(headerFila(
                new String[]{"ID", "Nombre", "Categoría", "Total", "Disponible", "Estado"},
                new double[]{50, 155, 105, 65, 80, 80}));
        for (int i = 0; i < materiales.size(); i++) {
            Material m = materiales.get(i);
            String color = "Crítico".equals(m.getEstado()) ? "#EF4444" : "#374151";
            vboxCatalogo.getChildren().add(dataFila(new String[]{
                    String.valueOf(m.getIdMaterial()), m.getNombre(), m.getCategoria(),
                    String.valueOf(m.getCantidadTotal()), String.valueOf(m.getCantidadDisponible()), m.getEstado()
            }, new double[]{50, 155, 105, 65, 80, 80}, i, color));
        }
    }

    /**
     * Llena {@code vboxAsignaciones} con el encabezado de columnas y una
     * fila por cada préstamo registrado, resaltando en rojo los que
     * están en estado "Vencido".
     */
    private void poblarAsignaciones() {
        vboxAsignaciones.getChildren().clear();
        vboxAsignaciones.getChildren().add(headerFila(
                new String[]{"Folio", "Insumo", "Responsable", "Cantidad", "F. Préstamo", "F. Devolución", "Estado"},
                new double[]{55, 115, 115, 60, 80, 80, 60}));
        for (int i = 0; i < prestamos.size(); i++) {
            Prestamo p = prestamos.get(i);
            String color = "Vencido".equals(p.getEstadoTexto()) ? "#EF4444" : "#374151";
            vboxAsignaciones.getChildren().add(dataFila(new String[]{
                    p.getFolio(), p.getMaterialNombre(), p.getUsuarioNombre(),
                    String.valueOf(p.getCantidad()),
                    p.getFechaPrestamoTexto(), p.getFechaDevolucionTexto(), p.getEstadoTexto()
            }, new double[]{55, 115, 115, 60, 80, 80, 60}, i, color));
        }
    }

    /**
     * Llena {@code vboxDesecho} con el encabezado de columnas y una fila
     * por cada registro de desecho existente.
     */
    private void poblarDesecho() {
        vboxDesecho.getChildren().clear();
        vboxDesecho.getChildren().add(headerFila(
                new String[]{"Folio", "Insumo", "Motivo", "Cantidad", "Peso (kg)", "Fecha"},
                new double[]{60, 130, 130, 65, 70, 90}));
        for (int i = 0; i < desechos.size(); i++) {
            Desecho d = desechos.get(i);
            vboxDesecho.getChildren().add(dataFila(new String[]{
                    d.getFolio(), d.getMaterialNombre(), d.getMotivo(),
                    String.valueOf(d.getCantidad()), String.format("%.2f", d.getPeso()), d.getFechaTexto()
            }, new double[]{60, 130, 130, 65, 70, 90}, i, "#374151"));
        }
    }

    /**
     * Llena {@code vboxUsuarios} con el encabezado de columnas y una fila
     * por cada usuario registrado en el sistema.
     */
    private void poblarUsuarios() {
        vboxUsuarios.getChildren().clear();
        vboxUsuarios.getChildren().add(headerFila(
                new String[]{"ID", "Nombre", "Username", "Rol"},
                new double[]{60, 200, 150, 90}));
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            vboxUsuarios.getChildren().add(dataFila(new String[]{
                    String.valueOf(u.getIdUsuario()), u.getNombre(), u.getUsername(), u.getRol()
            }, new double[]{60, 200, 150, 90}, i, "#374151"));
        }
    }

    /**
     * Construye la fila de encabezado (nombres de columna) para una de
     * las tablas mostradas en la vista previa de un reporte.
     *
     * @param cols   textos de cada columna, en orden
     * @param anchos ancho preferido (en píxeles) de cada columna, en el
     *               mismo orden que {@code cols}
     * @return el {@link HBox} con las etiquetas de encabezado ya estilizadas
     */
    private HBox headerFila(String[] cols, double[] anchos) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 6 16 6 16;");
        for (int i = 0; i < cols.length; i++) {
            Label l = new Label(cols[i]);
            l.setPrefWidth(anchos[i]);
            l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
            row.getChildren().add(l);
        }
        return row;
    }

    /**
     * Construye una fila de datos para una de las tablas mostradas en la
     * vista previa de un reporte, alternando el color de fondo según el
     * índice de la fila (efecto "zebra").
     *
     * @param vals   valores de cada celda, en el mismo orden que las columnas
     * @param anchos ancho preferido (en píxeles) de cada columna
     * @param idx    índice de la fila dentro de la lista, usado para alternar
     *               el color de fondo
     * @param color  color (en formato CSS hexadecimal) con el que se
     *               pintará el texto de la fila, por ejemplo para resaltar
     *               registros críticos o vencidos
     * @return el {@link HBox} con las etiquetas de datos ya estilizadas
     */
    private HBox dataFila(String[] vals, double[] anchos, int idx, String color) {
        HBox row = new HBox();
        String bg = idx % 2 != 0 ? "-fx-background-color: #F9FAFB; " : "";
        row.setStyle(bg + "-fx-padding: 6 16 6 16; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 1;");
        for (int i = 0; i < vals.length; i++) {
            Label l = new Label(vals[i] != null ? vals[i] : "");
            l.setPrefWidth(anchos[i]);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
            row.getChildren().add(l);
        }
        return row;
    }

    // ── Descargas PDF ─────────────────────────────────────────────────────────

    /**
     * Genera y guarda un PDF con el resumen general del sistema
     * (insumos registrados, críticos, técnicos activos y desecho del mes).
     * <p>
     * Solicita al usuario la ruta de destino mediante un {@link FileChooser};
     * si el usuario cancela el diálogo, el método termina sin hacer nada.
     * Al finalizar, muestra una alerta de éxito o de error según el
     * resultado de la operación.
     */
    @FXML
    private void descargarResumen() {
        File f = pedirRuta("Resumen_General");
        if (f == null) return;
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Resumen General");
            PdfPTable t = new PdfPTable(2);
            t.setWidthPercentage(70);
            t.setWidths(new float[]{4, 1});
            hCell(t, "Indicador"); hCell(t, "Valor");
            dCell(t, "Insumos registrados",       false); dCell(t, String.valueOf(totalMateriales), false);
            dCell(t, "Insumos en estado crítico", false); dCell(t, String.valueOf(criticos),        true);
            dCell(t, "Técnicos activos",           false); dCell(t, String.valueOf(tecnicos),        false);
            dCell(t, "Desecho del mes",            false); dCell(t, String.format("%.1f kg", pesoMes), false);
            doc.add(t);
            ok("Resumen General guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    /**
     * Genera y guarda un PDF (en orientación horizontal) con el catálogo
     * completo de insumos, resaltando en rojo los materiales en estado
     * "Crítico".
     * <p>
     * Solicita al usuario la ruta de destino mediante un {@link FileChooser};
     * si el usuario cancela el diálogo, el método termina sin hacer nada.
     * Al finalizar, muestra una alerta de éxito o de error según el
     * resultado de la operación.
     */
    @FXML
    private void descargarCatalogo() {
        File f = pedirRuta("Catalogo_Insumos");
        if (f == null) return;
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Catálogo de Insumos");
            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1, 3, 2, 1, 1, 2});
            for (String h : new String[]{"ID", "Nombre", "Categoría", "Total", "Disponible", "Estado"}) hCell(t, h);
            for (Material m : materiales) {
                dCell(t, String.valueOf(m.getIdMaterial()),          false);
                dCell(t, m.getNombre(),                              false);
                dCell(t, m.getCategoria(),                           false);
                dCell(t, String.valueOf(m.getCantidadTotal()),       false);
                dCell(t, String.valueOf(m.getCantidadDisponible()),  false);
                dCell(t, m.getEstado(), "Crítico".equals(m.getEstado()));
            }
            doc.add(t);
            ok("Catálogo guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    /**
     * Genera y guarda un PDF (en orientación horizontal) con el historial
     * completo de préstamos, resaltando en rojo los que están en estado
     * "Vencido".
     * <p>
     * Solicita al usuario la ruta de destino mediante un {@link FileChooser};
     * si el usuario cancela el diálogo, el método termina sin hacer nada.
     * Al finalizar, muestra una alerta de éxito o de error según el
     * resultado de la operación.
     */
    @FXML
    private void descargarAsignaciones() {
        File f = pedirRuta("Historial_Prestamos");
        if (f == null) return;
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Historial de Préstamos");
            PdfPTable t = new PdfPTable(7);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1, 2, 2, 1, 2, 2, 1});
            for (String h : new String[]{"Folio", "Insumo", "Responsable", "Cantidad", "F. Préstamo", "F. Devolución", "Estado"}) hCell(t, h);
            for (Prestamo p : prestamos) {
                dCell(t, p.getFolio(),                false);
                dCell(t, p.getMaterialNombre(),       false);
                dCell(t, p.getUsuarioNombre(),        false);
                dCell(t, String.valueOf(p.getCantidad()), false);
                dCell(t, p.getFechaPrestamoTexto(),   false);
                dCell(t, p.getFechaDevolucionTexto(), false);
                dCell(t, p.getEstadoTexto(), "Vencido".equals(p.getEstadoTexto()));
            }
            doc.add(t);
            ok("Historial de Préstamos guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    /**
     * Genera y guarda un PDF (en orientación horizontal) con el registro
     * completo de desecho de insumos.
     * <p>
     * Solicita al usuario la ruta de destino mediante un {@link FileChooser};
     * si el usuario cancela el diálogo, el método termina sin hacer nada.
     * Al finalizar, muestra una alerta de éxito o de error según el
     * resultado de la operación.
     */
    @FXML
    private void descargarDesecho() {
        File f = pedirRuta("Registro_Desecho");
        if (f == null) return;
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Registro de Desecho");
            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1, 2, 2, 1, 1, 2});
            for (String h : new String[]{"Folio", "Insumo", "Motivo", "Cantidad", "Peso (kg)", "Fecha"}) hCell(t, h);
            for (Desecho d : desechos) {
                dCell(t, d.getFolio(),                        false);
                dCell(t, d.getMaterialNombre(),               false);
                dCell(t, d.getMotivo(),                       false);
                dCell(t, String.valueOf(d.getCantidad()),     false);
                dCell(t, String.format("%.2f", d.getPeso()), false);
                dCell(t, d.getFechaTexto(),                   false);
            }
            doc.add(t);
            ok("Registro de Desecho guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    /**
     * Genera y guarda un PDF con la lista completa de usuarios
     * registrados en el sistema.
     * <p>
     * Solicita al usuario la ruta de destino mediante un {@link FileChooser};
     * si el usuario cancela el diálogo, el método termina sin hacer nada.
     * Al finalizar, muestra una alerta de éxito o de error según el
     * resultado de la operación.
     */
    @FXML
    private void descargarUsuarios() {
        File f = pedirRuta("Usuarios_Registrados");
        if (f == null) return;
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Usuarios Registrados");
            PdfPTable t = new PdfPTable(4);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1, 3, 2, 1});
            for (String h : new String[]{"ID", "Nombre", "Username", "Rol"}) hCell(t, h);
            for (Usuario u : usuarios) {
                dCell(t, String.valueOf(u.getIdUsuario()), false);
                dCell(t, u.getNombre(),                    false);
                dCell(t, u.getUsername(),                  false);
                dCell(t, u.getRol(),                       false);
            }
            doc.add(t);
            ok("Usuarios guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    /**
     * Genera y guarda un único PDF (en orientación horizontal) que combina,
     * en secciones sucesivas, el resumen general, el catálogo de insumos,
     * el historial de préstamos, el registro de desecho y la lista de
     * usuarios. Es la versión "todo en uno" de los cinco métodos
     * {@code descargarXxx()} individuales.
     * <p>
     * Solicita al usuario la ruta de destino mediante un {@link FileChooser};
     * si el usuario cancela el diálogo, el método termina sin hacer nada.
     * Al finalizar, muestra una alerta de éxito o de error según el
     * resultado de la operación.
     */
    @FXML
    private void descargarReporte() {
        File f = pedirRuta("Reporte_General_CGTI");
        if (f == null) return;
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Reporte General del Sistema de Inventario");

            titulo(doc, "Resumen General");
            PdfPTable tRes = new PdfPTable(2);
            tRes.setWidthPercentage(60);
            tRes.setWidths(new float[]{4, 1});
            hCell(tRes, "Indicador"); hCell(tRes, "Valor");
            dCell(tRes, "Insumos registrados",       false); dCell(tRes, String.valueOf(totalMateriales), false);
            dCell(tRes, "Insumos en estado crítico", false); dCell(tRes, String.valueOf(criticos),        true);
            dCell(tRes, "Técnicos activos",           false); dCell(tRes, String.valueOf(tecnicos),        false);
            dCell(tRes, "Desecho del mes",            false); dCell(tRes, String.format("%.1f kg", pesoMes), false);
            doc.add(tRes);

            titulo(doc, "Catálogo de Insumos");
            PdfPTable tCat = new PdfPTable(6);
            tCat.setWidthPercentage(100);
            tCat.setWidths(new float[]{1, 3, 2, 1, 1, 2});
            for (String h : new String[]{"ID", "Nombre", "Categoría", "Total", "Disponible", "Estado"}) hCell(tCat, h);
            for (Material m : materiales) {
                dCell(tCat, String.valueOf(m.getIdMaterial()),         false);
                dCell(tCat, m.getNombre(),                             false);
                dCell(tCat, m.getCategoria(),                          false);
                dCell(tCat, String.valueOf(m.getCantidadTotal()),      false);
                dCell(tCat, String.valueOf(m.getCantidadDisponible()), false);
                dCell(tCat, m.getEstado(), "Crítico".equals(m.getEstado()));
            }
            doc.add(tCat);

            titulo(doc, "Historial de Préstamos");
            PdfPTable tPre = new PdfPTable(7);
            tPre.setWidthPercentage(100);
            tPre.setWidths(new float[]{1, 2, 2, 1, 2, 2, 1});
            for (String h : new String[]{"Folio", "Insumo", "Responsable", "Cantidad", "F. Préstamo", "F. Devolución", "Estado"}) hCell(tPre, h);
            for (Prestamo p : prestamos) {
                dCell(tPre, p.getFolio(),                false);
                dCell(tPre, p.getMaterialNombre(),       false);
                dCell(tPre, p.getUsuarioNombre(),        false);
                dCell(tPre, String.valueOf(p.getCantidad()), false);
                dCell(tPre, p.getFechaPrestamoTexto(),   false);
                dCell(tPre, p.getFechaDevolucionTexto(), false);
                dCell(tPre, p.getEstadoTexto(), "Vencido".equals(p.getEstadoTexto()));
            }
            doc.add(tPre);

            titulo(doc, "Registro de Desecho");
            PdfPTable tDes = new PdfPTable(6);
            tDes.setWidthPercentage(100);
            tDes.setWidths(new float[]{1, 2, 2, 1, 1, 2});
            for (String h : new String[]{"Folio", "Insumo", "Motivo", "Cantidad", "Peso (kg)", "Fecha"}) hCell(tDes, h);
            for (Desecho d : desechos) {
                dCell(tDes, d.getFolio(),                        false);
                dCell(tDes, d.getMaterialNombre(),               false);
                dCell(tDes, d.getMotivo(),                       false);
                dCell(tDes, String.valueOf(d.getCantidad()),     false);
                dCell(tDes, String.format("%.2f", d.getPeso()), false);
                dCell(tDes, d.getFechaTexto(),                   false);
            }
            doc.add(tDes);

            titulo(doc, "Usuarios Registrados");
            PdfPTable tUsr = new PdfPTable(4);
            tUsr.setWidthPercentage(100);
            tUsr.setWidths(new float[]{1, 3, 2, 1});
            for (String h : new String[]{"ID", "Nombre", "Username", "Rol"}) hCell(tUsr, h);
            for (Usuario u : usuarios) {
                dCell(tUsr, String.valueOf(u.getIdUsuario()), false);
                dCell(tUsr, u.getNombre(),                    false);
                dCell(tUsr, u.getUsername(),                  false);
                dCell(tUsr, u.getRol(),                       false);
            }
            doc.add(tUsr);

            ok("Reporte General guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    // ── Helpers PDF ───────────────────────────────────────────────────────────

    /**
     * Abre un diálogo {@link FileChooser} para que el usuario elija dónde
     * guardar el PDF, sugiriendo como nombre inicial el prefijo indicado
     * seguido de la fecha y hora actuales (formato {@code yyyyMMdd_HHmm})
     * y la extensión {@code .pdf}.
     *
     * @param nombre prefijo descriptivo del reporte (por ejemplo,
     *               "Catalogo_Insumos")
     * @return el {@link File} elegido por el usuario, o {@code null} si
     *         canceló el diálogo
     */
    private File pedirRuta(String nombre) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte");
        fc.setInitialFileName(nombre + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fc.showSaveDialog(btnVolver.getScene().getWindow());
    }

    /**
     * Agrega al documento PDF el encabezado institucional: el logo de la
     * UTNG (si se puede cargar), el título del reporte y la fecha/hora
     * de generación.
     * <p>
     * Si el logo no puede cargarse por cualquier motivo, el encabezado
     * se genera igualmente, simplemente sin la imagen.
     *
     * @param doc   documento PDF (ya abierto) al que se agrega el encabezado
     * @param texto título específico del reporte (se antepone "CGTI  |  ")
     * @throws DocumentException si iText no puede agregar los elementos al documento
     */
    private void encabezado(Document doc, String texto) throws DocumentException {
        // Logo institucional
        try {
            URL logoUrl = getClass().getResource("/utng/gtid2/images/logosUTNG.png");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(300, 60);
                logo.setAlignment(Element.ALIGN_CENTER);
                doc.add(logo);
            }
        } catch (Exception e) {
            // Si no carga el logo, continúa sin él
        }

        Font fTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, C_MORADO);
        Paragraph pTit = new Paragraph("CGTI  |  " + texto, fTitulo);
        pTit.setAlignment(Element.ALIGN_CENTER);
        pTit.setSpacingBefore(6);
        doc.add(pTit);

        Font fFecha = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, C_GRIS);
        Paragraph pFecha = new Paragraph("Generado el: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fFecha);
        pFecha.setAlignment(Element.ALIGN_CENTER);
        doc.add(pFecha);
        doc.add(Chunk.NEWLINE);
    }

    /**
     * Agrega al documento PDF un subtítulo de sección (usado en el
     * "Reporte General" para separar cada bloque de datos).
     *
     * @param doc   documento PDF (ya abierto) al que se agrega el subtítulo
     * @param texto texto del subtítulo de la sección
     * @throws DocumentException si iText no puede agregar el párrafo al documento
     */
    private void titulo(Document doc, String texto) throws DocumentException {
        Font f = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(31, 41, 55));
        Paragraph p = new Paragraph(texto, f);
        p.setSpacingBefore(14);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    /**
     * Agrega a la tabla PDF una celda de encabezado de columna, con fondo
     * gris claro y texto en negrita.
     *
     * @param tabla tabla PDF a la que se agrega la celda
     * @param texto nombre de la columna
     */
    private void hCell(PdfPTable tabla, String texto) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, C_HEADER_TEXT);
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setBackgroundColor(C_HEADER_BG);
        c.setPadding(6);
        tabla.addCell(c);
    }

    /**
     * Agrega a la tabla PDF una celda de datos.
     *
     * @param tabla tabla PDF a la que se agrega la celda
     * @param texto valor a mostrar en la celda (se muestra vacío si es {@code null})
     * @param rojo  si es {@code true}, pinta el texto en rojo (usado para
     *              resaltar registros críticos o vencidos)
     */
    private void dCell(PdfPTable tabla, String texto, boolean rojo) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, rojo ? C_ROJO : C_TEXT);
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", f));
        c.setPadding(6);
        tabla.addCell(c);
    }

    /**
     * Muestra una alerta informativa confirmando que la operación de
     * exportación se completó correctamente.
     *
     * @param msg mensaje a mostrar en la alerta
     */
    private void ok(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    /**
     * Muestra una alerta de error indicando que no se pudo generar el
     * PDF, incluyendo el mensaje de la excepción original.
     *
     * @param e excepción capturada durante la generación del PDF
     */
    private void err(Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR,
                "No se pudo generar el PDF: " + e.getMessage(), ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    /**
     * Regresa a la pantalla del panel principal.
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }
}