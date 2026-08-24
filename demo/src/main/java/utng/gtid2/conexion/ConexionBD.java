package utng.gtid2.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto único de conexión a la base de datos SQL Server del sistema CGTI.
 * <p>
 * Aísla la URL, el driver y las credenciales de conexión en una sola
 * clase, para que si en un futuro se cambia de motor de base de datos,
 * solo sea necesario modificar esta clase sin afectar a los DAO ni a
 * los controladores que dependen de ella.
 */
public class ConexionBD {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;" +
        "databaseName=CGTI;" +
        "encrypt=true;" +
        "trustServerCertificate=true;";

    private static final String USUARIO = "sa";
    private static final String PASSWORD = "sql123";

    /**
     * Abre una nueva conexión a la base de datos CGTI en SQL Server.
     *
     * @return una {@link Connection} activa, o {@code null} si ocurrió
     *         un error al cargar el driver o al conectar (el error se
     *         imprime en consola para depuración)
     */
    public static Connection conectar() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}