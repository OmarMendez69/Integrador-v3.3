package utng.gtid2.jome;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada de la aplicación JavaFX del sistema CGTI.
 * <p>
 * Se encarga de crear la escena inicial (pantalla de login) y de
 * exponer {@link #setRoot(String)} para que los controladores puedan
 * cambiar de vista reutilizando la misma escena, en lugar de abrir
 * ventanas nuevas por cada pantalla.
 */
public class App extends Application {

    private static Scene scene;

    /**
     * Inicializa la ventana principal cargando la pantalla de login
     * como vista inicial y aplicando la hoja de estilos del sistema.
     *
     * @param stage ventana principal proporcionada por JavaFX
     * @throws IOException si no se puede cargar el archivo FXML del login
     */
    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Pantalla_Login"), 640, 480);
        scene.getStylesheets().add(getClass().getResource("/utng/gtid2/styles/estilos.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Cambia la vista actual de la escena principal por otra pantalla,
     * usado por los controladores para navegar entre módulos.
     *
     * @param fxml nombre del archivo FXML (sin extensión) a mostrar
     * @throws IOException si no se puede cargar el archivo FXML indicado
     */
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Carga un archivo FXML y devuelve su nodo raíz.
     *
     * @param fxml nombre del archivo FXML (sin extensión)
     * @return el nodo raíz de la vista cargada
     * @throws IOException si el archivo FXML no existe o no se puede leer
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Lanza la aplicación JavaFX.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        launch();
    }

}