package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utng.gtid2.dao.DashboardDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PrimaryController {

    @FXML private Label lblTitulo;
    @FXML private Label lblId;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblInsumosRegistrados;
    @FXML private Label lblInsumosCriticos;
    @FXML private Label lblTecnicosActivos;
    @FXML private Label lblDesechoMes;
    @FXML private HBox hboxAlerta;
    @FXML private Label lblAlertaTexto;
    @FXML private VBox vboxListaCriticos;
    @FXML private VBox vboxActividad;
    @FXML private ScrollPane scrollCriticos;
    @FXML private ScrollPane scrollActividad;
    @FXML private Button btnAsignaciones;
    @FXML private Button btnDesecho;
    @FXML private Button btnUsuarios;
    @FXML private Button btnProveedores;
    @FXML private Button btnReportes;

    @FXML
    public void initialize() {
        if (Sesion.estaActiva()) {
            lblId.setText("Bienvenido, " + Sesion.getNombre());
            lblRolUsuario.setText(Sesion.getRol());
        }
        aplicarRestriccionesPorRol();
        cargarDashboard();
    }

    public void setUsuario(String nombreUsuario) {
        lblId.setText("Bienvenido, " + nombreUsuario);
        if (Sesion.estaActiva()) {
            lblRolUsuario.setText(Sesion.getRol());
        }
    }

    private void aplicarRestriccionesPorRol() {
        if (Sesion.isUsuario()) {
            btnAsignaciones.setVisible(false);
            btnAsignaciones.setManaged(false);
            btnDesecho.setVisible(false);
            btnDesecho.setManaged(false);
            btnUsuarios.setVisible(false);
            btnUsuarios.setManaged(false);
            btnProveedores.setVisible(false);
            btnProveedores.setManaged(false);
            btnReportes.setVisible(false);
            btnReportes.setManaged(false);

        } else if (Sesion.isTecnico()) {
            btnUsuarios.setVisible(false);
            btnUsuarios.setManaged(false);
            btnProveedores.setVisible(false);
            btnProveedores.setManaged(false);
            btnReportes.setVisible(false);
            btnReportes.setManaged(false);
        }
    }

    private void cargarDashboard() {
        try {
            DashboardDAO dao = new DashboardDAO();

            int total    = dao.contarMateriales();
            int criticos = dao.contarCriticos();
            int tecnicos = dao.contarTecnicos();
            double peso  = dao.pesoDesechoMes();

            lblInsumosRegistrados.setText(String.valueOf(total));
            lblInsumosCriticos.setText(String.valueOf(criticos));
            lblTecnicosActivos.setText(String.valueOf(tecnicos));
            lblDesechoMes.setText(String.format("%.1f kg", peso));

            if (criticos > 0) {
                lblAlertaTexto.setText("Alerta de desabasto: " + criticos + " insumo(s) en nivel critico");
                hboxAlerta.setVisible(true);
                hboxAlerta.setManaged(true);

                vboxListaCriticos.getChildren().clear();
                List<String[]> listaCriticos = dao.listarCriticos();

                for (int i = 0; i < listaCriticos.size(); i++) {
                    String[] item = listaCriticos.get(i);
                    HBox fila = crearFila(item[0], item[1], i, true);
                    vboxListaCriticos.getChildren().add(fila);
                }

                if (criticos > 3) {
                    scrollCriticos.setPrefHeight(120.0);
                    scrollCriticos.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                } else {
                    scrollCriticos.setPrefHeight(-1.0);
                    scrollCriticos.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                }

            } else {
                hboxAlerta.setVisible(false);
                hboxAlerta.setManaged(false);
            }

            vboxActividad.getChildren().clear();
            List<String[]> actividad = dao.listarActividadReciente();

            if (actividad.isEmpty()) {
                Label sinActividad = new Label("No hay actividad reciente registrada.");
                sinActividad.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF; -fx-padding: 12 0 12 0;");
                vboxActividad.getChildren().add(sinActividad);
            } else {
                for (int i = 0; i < actividad.size(); i++) {
                    String[] item = actividad.get(i);
                    HBox fila = crearFila(item[0], item[1], i, false);
                    vboxActividad.getChildren().add(fila);
                }
            }

            if (actividad.size() > 4) {
                scrollActividad.setPrefHeight(220.0);
                scrollActividad.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            } else {
                scrollActividad.setPrefHeight(-1.0);
                scrollActividad.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblInsumosRegistrados.setText("--");
            lblInsumosCriticos.setText("--");
            lblTecnicosActivos.setText("--");
            lblDesechoMes.setText("--");
        }
    }

    private HBox crearFila(String izquierda, String derecha, int indice, boolean esCritico) {
        HBox fila = new HBox();
        String bg = indice % 2 != 0 ? "-fx-background-color: #F9FAFB;" : "";
        fila.setStyle("-fx-padding: 8 12 8 12; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 1; " + bg);

        Label lblIzq = new Label(izquierda);
        lblIzq.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        HBox.setHgrow(lblIzq, Priority.ALWAYS);

        Label lblDer = new Label(derecha);
        lblDer.setStyle(esCritico
                ? "-fx-font-size: 12px; -fx-text-fill: #EF4444; -fx-font-weight: bold;"
                : "-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        fila.getChildren().addAll(lblIzq, lblDer);
        return fila;
    }

    @FXML private void irAInicio() { cargarDashboard(); }

    @FXML
    private void irACatalogo() throws IOException {
        cambiarPantalla("from_Catalogo.fxml", "Catalogo de Insumos");
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
    private void irAProveedores() throws IOException {
        cambiarPantalla("from_ListaProveedores.fxml", "Proveedores");
    }

    @FXML
    private void irAPrestamo() throws IOException {
        cambiarPantalla("from_ListaAsignaciones.fxml", "Prestamo");
    }

    @FXML
    private void irAReportes() throws IOException {
        cambiarPantalla("from_Reportes.fxml", "Reportes");
    }

    @FXML
    private void cerrarSesion() throws IOException {
        Sesion.cerrar();
        cambiarPantalla("Pantalla_Login.fxml", "Login");
    }

    private void cambiarPantalla(String nombreFxml, String tituloVentana) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(nombreFxml));
        Parent root = loader.load();

        Stage stage = (Stage) lblId.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(tituloVentana);
        stage.show();
    }
}