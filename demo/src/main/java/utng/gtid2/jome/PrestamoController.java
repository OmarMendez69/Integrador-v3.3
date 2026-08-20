package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.dao.PrestamoDAO;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Material;
import utng.gtid2.modelo.Prestamo;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PrestamoController implements Initializable {

    @FXML private TextField txtFolio;
    @FXML private ComboBox<String> cmbInsumo;
    @FXML private TextField txtCantidad;
    @FXML private Label lblDisponibleInfo;
    @FXML private ComboBox<String> cmbResponsable;
    @FXML private DatePicker dpFechaPrestamo;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private TextField txtObservaciones;
    @FXML private Label lblError;
    @FXML private Button btnVolver;
    @FXML private Button btnVerHistorial;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    private final Map<String, Material> materialesPorNombre = new HashMap<>();
    private final Map<String, Usuario> usuariosPorNombre = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
        cargarInsumos();
        cargarResponsables();
        cargarSiguienteFolio();

        txtCantidad.setText("1");

        cmbInsumo.valueProperty().addListener((obs, viejo, nuevo) -> actualizarDisponibleInfo());
    }

    private void cargarInsumos() {
        try {
            cmbInsumo.getItems().clear();
            materialesPorNombre.clear();
            for (Material material : materialDAO.listarTodos()) {
                if (material.getCantidadDisponible() > 0) {
                    cmbInsumo.getItems().add(material.getNombre());
                    materialesPorNombre.put(material.getNombre(), material);
                }
            }
        } catch (SQLException e) {
            lblError.setText("No se pudo cargar el catálogo: " + e.getMessage());
        }
    }

    private void actualizarDisponibleInfo() {
        String nombreInsumo = cmbInsumo.getValue();
        if (nombreInsumo == null) {
            lblDisponibleInfo.setText("");
            return;
        }
        Material material = materialesPorNombre.get(nombreInsumo);
        if (material != null) {
            lblDisponibleInfo.setText("Disp: " + material.getCantidadDisponible());
        }
    }

    private void cargarResponsables() {
        try {
            cmbResponsable.getItems().clear();
            usuariosPorNombre.clear();
            for (Usuario usuario : usuarioDAO.listarTodos()) {
                cmbResponsable.getItems().add(usuario.getNombre());
                usuariosPorNombre.put(usuario.getNombre(), usuario);
            }

            if (Sesion.estaActiva()) {
                String nombreSesion = Sesion.getNombre();
                if (cmbResponsable.getItems().contains(nombreSesion)) {
                    cmbResponsable.setValue(nombreSesion);
                }
                cmbResponsable.setDisable(true);
            }

        } catch (SQLException e) {
            lblError.setText("No se pudieron cargar los usuarios: " + e.getMessage());
        }
    }

    private void cargarSiguienteFolio() {
        try {
            txtFolio.setText(prestamoDAO.generarSiguienteFolio());
        } catch (SQLException e) {
            txtFolio.setText("F001");
        }
    }

    @FXML
    private void mostrarInformacion() {
        String folio = txtFolio.getText();
        String nombreInsumo = cmbInsumo.getValue();
        String nombreResponsable = cmbResponsable.getValue();
        LocalDate fechaPrestamo = dpFechaPrestamo.getValue();
        LocalDate fechaDevolucion = dpFechaDevolucion.getValue();
        String textoCantidad = txtCantidad.getText() == null ? "" : txtCantidad.getText().trim();

        if (nombreInsumo == null || nombreResponsable == null || fechaPrestamo == null) {
            lblError.setText("Completa Insumo, Responsable y Fecha de Préstamo antes de registrar.");
            return;
        }

        if (textoCantidad.isEmpty()) {
            lblError.setText("Indica la cantidad a prestar.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(textoCantidad);
        } catch (NumberFormatException e) {
            lblError.setText("La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            lblError.setText("La cantidad debe ser mayor a 0.");
            return;
        }

        Material material = materialesPorNombre.get(nombreInsumo);
        Usuario usuario = usuariosPorNombre.get(nombreResponsable);

        if (material == null || usuario == null) {
            lblError.setText("Selecciona un insumo y un responsable válidos de la lista.");
            return;
        }

        if (cantidad > material.getCantidadDisponible()) {
            lblError.setText("Solo hay " + material.getCantidadDisponible() + " unidades disponibles de \""
                    + material.getNombre() + "\".");
            return;
        }

        if (fechaDevolucion == null) {
            fechaDevolucion = fechaPrestamo.plusDays(7);
        }

        Prestamo prestamo = new Prestamo();
        prestamo.setFolio(folio);
        prestamo.setIdMaterial(material.getIdMaterial());
        prestamo.setIdUsuario(usuario.getIdUsuario());
        prestamo.setCantidad(cantidad);
        prestamo.setFechaPrestamo(fechaPrestamo);
        prestamo.setFechaDevolucion(fechaDevolucion);
        prestamo.setObservaciones(txtObservaciones.getText().trim());

        try {
            prestamoDAO.registrarPrestamo(prestamo);
            lblError.setText("");

            Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
            confirmacion.setTitle("Préstamo registrado");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("Préstamo de " + cantidad + " unidad(es) de \"" + nombreInsumo + "\" registrado correctamente.");
            confirmacion.showAndWait();

            accionCancelar();
            cargarInsumos();
            cargarSiguienteFolio();

        } catch (SQLException e) {
            lblError.setText("Error al registrar el préstamo: " + e.getMessage());
        }
    }

    @FXML
    private void accionCancelar() {
        cmbInsumo.getSelectionModel().clearSelection();
        txtCantidad.setText("1");
        lblDisponibleInfo.setText("");
        dpFechaPrestamo.setValue(null);
        dpFechaDevolucion.setValue(null);
        txtObservaciones.clear();
        lblError.setText("");
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

    @FXML
    private void accionVerHistorial() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaAsignaciones.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVerHistorial.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Historial de Préstamos");
        stage.show();
    }
}