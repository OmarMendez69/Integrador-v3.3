package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class PrimaryController {

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblId;

    private String usuarioActual;

    public void setUsuario(String nombreUsuario) {
        this.usuarioActual = nombreUsuario;
        lblId.setText("Bienvenido, " + nombreUsuario);
    }

    @FXML
    private void irAInicio() {
    }

    @FXML
    private void irACatalogo() throws IOException {
        cambiarPantalla("from_Catalogo.fxml", "Catálogo de Insumos");
    }

    @FXML
    private void irAAsignaciones() throws IOException {
        cambiarPantalla("from_ListaAsignaciones.fxml", "Asignaciones");
    }

    @FXML
    private void irADesecho() throws IOException {
        cambiarPantalla("from_ListaDesecho.fxml", "Registro de Desecho");
    }

    @FXML
    private void irAUsuarios() throws IOException {
        cambiarPantalla("from_ListaUsuarios.fxml", "Usuarios");
    }

    @FXML
    private void irAPrestamo() throws IOException {
        cambiarPantalla("from_Prestamo.fxml", "Préstamo");
    }

    @FXML
    private void irAProveedores() throws IOException {
        cambiarPantalla("from_ListaProveedores.fxml", "Proveedores");
    }

    @FXML
    private void irAReportes() throws IOException {
        cambiarPantalla("from_Reportes.fxml", "Reportes");
    }

    @FXML
    private void cerrarSesion() throws IOException {
        cambiarPantalla("Pantalla_Login.fxml", "Login");
    }

    private void cambiarPantalla(String nombreFxml, String tituloVentana) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(nombreFxml));
        Parent root = loader.load();

        Stage stage = (Stage) lblId.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(tituloVentana);
        stage.show();
    }
}