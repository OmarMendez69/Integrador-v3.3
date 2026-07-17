package utng.gtid2.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;" +
        "databaseName=CGTI;" +
        "encrypt=true;" +
        "trustServerCertificate=true;";

    private static final String USUARIO = "sa";
    private static final String PASSWORD = "sql123";

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