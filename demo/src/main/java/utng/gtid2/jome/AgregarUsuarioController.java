package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

public class AgregarUsuarioController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtUsername;
    @FXML private Label lblPassword;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtRol;
    @FXML private Button btnGuardar;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private boolean modoEdicion = false;
    private int idUsuario;

    public void cargarUsuario(Usuario usuario) {
        modoEdicion = true;
        idUsuario = usuario.getIdUsuario();

        txtNombre.setText(usuario.getNombre());
        txtUsername.setText(usuario.getUsername());
        txtRol.setText(usuario.getRol());
        btnGuardar.setText("Actualizar");
        lblTitulo.setText("Editar Usuario");

        // En modo edición no se permite tocar username ni password:
        // ambos son datos de acceso originales del primer registro.
        txtUsername.setDisable(true);
        lblPassword.setVisible(false);
        lblPassword.setManaged(false);
        txtPassword.setVisible(false);
        txtPassword.setManaged(false);
    }

    @FXML
    private void guardarUsuario() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String rol = txtRol.getText().trim();

        if (nombre.isEmpty() || rol.isEmpty() || (!modoEdicion && (username.isEmpty() || password.isEmpty()))) {
            mostrarAlerta("Completa todos los campos requeridos.");
            return;
        }

        if (!rol.equalsIgnoreCase("Admin") && !rol.equalsIgnoreCase("Tecnico") && !rol.equalsIgnoreCase("Usuario")) {
            mostrarAlerta("El rol debe ser Admin, Tecnico o Usuario.");
            return;
        }

        try {
            if (modoEdicion) {
                usuarioDAO.actualizarNombreRol(idUsuario, nombre, rol);
            } else {
                Usuario usuario = new Usuario(0, nombre, username, password, rol);
                usuarioDAO.insertar(usuario);
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
        txtUsername.clear();
        txtPassword.clear();
        txtRol.clear();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaUsuarios.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Lista de Usuarios");
        stage.show();
    }
}