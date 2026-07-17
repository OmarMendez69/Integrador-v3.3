package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.modelo.Material;

import java.io.IOException;
import java.sql.SQLException;

public class AgregarProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtUbicacion;
    @FXML private TextField txtCantidadTotal;
    @FXML private TextField txtDisponible;   // deshabilitado: lo calcula el sistema
    @FXML private TextField txtCostoUnitario;
    @FXML private TextField txtEstado;       // deshabilitado: Crítico si Disponible <= 10
    @FXML private Button btnGuardar;
    @FXML private Label lblTitulo;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private boolean modoEdicion = false;
    private Material materialEnEdicion;

    public void cargarProducto(Material material) {
        modoEdicion = true;
        materialEnEdicion = material;

        txtCodigo.setText(String.valueOf(material.getIdMaterial()));
        txtNombre.setText(material.getNombre());
        txtCategoria.setText(material.getCategoria());
        txtUbicacion.setText(material.getUbicacion());
        txtCantidadTotal.setText(String.valueOf(material.getCantidadTotal()));
        txtDisponible.setText(String.valueOf(material.getCantidadDisponible()));
        txtCostoUnitario.setText(String.format("%.2f", material.getCostoUnitario()));
        txtEstado.setText(material.getEstado());

        lblTitulo.setText("Editar Producto");
        btnGuardar.setText("Actualizar");
    }

    @FXML
    private void guardarMaterial() {
        String nombre = txtNombre.getText().trim();
        String categoria = txtCategoria.getText().trim();
        String ubicacion = txtUbicacion.getText().trim();
        String textoCantidad = txtCantidadTotal.getText().trim();
        String textoCosto = txtCostoUnitario.getText().trim();

        if (nombre.isEmpty() || categoria.isEmpty() || textoCantidad.isEmpty() || textoCosto.isEmpty()) {
            mostrarAlerta("Completa nombre, categoría, cantidad total y costo unitario.");
            return;
        }

        int cantidadTotal;
        double costoUnitario;
        try {
            cantidadTotal = Integer.parseInt(textoCantidad);
            costoUnitario = Double.parseDouble(textoCosto);
        } catch (NumberFormatException e) {
            mostrarAlerta("Cantidad total debe ser entero y costo unitario un número (ej. 150.00).");
            return;
        }

        if (cantidadTotal < 0 || costoUnitario < 0) {
            mostrarAlerta("Cantidad total y costo unitario no pueden ser negativos.");
            return;
        }

        try {
            if (modoEdicion) {
                int prestados = materialEnEdicion.getCantidadTotal() - materialEnEdicion.getCantidadDisponible();
                int nuevoDisponible = Math.max(cantidadTotal - prestados, 0);
                String estado = nuevoDisponible <= 10 ? "Crítico" : "Disponible";

                Material material = new Material(materialEnEdicion.getIdMaterial(), nombre, categoria,
                        cantidadTotal, nuevoDisponible, ubicacion, costoUnitario, estado);
                materialDAO.actualizar(material);
            } else {
                String estado = cantidadTotal <= 10 ? "Crítico" : "Disponible";
                Material material = new Material(0, nombre, categoria, cantidadTotal,
                        cantidadTotal, ubicacion, costoUnitario, estado);
                materialDAO.insertar(material);
            }

            accionVolver();

        } catch (SQLException e) {
            mostrarAlerta("Error al guardar en la base de datos: " + e.getMessage());
        } catch (IOException e) {
            mostrarAlerta("Se guardó, pero no se pudo volver al catálogo: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @FXML
    private void accionCancelar() {
        txtNombre.clear();
        txtCategoria.clear();
        txtUbicacion.clear();
        txtCantidadTotal.clear();
        txtCostoUnitario.clear();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Catalogo.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) txtCodigo.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Catálogo de Insumos");
        stage.show();
    }
}