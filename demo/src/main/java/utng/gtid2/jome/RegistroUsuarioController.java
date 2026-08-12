package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

public class RegistroUsuarioController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnResultado;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblRegistro;

    @FXML
    private void mostrarInformacion() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Validación de campos vacíos
        if (nombre.isEmpty() || username.isEmpty() || password.isEmpty()) {
            lblRegistro.setText("Por favor completa todos los campos.");
            lblRegistro.setStyle("-fx-text-fill: #ff4444;");
            return;
        }

        // Validación de longitud mínima de contraseña
        if (password.length() < 4) {
            lblRegistro.setText("La contraseña debe tener al menos 4 caracteres.");
            lblRegistro.setStyle("-fx-text-fill: #ff4444;");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();

            // Validar que el username no exista ya
            if (dao.existeUsername(username)) {
                lblRegistro.setText("El nombre de usuario ya está en uso.");
                lblRegistro.setStyle("-fx-text-fill: #ff4444;");
                return;
            }

            // Rol por defecto: Usuario
            Usuario nuevo = new Usuario(0, nombre, username, password, "Usuario");
            dao.insertar(nuevo);

            lblRegistro.setText("¡Usuario registrado correctamente!");
            lblRegistro.setStyle("-fx-text-fill: #2E7D32;");

            // Limpiar campos
            txtNombre.clear();
            txtUsername.clear();
            txtPassword.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            lblRegistro.setText("Error de conexión con la base de datos.");
            lblRegistro.setStyle("-fx-text-fill: #ff4444;");
        }
    }

    @FXML
    private void accionVolverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Pantalla_Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblRegistro.setText("Error al volver al login.");
            lblRegistro.setStyle("-fx-text-fill: #ff4444;");
        }
    }
}