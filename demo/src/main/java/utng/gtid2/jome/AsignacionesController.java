package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controlador del formulario de asignación de material a un técnico.
 * Permite precargar los datos de una asignación existente para su
 * edición mediante {@link #cargarAsignacion(String, String, String, String)}.
 */
public class AsignacionesController {

    @FXML
    private TextField txtFolio;

    @FXML
    private TextField txtTecAsignado;

    @FXML
    private TextField txtMaterial;

    @FXML
    private TextField txtFecha;

    @FXML
    private TextField txtObservaciones;

    @FXML
    private Button btnGuardar;

    private boolean modoEdicion = false;

    /**
     * Precarga el formulario con los datos de una asignación existente
     * y cambia el formulario a modo edición.
     *
     * @param folio    folio de la asignación
     * @param tecnico  nombre del técnico asignado
     * @param material nombre del material asignado
     * @param fecha    fecha de la asignación
     */
    public void cargarAsignacion(String folio, String tecnico, String material, String fecha) {
        modoEdicion = true;
        txtFolio.setText(folio);
        txtTecAsignado.setText(tecnico);
        txtMaterial.setText(material);
        txtFecha.setText(fecha);
        btnGuardar.setText("Actualizar");
    }

    @FXML
    private void mostrarInformacion() {
        // por ahora no hace nada
    }

    /** Limpia todos los campos del formulario. */
    @FXML
    private void accionCancelar() {
        txtFolio.clear();
        txtTecAsignado.clear();
        txtMaterial.clear();
        txtFecha.clear();
        txtObservaciones.clear();
    }

    /**
     * Regresa a la lista de asignaciones, reemplazando la escena actual.
     *
     * @throws IOException si no se puede cargar el archivo FXML de la lista
     */
    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaAsignaciones.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) txtFolio.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Asignaciones");
        stage.show();
    }
}