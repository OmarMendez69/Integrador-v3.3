package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controlador de la pantalla de baja definitiva de un material del
 * catálogo. A diferencia del registro de desecho, aquí se elimina por
 * completo el material de la base de datos, previa confirmación del
 * usuario mediante una alerta.
 */
public class BajaInsumoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCategoria;
    @FXML private Button btnVolver;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private int idMaterial;

    /**
     * Precarga el formulario con los datos del material que se va a dar
     * de baja.
     *
     * @param idMaterial identificador del material a eliminar
     * @param nombre     nombre del material
     * @param categoria  categoría del material
     */
    public void cargarProducto(int idMaterial, String nombre, String categoria) {
        this.idMaterial = idMaterial;
        txtCodigo.setText(String.valueOf(idMaterial));
        txtNombre.setText(nombre);
        txtCategoria.setText(categoria);
    }

    /**
     * Pide confirmación al usuario y, si acepta, elimina el material de
     * forma permanente mediante {@link MaterialDAO#eliminar(int)} y
     * regresa al catálogo.
     */
    @FXML
    private void eliminarDefinitivamente() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres eliminar \"" + txtNombre.getText() + "\"? Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    materialDAO.eliminar(idMaterial);
                    accionVolver();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el insumo: " + e.getMessage());
                } catch (IOException e) {
                    mostrarError("Se eliminó, pero no se pudo volver al catálogo: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Muestra una alerta de error con el mensaje indicado.
     *
     * @param mensaje texto a mostrar en la alerta
     */
    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    /** Limpia todos los campos del formulario. */
    @FXML
    private void accionCancelar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtCategoria.clear();
    }

    /**
     * Regresa al catálogo de insumos, reemplazando la escena actual.
     *
     * @throws IOException si no se puede cargar el archivo FXML del catálogo
     */
    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Catalogo.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Catálogo de Insumos");
        stage.show();
    }
}