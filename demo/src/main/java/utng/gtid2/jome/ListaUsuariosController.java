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

import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

public class ListaUsuariosController {

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private Button btnResultado;
    @FXML private Button btnVolver;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            ObservableList<Usuario> usuarios = FXCollections.observableArrayList(usuarioDAO.listarTodos());
            tablaUsuarios.setItems(usuarios);
        } catch (SQLException e) {
            mostrarError("No se pudo cargar la lista de usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void accionAgregar() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarUsuario.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Agregar Usuario");
        stage.show();
    }

    @FXML
    private void accionEditar() throws IOException {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un usuario de la tabla para editar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarUsuario.fxml"));
        Parent root = loader.load();

        AgregarUsuarioController controller = loader.getController();
        controller.cargarUsuario(seleccionado);

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar Usuario");
        stage.show();
    }

    @FXML
    private void accionEliminar() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un usuario de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres eliminar a \"" + seleccionado.getNombre() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    usuarioDAO.eliminar(seleccionado.getIdUsuario());
                    cargarDatos();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el usuario: " + e.getMessage());
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