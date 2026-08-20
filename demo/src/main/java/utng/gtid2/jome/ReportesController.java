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

    @FXML
    public void initialize() {
        cargarDatos();
    }

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

    private void poblarResumen() {
        vboxResumen.getChildren().clear();
        agregarFilaResumen("Insumos registrados:",       String.valueOf(totalMateriales), false);
        agregarFilaResumen("Insumos en estado crítico:", String.valueOf(criticos),        true);
        agregarFilaResumen("Técnicos activos:",          String.valueOf(tecnicos),        false);
        agregarFilaResumen("Desecho del mes:",           String.format("%.1f kg", pesoMes), false);
    }

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

    private File pedirRuta(String nombre) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte");
        fc.setInitialFileName(nombre + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fc.showSaveDialog(btnVolver.getScene().getWindow());
    }

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

    private void titulo(Document doc, String texto) throws DocumentException {
        Font f = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(31, 41, 55));
        Paragraph p = new Paragraph(texto, f);
        p.setSpacingBefore(14);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private void hCell(PdfPTable tabla, String texto) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, C_HEADER_TEXT);
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setBackgroundColor(C_HEADER_BG);
        c.setPadding(6);
        tabla.addCell(c);
    }

    private void dCell(PdfPTable tabla, String texto, boolean rojo) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, rojo ? C_ROJO : C_TEXT);
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", f));
        c.setPadding(6);
        tabla.addCell(c);
    }

    private void ok(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void err(Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR,
                "No se pudo generar el PDF: " + e.getMessage(), ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

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