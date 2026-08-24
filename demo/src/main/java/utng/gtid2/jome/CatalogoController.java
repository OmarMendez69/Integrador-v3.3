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

import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.modelo.Material;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Controlador de la pantalla de Catálogo de Insumos.
 * <p>
 * Muestra la tabla de materiales registrados, permite buscar y filtrar
 * por categoría/estado, y da acceso a las acciones de agregar, actualizar,
 * eliminar y reabastecer materiales. Además aplica restricciones de
 * visibilidad de botones según el rol del usuario en sesión.
 */
public class CatalogoController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Material> tablaMateriales;
    @FXML private TableColumn<Material, Integer> colId;
    @FXML private TableColumn<Material, String> colNombre;
    @FXML private TableColumn<Material, String> colCategoria;
    @FXML private TableColumn<Material, Integer> colCantidadTotal;
    @FXML private TableColumn<Material, Integer> colCantidadDisponible;
    @FXML private TableColumn<Material, String> colUbicacion;
    @FXML private TableColumn<Material, Double> colCostoUnitario;
    @FXML private TableColumn<Material, String> colEstado;
    @FXML private Label lblUsuario;
    @FXML private Label lblTotalMateriales;
    @FXML private Label lblDisponibles;
    @FXML private Label lblPrestados;
    @FXML private Label lblStockBajo;
    @FXML private Label lblUltimaActualizacion;
    @FXML private Button btnVolver;
    @FXML private Button btnAgregarMaterial;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminarMaterial;
    @FXML private Button btnReabastecer;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final ObservableList<Material> listaCompleta = FXCollections.observableArrayList();

    /**
     * Inicializa la tabla, los listeners de búsqueda/filtros, aplica las
     * restricciones de rol y carga los datos iniciales del catálogo.
     * Se invoca automáticamente al cargar el FXML.
     */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idMaterial"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCantidadTotal.setCellValueFactory(new PropertyValueFactory<>("cantidadTotal"));
        colCantidadDisponible.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colCostoUnitario.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colCostoUnitario.setCellFactory(col -> new TableCell<Material, Double>() {
            @Override
            protected void updateItem(Double valor, boolean vacio) {
                super.updateItem(valor, vacio);
                setText(vacio || valor == null ? null : String.format("$%.2f", valor));
            }
        });

        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Disponible", "Crítico"));

        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());

        // Aplicar restricciones según rol
        aplicarRestriccionesPorRol();

        // Mostrar usuario en sesión
        if (Sesion.estaActiva()) {
            lblUsuario.setText("👤 " + Sesion.getNombre() + " (" + Sesion.getRol() + ")");
        }

        cargarDatos();
    }

    /**
     * Oculta los botones de administración (agregar, actualizar, eliminar,
     * reabastecer) cuando el usuario en sesión tiene rol "Usuario".
     */
    private void aplicarRestriccionesPorRol() {
        if (Sesion.isUsuario()) {
            btnAgregarMaterial.setVisible(false);
            btnAgregarMaterial.setManaged(false);
            btnActualizar.setVisible(false);
            btnActualizar.setManaged(false);
            btnEliminarMaterial.setVisible(false);
            btnEliminarMaterial.setManaged(false);
            btnReabastecer.setVisible(false);
            btnReabastecer.setManaged(false);
        }
    }

    /**
     * Establece el nombre de usuario mostrado en la etiqueta superior.
     *
     * @param nombreUsuario nombre del usuario a mostrar
     */
    public void setUsuario(String nombreUsuario) {
        lblUsuario.setText("👤 " + nombreUsuario);
    }

    /**
     * Carga desde la base de datos todos los materiales registrados,
     * llena el combo de categorías disponibles y aplica los filtros actuales.
     */
    private void cargarDatos() {
        try {
            listaCompleta.setAll(materialDAO.listarTodos());

            ObservableList<String> categorias = listaCompleta.stream()
                    .map(Material::getCategoria)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            cmbFiltroCategoria.setItems(categorias);

            aplicarFiltros();
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el catálogo: " + e.getMessage());
        }
    }

    /**
     * Filtra la lista completa de materiales según el texto de búsqueda,
     * la categoría y el estado seleccionados, y actualiza la tabla.
     */
    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String categoria = cmbFiltroCategoria.getValue();
        String estado = cmbFiltroEstado.getValue();

        ObservableList<Material> filtrada = listaCompleta.stream()
                .filter(m -> texto.isEmpty() || m.getNombre().toLowerCase().contains(texto))
                .filter(m -> categoria == null || categoria.equals(m.getCategoria()))
                .filter(m -> estado == null || estado.equals(m.getEstado()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tablaMateriales.setItems(filtrada);
        actualizarResumen();
    }

    /**
     * Recalcula y muestra los contadores del panel de resumen
     * (total, disponibles, prestados, stock bajo y última actualización).
     */
    private void actualizarResumen() {
        int total = listaCompleta.size();
        long disponibles = listaCompleta.stream().filter(m -> "Disponible".equals(m.getEstado())).count();
        long stockBajo = listaCompleta.stream().filter(m -> "Crítico".equals(m.getEstado())).count();
        int prestados = listaCompleta.stream()
                .mapToInt(m -> m.getCantidadTotal() - m.getCantidadDisponible())
                .sum();

        lblTotalMateriales.setText("Total: " + total + " materiales");
        lblDisponibles.setText("Disponibles: " + disponibles);
        lblPrestados.setText("Prestados: " + prestados);
        lblStockBajo.setText("Stock bajo: " + stockBajo);
        lblUltimaActualizacion.setText("Última actualización: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    /**
     * Abre la pantalla para registrar un nuevo material en el catálogo.
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void handleAgregarMaterial() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarProducto.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Agregar Producto");
        stage.show();
    }

    /**
     * Abre la pantalla de edición precargada con el material seleccionado
     * en la tabla. Muestra un error si no hay ningún material seleccionado.
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void handleActualizar() throws IOException {
        Material seleccionado = tablaMateriales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un material de la tabla para actualizar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarProducto.fxml"));
        Parent root = loader.load();

        AgregarProductoController controller = loader.getController();
        controller.cargarProducto(seleccionado);

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar Producto");
        stage.show();
    }

    /**
     * Abre la pantalla de baja de insumo para el material seleccionado
     * en la tabla. Muestra un error si no hay ningún material seleccionado.
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void handleEliminarMaterial() throws IOException {
        Material seleccionado = tablaMateriales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un material de la tabla para eliminar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_BajaInsumo.fxml"));
        Parent root = loader.load();

        BajaInsumoController controller = loader.getController();
        controller.cargarProducto(seleccionado.getIdMaterial(), seleccionado.getNombre(), seleccionado.getCategoria());

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Eliminar Producto");
        stage.show();
    }

    /**
     * Abre la pantalla de reabastecimiento para el material seleccionado
     * en la tabla. Muestra un error si no hay ningún material seleccionado.
     *
     * @throws IOException si ocurre un error al cargar el FXML
     */
    @FXML
    private void handleReabastecer() throws IOException {
        Material seleccionado = tablaMateriales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un material de la tabla para reabastecer.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Reabastecer.fxml"));
        Parent root = loader.load();

        ReabastecerController controller = loader.getController();
        controller.cargarMaterial(seleccionado);

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Reabastecer Material");
        stage.show();
    }

    /**
     * Limpia el texto de búsqueda y las selecciones de los combos de filtro.
     */
    @FXML
    private void handleLimpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroCategoria.getSelectionModel().clearSelection();
        cmbFiltroEstado.getSelectionModel().clearSelection();
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