package utng.gtid2.jome;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import utng.gtid2.dao.PrestamoDAO;
import utng.gtid2.modelo.Prestamo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class ListaAsignacionesController {

    @FXML private Button btnVolver;
    @FXML private Button btnRegistrarPrestamo;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Prestamo> tablaAsignaciones;
    @FXML private TableColumn<Prestamo, String> colFolio;
    @FXML private TableColumn<Prestamo, String> colInsumo;
    @FXML private TableColumn<Prestamo, String> colTecnico;
    @FXML private TableColumn<Prestamo, String> colFechaPrestamo;
    @FXML private TableColumn<Prestamo, String> colFechaDevolucion;
    @FXML private TableColumn<Prestamo, String> colEstado;
    @FXML private TableColumn<Prestamo, String> colObservaciones;
    @FXML private TableColumn<Prestamo, Void> colAccion;
    @FXML private Label lblTotalAsignaciones;
    @FXML private Label lblActivos;
    @FXML private Label lblVencidos;
    @FXML private Label lblDevueltos;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ObservableList<Prestamo> listaCompleta = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colFolio.setCellValueFactory(new PropertyValueFactory<>("folio"));
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("materialNombre"));
        colTecnico.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        colFechaPrestamo.setCellValueFactory(new PropertyValueFactory<>("fechaPrestamoTexto"));
        colFechaDevolucion.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucionTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        configurarColumnaAccion();

        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());

        cargarDatos();
    }

    private void configurarColumnaAccion() {
        colAccion.setCellFactory(col -> new TableCell<Prestamo, Void>() {
            private final Button btnDevolver = new Button("Registrar Devolución");
            private final Button btnEliminar = new Button("🗑");
            private final HBox contenedor = new HBox(6);

            {
                btnDevolver.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                contenedor.setAlignment(Pos.CENTER);

                btnDevolver.setOnAction(e -> registrarDevolucion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarPrestamo(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio) {
                    setGraphic(null);
                    return;
                }
                Prestamo prestamo = getTableView().getItems().get(getIndex());
                contenedor.getChildren().clear();
                if (!prestamo.isDevuelto()) {
                    contenedor.getChildren().add(btnDevolver);
                }
                contenedor.getChildren().add(btnEliminar);
                setGraphic(contenedor);
            }
        });
    }

    private void cargarDatos() {
        try {
            listaCompleta.setAll(prestamoDAO.listarTodos());
            aplicarFiltro();
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el historial: " + e.getMessage());
        }
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();

        ObservableList<Prestamo> filtrada = listaCompleta.stream()
                .filter(p -> texto.isEmpty()
                        || p.getFolio().toLowerCase().contains(texto)
                        || p.getMaterialNombre().toLowerCase().contains(texto)
                        || p.getUsuarioNombre().toLowerCase().contains(texto))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tablaAsignaciones.setItems(filtrada);
        actualizarContadores();
    }

    private void actualizarContadores() {
        int total = listaCompleta.size();
        long activos = listaCompleta.stream().filter(p -> "Activo".equals(p.getEstadoTexto())).count();
        long vencidos = listaCompleta.stream().filter(p -> "Vencido".equals(p.getEstadoTexto())).count();
        long devueltos = listaCompleta.stream().filter(Prestamo::isDevuelto).count();

        lblTotalAsignaciones.setText("Total: " + total + " préstamos");
        lblActivos.setText("Activos: " + activos);
        lblVencidos.setText("Vencidos: " + vencidos);
        lblDevueltos.setText("Devueltos: " + devueltos);
    }

    private void registrarDevolucion(Prestamo prestamo) {
        try {
            prestamoDAO.registrarDevolucion(prestamo.getIdPrestamo());
            cargarDatos();
        } catch (SQLException e) {
            mostrarError("No se pudo registrar la devolución: " + e.getMessage());
        }
    }

    private void eliminarPrestamo(Prestamo prestamo) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el préstamo con folio \"" + prestamo.getFolio() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    prestamoDAO.eliminar(prestamo.getIdPrestamo());
                    cargarDatos();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el préstamo: " + e.getMessage());
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

    @FXML
    private void accionAbrirPrestamo() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Prestamo.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnRegistrarPrestamo.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Registro de Préstamo");
        stage.show();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}