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

    private final DesechoDAO desechoDAO = new DesechoDAO();
    private final ObservableList<Desecho> listaCompleta = FXCollections.observableArrayList();

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

        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            listaCompleta.setAll(desechoDAO.listarTodos());
            aplicarFiltro();
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el historial de desecho: " + e.getMessage());
        }
    }

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

    private void actualizarResumen() {
        int total = listaCompleta.size();
        double pesoTotal = listaCompleta.stream().mapToDouble(Desecho::getPeso).sum();

        lblTotalDesecho.setText("Total: " + total + " registros");
        lblPesoTotal.setText(String.format("Desecho acumulado: %.2f kg", pesoTotal));
    }

    @FXML
    private void accionAgregar() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_RegistroDesecho.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Nuevo Registro de Desecho");
        stage.show();
    }

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

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}