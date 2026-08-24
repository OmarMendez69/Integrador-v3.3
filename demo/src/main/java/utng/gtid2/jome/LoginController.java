package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controlador de la pantalla de inicio de sesión. Verifica las
 * credenciales contra la base de datos mediante {@link UsuarioDAO},
 * abre el panel principal si son correctas y da acceso a la pantalla
 * de registro de usuario nuevo.
 */
public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje;

    /**
     * Valida los campos del formulario, verifica las credenciales contra
     * la base de datos y, si son correctas, inicia sesión y navega al
     * panel principal. Muestra un mensaje de error en pantalla si los
     * campos están vacíos, las credenciales son incorrectas o falla la
     * conexión.
     */
    @FXML
    private void accionLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Validación de campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            lblMensaje.setText("Por favor completa todos los campos.");
            lblMensaje.setStyle("-fx-text-fill: #ff4444;");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.verificarCredenciales(username, password);

            if (usuario == null) {
                lblMensaje.setText("Usuario o contraseña incorrectos.");
                lblMensaje.setStyle("-fx-text-fill: #ff4444;");
                return;
            }

            // Guardar sesión
            Sesion.iniciar(usuario);

            // Navegar al dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
            Parent root = loader.load();

            PrimaryController controller = loader.getController();
            controller.setUsuario(Sesion.getNombre());

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel Principal");
            stage.show();

        } catch (SQLException e) {
            e.printStackTrace();
            lblMensaje.setText("Error de conexión con la base de datos.");
            lblMensaje.setStyle("-fx-text-fill: #ff4444;");
        } catch (IOException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al abrir el panel principal.");
            lblMensaje.setStyle("-fx-text-fill: #ff4444;");
        }
    }

    /**
     * Abre la pantalla de registro de usuario nuevo, reemplazando la
     * escena actual.
     */
    @FXML
    private void accionRegistrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("from_RegistroUsuarios.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblMensaje.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Usuario");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al abrir el registro.");
            lblMensaje.setStyle("-fx-text-fill: #ff4444;");
        }
    }
}