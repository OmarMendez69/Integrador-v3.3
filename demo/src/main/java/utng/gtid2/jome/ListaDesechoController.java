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

import utng.gtid2.dao.DesechoDAO;
import utng.gtid2.modelo.Desecho;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * Controlador de la pantalla de historial de Desecho.
 * <p>
 * Muestra los registros de baja de insumos por desecho, permite buscar
 * por folio/insumo/motivo, y da acceso a las acciones de agregar,
 * editar y eliminar registros.
 */
public class ListaDesechoController {

    @FXML private Button btnResultado;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Desecho> tablaDesecho;
    @FXML private TableColumn<Desecho, String> colFolio;
    @FXML private TableColumn<Desecho, String> colInsumo;
    @FXML private TableColumn<Desecho, Integer> colCantidad;
    @FXML private TableColumn<Desecho, Double> colPeso;
    @FXML private TableColumn<Desecho, String> colMotivo;
    @FXML private TableColumn<Desecho, String> colFecha;
    @FXML private TableColumn<Desecho, String> colResponsable;
    @FXML private TableColumn<Desecho, String> colDescripcion;
    @FXML private Label lblTotalDesecho;
    @FXML private Label lblPesoTotal;
    @FXML private Label lblUsuario;

    private final DesechoDAO desechoDAO = new DesechoDAO();
    private final ObservableList<Desecho> listaCompleta = FXCollections.observableArrayList();

    /**
     * Inicializa las columnas de la tabla, el listener de búsqueda y
     * carga los datos iniciales del historial de desecho.
     * Se invoca automáticamente al cargar el FXML.
     */
    @FXML
    public void initialize() {
        colFolio.setCellValueFactory(new PropertyValueFactory<>("folio"));
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("materialNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colPeso.setCellFactory(col -> new TableCell<Desecho, Double>() {
            @Override
            protected void updateItem(Double valor, boolean vacio) {
                super.updateItem(valor, vacio);
                setText(vacio || valor == null ? null : String.format("%.2f kg", valor));
            }
        });

        // Mostrar usuario y rol en sesión
        if (Sesion.estaActiva()) {
            lblUsuario.setText("👤 " + Sesion.getNombre() + " (" + Sesion.getRol() + ")");
        }

        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());

        cargarDatos();
    }

    /**
     * Carga desde la base de datos todos los registros de desecho
     * y aplica el filtro de búsqueda actual.
     */
    private void cargarDatos() {
        try {
            listaCompleta.setAll(desechoDAO.listarTodos());
            aplicarFiltro();
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el historial de desecho: " + e.getMessage());
        }
    }

    /**
     * Filtra la lista completa de registros según el texto de búsqueda
     * (folio, insumo o motivo) y actualiza la tabla y el resumen.
     */
    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();

        ObservableList<Desecho> filtrada = listaCompleta.stream()
                .filter(d -> texto.isEmpty()
                        || d.getFolio().toLowerCase().contains(texto)
                        || d.getMaterialNombre().toLowerCase().contains(texto)
                        || d.getMotivo().toLowerCase().contains(texto))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tablaDesecho.setItems(filtrada);
        actualizarResumen();
    }

    /**
     * Recalcula y muestra el total de registros y el peso acumulado
     * de desecho.
     */
    private void actualizarResumen() {
        int total = listaCompleta.size();
        double pesoTotal = listaCompleta.stream().mapToDouble(Desecho::getPeso).sum();

        lblTotalDesecho.setText("Total: " + total + " registros");
        lblPesoTotal.setText(String.format("Desecho acumulado: %.2f kg", pesoTotal));
    }

    /**
     * Abre la pantalla para registrar un nuevo desecho.
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void accionAgregar() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_RegistroDesecho.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Nuevo Registro de Desecho");
        stage.show();
    }

    /**
     * Abre la pantalla de edición precargada con el registro de desecho
     * seleccionado en la tabla. Muestra un error si no hay ninguno seleccionado.
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void accionEditar() throws IOException {
        Desecho seleccionado = tablaDesecho.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un registro de la tabla para editar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_RegistroDesecho.fxml"));
        Parent root = loader.load();

        RegistroDesechoController controller = loader.getController();
        controller.cargarDesecho(seleccionado);

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar Registro de Desecho");
        stage.show();
    }

    /**
     * Solicita confirmación y, si se acepta, elimina el registro de desecho
     * seleccionado (reponiendo el stock del insumo en el catálogo).
     */
    @FXML
    private void accionEliminar() {
        Desecho seleccionado = tablaDesecho.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un registro de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el registro \"" + seleccionado.getFolio() + "\"? El stock del insumo se repondrá en el catálogo.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    desechoDAO.eliminar(seleccionado.getIdDesecho());
                    cargarDatos();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el registro: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Regresa a la pantalla del panel principal.
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    /**
     * Muestra una alerta de advertencia con el mensaje indicado.
     *
     * @param mensaje texto a mostrar en la alerta
     */
    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}