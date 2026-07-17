package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MaterialDAO {

    public List<Material> listarTodos() throws SQLException {
        String sql = "SELECT idMaterial, nombre, categoria, cantidadTotal, cantidadDisponible, "
                + "ubicacion, costoUnitario, estado FROM dbo.Materiales ORDER BY nombre";

        List<Material> materiales = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                materiales.add(mapearMaterial(rs));
            }
        }
        return materiales;
    }

    public void insertar(Material material) throws SQLException {
        String sql = "INSERT INTO dbo.Materiales (nombre, categoria, cantidadTotal, cantidadDisponible, "
                + "ubicacion, costoUnitario, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, material.getNombre());
            ps.setString(2, material.getCategoria());
            ps.setInt(3, material.getCantidadTotal());
            ps.setInt(4, material.getCantidadDisponible());
            ps.setString(5, material.getUbicacion());
            ps.setDouble(6, material.getCostoUnitario());
            ps.setString(7, material.getEstado());
            ps.executeUpdate();
        }
    }

    public void actualizar(Material material) throws SQLException {
        String sql = "UPDATE dbo.Materiales SET nombre = ?, categoria = ?, cantidadTotal = ?, "
                + "cantidadDisponible = ?, ubicacion = ?, costoUnitario = ?, estado = ? "
                + "WHERE idMaterial = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, material.getNombre());
            ps.setString(2, material.getCategoria());
            ps.setInt(3, material.getCantidadTotal());
            ps.setInt(4, material.getCantidadDisponible());
            ps.setString(5, material.getUbicacion());
            ps.setDouble(6, material.getCostoUnitario());
            ps.setString(7, material.getEstado());
            ps.setInt(8, material.getIdMaterial());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idMaterial) throws SQLException {
        String sql = "DELETE FROM dbo.Materiales WHERE idMaterial = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMaterial);
            ps.executeUpdate();
        }
    }

    private Material mapearMaterial(ResultSet rs) throws SQLException {
        return new Material(
                rs.getInt("idMaterial"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getInt("cantidadTotal"),
                rs.getInt("cantidadDisponible"),
                rs.getString("ubicacion"),
                rs.getDouble("costoUnitario"),
                rs.getString("estado")
        );
    }

    void ajustarDisponibleEnTransaccion(Connection conexion, int idMaterial, int delta) throws SQLException {
        String sqlSelect = "SELECT cantidadTotal, cantidadDisponible FROM dbo.Materiales WHERE idMaterial = ?";

        int total;
        int disponibleActual;
        try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El material seleccionado ya no existe.");
                }
                total = rs.getInt("cantidadTotal");
                disponibleActual = rs.getInt("cantidadDisponible");
            }
        }

        int nuevoDisponible = disponibleActual + delta;
        if (nuevoDisponible < 0) {
            throw new SQLException("No hay unidades disponibles de este material.");
        }
        if (nuevoDisponible > total) {
            nuevoDisponible = total;
        }
        String estado = nuevoDisponible <= 10 ? "Crítico" : "Disponible";

        String sqlUpdate = "UPDATE dbo.Materiales SET cantidadDisponible = ?, estado = ? WHERE idMaterial = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
            ps.setInt(1, nuevoDisponible);
            ps.setString(2, estado);
            ps.setInt(3, idMaterial);
            ps.executeUpdate();
        }
    }
}