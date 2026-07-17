module utng.gtid2.jome {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;

    opens utng.gtid2.jome to javafx.fxml;
    exports utng.gtid2.jome;

    opens utng.gtid2.modelo to javafx.base;
    exports utng.gtid2.modelo;

    exports utng.gtid2.conexion;
    exports utng.gtid2.dao;
}