package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public List<Proveedor> listarTodos() throws SQLException {
        String sql = "SELECT idProveedor, nombre, contacto, telefono, correo, estado "
                + "FROM dbo.Proveedores ORDER BY nombre";

        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                proveedores.add(mapearProveedor(rs));
            }
        }
        return proveedores;
    }

    public void insertar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO dbo.Proveedores (nombre, contacto, telefono, correo, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getContacto());
            ps.setString(3, proveedor.getTelefono());
            ps.setString(4, proveedor.getCorreo());
            ps.setString(5, proveedor.getEstado());
            ps.executeUpdate();
        }
    }

    public void actualizar(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE dbo.Proveedores SET nombre = ?, contacto = ?, telefono = ?, "
                + "correo = ?, estado = ? WHERE idProveedor = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getContacto());
            ps.setString(3, proveedor.getTelefono());
            ps.setString(4, proveedor.getCorreo());
            ps.setString(5, proveedor.getEstado());
            ps.setInt(6, proveedor.getIdProveedor());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idProveedor) throws SQLException {
        String sql = "DELETE FROM dbo.Proveedores WHERE idProveedor = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            ps.executeUpdate();
        }
    }

    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getInt("idProveedor"),
                rs.getString("nombre"),
                rs.getString("contacto"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("estado")
        );
    }
}