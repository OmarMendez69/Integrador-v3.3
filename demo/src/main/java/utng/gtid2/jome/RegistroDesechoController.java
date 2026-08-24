package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.DesechoDAO;
import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Desecho;
import utng.gtid2.modelo.Material;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controlador del formulario para registrar o editar una baja parcial
 * de material (desecho). Carga los insumos con stock disponible y los
 * usuarios registrados para llenar los combos, y en modo edición
 * restringe los campos que ya afectaron el inventario (insumo,
 * cantidad, peso, responsable) para que solo se puedan corregir el
 * motivo, la fecha y la descripción.
 */
public class RegistroDesechoController implements Initializable {

    @FXML private TextField txtFolio;
    @FXML private ComboBox<String> cmbInsumo;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPeso;
    @FXML private TextField txtMotivo;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbResponsable;
    @FXML private TextArea txtDescripcion;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Button btnVolver;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DesechoDAO desechoDAO = new DesechoDAO();

    private final Map<String, Material> materialesPorNombre = new HashMap<>();
    private final Map<String, Usuario> usuariosPorNombre = new HashMap<>();

    private boolean modoEdicion = false;
    private int idDesecho;

    /**
     * Llena los combos de insumo y responsable, y genera el folio del
     * siguiente registro de desecho.
     *
     * @param url ubicación usada para resolver rutas relativas del FXML (no utilizado)
     * @param rb  recursos de internacionalización del FXML (no utilizado)
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
        cargarInsumos();
        cargarResponsables();
        cargarSiguienteFolio();
    }

    /** Llena el combo de insumos solo con los materiales que tienen stock disponible. */
    private void cargarInsumos() {
        try {
            cmbInsumo.getItems().clear();
            materialesPorNombre.clear();
            for (Material material : materialDAO.listarTodos()) {
                if (material.getCantidadDisponible() > 0) {
                    cmbInsumo.getItems().add(material.getNombre());
                    materialesPorNombre.put(material.getNombre(), material);
                }
            }
        } catch (SQLException e) {
            lblError.setText("No se pudo cargar el catálogo: " + e.getMessage());
        }
    }

    /**
     * Llena el combo de responsables con los usuarios registrados y, si
     * hay una sesión activa, preselecciona y bloquea el responsable con
     * el usuario que inició sesión.
     */
    private void cargarResponsables() {
        try {
            cmbResponsable.getItems().clear();
            usuariosPorNombre.clear();
            for (Usuario usuario : usuarioDAO.listarTodos()) {
                cmbResponsable.getItems().add(usuario.getNombre());
                usuariosPorNombre.put(usuario.getNombre(), usuario);
            }

            if (Sesion.estaActiva()) {
                String nombreSesion = Sesion.getNombre();
                if (cmbResponsable.getItems().contains(nombreSesion)) {
                    cmbResponsable.setValue(nombreSesion);
                }
                cmbResponsable.setDisable(true);
            }

        } catch (SQLException e) {
            lblError.setText("No se pudieron cargar los usuarios: " + e.getMessage());
        }
    }

    /** Genera y muestra el folio autogenerado para el siguiente desecho. */
    private void cargarSiguienteFolio() {
        try {
            txtFolio.setText(desechoDAO.generarSiguienteFolio());
        } catch (SQLException e) {
            txtFolio.setText("DS-0001");
        }
    }

    /**
     * Precarga el formulario con los datos de un desecho existente y
     * bloquea los campos que ya afectaron el inventario (insumo,
     * cantidad, peso y responsable), dejando editables solo motivo,
     * fecha y descripción.
     *
     * @param desecho registro de desecho a editar
     */
    public void cargarDesecho(Desecho desecho) {
        modoEdicion = true;
        idDesecho = desecho.getIdDesecho();

        txtFolio.setText(desecho.getFolio());
        cmbInsumo.setValue(desecho.getMaterialNombre());
        txtCantidad.setText(String.valueOf(desecho.getCantidad()));
        txtPeso.setText(String.valueOf(desecho.getPeso()));
        txtMotivo.setText(desecho.getMotivo());

        String fechaTexto = desecho.getFechaTexto();
        if (fechaTexto != null && !fechaTexto.isBlank()) {
            try {
                dpFecha.setValue(LocalDate.parse(fechaTexto, FORMATO_FECHA));
            } catch (Exception e) {
                dpFecha.setValue(null);
            }
        }

        cmbResponsable.setValue(desecho.getUsuarioNombre());
        txtDescripcion.setText(desecho.getDescripcion());

        cmbInsumo.setDisable(true);
        txtCantidad.setDisable(true);
        txtPeso.setDisable(true);
        cmbResponsable.setDisable(true);

        btnGuardar.setText("Actualizar");
    }

    /**
     * Valida los campos del formulario y guarda el desecho: si está en
     * modo edición solo actualiza motivo, fecha y descripción; si es un
     * registro nuevo, lo inserta y descuenta el stock del material
     * afectado mediante {@link DesechoDAO#registrar(Desecho)}.
     */
    @FXML
    private void mostrarInformacion() {
        String nombreInsumo = cmbInsumo.getValue();
        String textoCantidad = txtCantidad.getText().trim();
        String textoPeso = txtPeso.getText().trim();
        String motivo = txtMotivo.getText().trim();
        LocalDate fecha = dpFecha.getValue();
        String nombreResponsable = cmbResponsable.getValue();

        if (nombreInsumo == null || textoCantidad.isEmpty() || textoPeso.isEmpty()
                || motivo.isEmpty() || fecha == null || nombreResponsable == null) {
            lblError.setText("Completa Insumo, Cantidad, Peso, Motivo, Fecha y Responsable antes de guardar.");
            return;
        }

        try {
            if (modoEdicion) {
                desechoDAO.actualizar(idDesecho, motivo, fecha, txtDescripcion.getText().trim());
            } else {
                int cantidad;
                double peso;
                try {
                    cantidad = Integer.parseInt(textoCantidad);
                    peso = Double.parseDouble(textoPeso);
                } catch (NumberFormatException e) {
                    lblError.setText("Cantidad debe ser entero y Peso un número (ej. 1.5).");
                    return;
                }

                if (cantidad <= 0 || peso < 0) {
                    lblError.setText("Cantidad debe ser mayor a 0 y Peso no puede ser negativo.");
                    return;
                }

                Material material = materialesPorNombre.get(nombreInsumo);
                Usuario usuario = usuariosPorNombre.get(nombreResponsable);
                if (material == null || usuario == null) {
                    lblError.setText("Selecciona un insumo y un responsable válidos de la lista.");
                    return;
                }

                Desecho desecho = new Desecho();
                desecho.setFolio(txtFolio.getText());
                desecho.setIdMaterial(material.getIdMaterial());
                desecho.setCantidad(cantidad);
                desecho.setPeso(peso);
                desecho.setMotivo(motivo);
                desecho.setFecha(fecha);
                desecho.setIdUsuario(usuario.getIdUsuario());
                desecho.setDescripcion(txtDescripcion.getText().trim());

                desechoDAO.registrar(desecho);
            }

            lblError.setText("");
            accionVolver();

        } catch (SQLException e) {
            lblError.setText("Error al guardar en la base de datos: " + e.getMessage());
        } catch (IOException e) {
            lblError.setText("Se guardó, pero no se pudo volver al historial: " + e.getMessage());
        }
    }

    /** Limpia los campos editables del formulario. */
    @FXML
    private void accionCancelar() {
        cmbInsumo.getSelectionModel().clearSelection();
        txtCantidad.clear();
        txtPeso.clear();
        txtMotivo.clear();
        dpFecha.setValue(null);
        txtDescripcion.clear();
        lblError.setText("");
    }

    /**
     * Regresa al historial de desechos, reemplazando la escena actual.
     *
     * @throws IOException si no se puede cargar el archivo FXML del historial
     */
    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaDesecho.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Historial de Desecho");
        stage.show();
    }
}