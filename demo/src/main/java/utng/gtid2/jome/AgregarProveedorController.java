package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.ProveedorDAO;
import utng.gtid2.modelo.Proveedor;

import java.io.IOException;
import java.sql.SQLException;

public class AgregarProveedorController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtContacto;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cbEstado;
    @FXML private Button btnGuardar;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private boolean modoEdicion = false;
    private int idProveedor;

    @FXML
    public void initialize() {
        cbEstado.getItems().setAll("Activo", "Inactivo");
        cbEstado.getSelectionModel().selectFirst();
    }

    public void cargarProveedor(Proveedor proveedor) {
        modoEdicion = true;
        idProveedor = proveedor.getIdProveedor();

        txtNombre.setText(proveedor.getNombre());
        txtContacto.setText(proveedor.getContacto());
        txtTelefono.setText(proveedor.getTelefono());
        txtCorreo.setText(proveedor.getCorreo());
        cbEstado.getSelectionModel().select(proveedor.getEstado());
        btnGuardar.setText("Actualizar");
        lblTitulo.setText("Editar Proveedor");
    }

    @FXML
    private void guardarProveedor() {
        String nombre = txtNombre.getText().trim();
        String contacto = txtContacto.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String estado = cbEstado.getValue();

        if (nombre.isEmpty() || contacto.isEmpty() || telefono.isEmpty() || estado == null) {
            mostrarAlerta("Completa todos los campos requeridos.");
            return;
        }

        try {
            if (modoEdicion) {
                Proveedor proveedor = new Proveedor(idProveedor, nombre, contacto, telefono, correo, estado);
                proveedorDAO.actualizar(proveedor);
            } else {
                Proveedor proveedor = new Proveedor(0, nombre, contacto, telefono, correo, estado);
                proveedorDAO.insertar(proveedor);
            }
            accionVolver();
        } catch (SQLException e) {
            mostrarAlerta("Error al guardar en la base de datos: " + e.getMessage());
        } catch (IOException e) {
            mostrarAlerta("Se guardó, pero no se pudo volver a la lista: " + e.getMessage());
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
        txtContacto.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        cbEstado.getSelectionModel().selectFirst();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaProveedores.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Lista de Proveedores");
        stage.show();
    }
}