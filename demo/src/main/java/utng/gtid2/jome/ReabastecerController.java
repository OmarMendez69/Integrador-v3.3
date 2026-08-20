package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.modelo.Material;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReabastecerController {

    @FXML private Label lblNombreMaterial;
    @FXML private Label lblDisponibleActual;
    @FXML private Label lblTotalActual;
    @FXML private Label lblEstadoActual;

    @FXML private TextField txtCantidadAAgregar;
    @FXML private DatePicker dpFechaEntrada;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblPreview;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private Material materialSeleccionado;

    public void cargarMaterial(Material material) {
        this.materialSeleccionado = material;

        lblNombreMaterial.setText(material.getNombre());
        lblDisponibleActual.setText(String.valueOf(material.getCantidadDisponible()));
        lblTotalActual.setText(String.valueOf(material.getCantidadTotal()));
        lblEstadoActual.setText(material.getEstado());
        lblEstadoActual.setStyle("-fx-font-size: 14px; -fx-font-weight: BOLD; -fx-text-fill: "
                + ("Crítico".equals(material.getEstado()) ? "#C0392B" : "#2E7D32") + ";");

        dpFechaEntrada.setValue(LocalDate.now());

        txtCantidadAAgregar.textProperty().addListener((obs, viejo, nuevo) -> actualizarPreview());
        actualizarPreview();
    }

    private void actualizarPreview() {
        if (materialSeleccionado == null) return;

        String texto = txtCantidadAAgregar.getText() == null ? "" : txtCantidadAAgregar.getText().trim();
        if (texto.isEmpty()) {
            lblPreview.setText("");
            return;
        }

        try {
            int cantidad = Integer.parseInt(texto);
            if (cantidad <= 0) {
                lblPreview.setText("");
                return;
            }
            int nuevoDisponible = materialSeleccionado.getCantidadDisponible() + cantidad;
            int nuevoTotal = materialSeleccionado.getCantidadTotal() + cantidad;
            lblPreview.setText("Nuevo stock: " + nuevoDisponible + " disponibles de " + nuevoTotal + " en total.");
        } catch (NumberFormatException e) {
            lblPreview.setText("");
        }
    }

    @FXML
    private void guardarReabastecimiento() {
        lblError.setText("");

        if (materialSeleccionado == null) {
            lblError.setText("No se seleccionó ningún material.");
            return;
        }

        String textoCantidad = txtCantidadAAgregar.getText() == null ? "" : txtCantidadAAgregar.getText().trim();
        if (textoCantidad.isEmpty()) {
            lblError.setText("Indica la cantidad a añadir.");
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

        if (dpFechaEntrada.getValue() == null) {
            lblError.setText("Selecciona la fecha de entrada.");
            return;
        }

        try {
            materialDAO.reabastecer(materialSeleccionado.getIdMaterial(), cantidad);

            Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
            confirmacion.setTitle("Reabastecimiento registrado");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("Se añadieron " + cantidad + " unidades a \""
                    + materialSeleccionado.getNombre() + "\".");
            confirmacion.showAndWait();

            accionVolver();

        } catch (SQLException e) {
            lblError.setText("Error al reabastecer: " + e.getMessage());
        } catch (IOException e) {
            lblError.setText("Se reabasteció, pero no se pudo volver al catálogo: " + e.getMessage());
        }
    }

    @FXML
    private void accionCancelar() {
        try {
            accionVolver();
        } catch (IOException e) {
            lblError.setText("No se pudo volver al catálogo: " + e.getMessage());
        }
    }

    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Catalogo.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Gestión de Materiales");
        stage.show();
    }
}