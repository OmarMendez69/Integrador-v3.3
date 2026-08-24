package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controlador del formulario para dar de alta o editar un usuario del
 * sistema. En modo edición no permite modificar el username ni la
 * contraseña, ya que son datos de acceso originales del registro.
 */
public class AgregarUsuarioController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtUsername;
    @FXML private Label lblPassword;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Button btnGuardar;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private boolean modoEdicion = false;
    private int idUsuario;

    /** Llena el combo de rol con las opciones disponibles del sistema. */
    @FXML
    public void initialize() {
        cmbRol.setItems(FXCollections.observableArrayList("Admin", "Tecnico", "Usuario"));
    }

    /**
     * Precarga el formulario con los datos de un usuario existente,
     * cambia a modo edición y oculta los campos de username y
     * contraseña, que no son editables.
     *
     * @param usuario usuario a editar
     */
    public void cargarUsuario(Usuario usuario) {
        modoEdicion = true;
        idUsuario = usuario.getIdUsuario();

        txtNombre.setText(usuario.getNombre());
        txtUsername.setText(usuario.getUsername());
        cmbRol.setValue(usuario.getRol());
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

    /**
     * Valida los campos requeridos y guarda el usuario: inserta uno
     * nuevo con contraseña (hasheada en {@link UsuarioDAO}), o
     * actualiza solo el nombre y rol si está en modo edición.
     */
    @FXML
    private void guardarUsuario() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String rol = cmbRol.getValue();

        if (nombre.isEmpty() || rol == null || rol.isEmpty() || (!modoEdicion && (username.isEmpty() || password.isEmpty()))) {
            mostrarAlerta("Completa todos los campos requeridos.");
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

    /**
     * Muestra una alerta de advertencia con el mensaje indicado.
     *
     * @param mensaje texto a mostrar en la alerta
     */
    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    /** Limpia todos los campos del formulario. */
    @FXML
    private void accionCancelar() {
        txtNombre.clear();
        txtUsername.clear();
        txtPassword.clear();
        cmbRol.setValue(null);
    }

    /**
     * Regresa a la lista de usuarios, reemplazando la escena actual.
     *
     * @throws IOException si no se puede cargar el archivo FXML de la lista
     */
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