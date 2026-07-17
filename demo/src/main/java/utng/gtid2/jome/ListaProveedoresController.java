package utng.gtid2.jome;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import utng.gtid2.dao.ProveedorDAO;
import utng.gtid2.modelo.Proveedor;

import java.io.IOException;
import java.sql.SQLException;

public class ListaProveedoresController {

    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Integer> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colContacto;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEstado;
    @FXML private Button btnResultado;
    @FXML private Button btnVolver;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colContacto.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            ObservableList<Proveedor> proveedores = FXCollections.observableArrayList(proveedorDAO.listarTodos());
            tablaProveedores.setItems(proveedores);
        } catch (SQLException e) {
            mostrarError("No se pudo cargar la lista de proveedores: " + e.getMessage());
        }
    }

    @FXML
    private void accionAgregar() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarProveedor.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Agregar Proveedor");
        stage.show();
    }

    @FXML
    private void accionEditar() throws IOException {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un proveedor de la tabla para editar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarProveedor.fxml"));
        Parent root = loader.load();

        AgregarProveedorController controller = loader.getController();
        controller.cargarProveedor(seleccionado);

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar Proveedor");
        stage.show();
    }

    @FXML
    private void accionEliminar() {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un proveedor de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres eliminar a \"" + seleccionado.getNombre() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    proveedorDAO.eliminar(seleccionado.getIdProveedor());
                    cargarDatos();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el proveedor: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}