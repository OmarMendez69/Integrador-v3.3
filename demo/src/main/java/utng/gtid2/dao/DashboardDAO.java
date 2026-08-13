package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    public int contarMateriales() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Materiales";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public int contarCriticos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Materiales WHERE estado = 'Crítico'";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public int contarTecnicos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Usuarios WHERE rol = 'Tecnico'";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public double pesoDesechoMes() throws SQLException {
        String sql = "SELECT ISNULL(SUM(peso), 0) FROM dbo.Desechos "
                + "WHERE MONTH(fecha) = MONTH(GETDATE()) AND YEAR(fecha) = YEAR(GETDATE())";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    public List<String[]> listarCriticos() throws SQLException {
        String sql = "SELECT nombre, cantidadDisponible FROM dbo.Materiales "
                + "WHERE estado = 'Crítico' ORDER BY cantidadDisponible ASC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("nombre"),
                    rs.getInt("cantidadDisponible") + " uds."
                });
            }
        }
        return lista;
    }

    public List<String[]> listarActividadReciente() throws SQLException {
        String sql = "SELECT TOP 10 descripcion, fecha FROM ("
                + "  SELECT 'Prestamo: ' + m.nombre + ' - ' + u.nombre AS descripcion, "
                + "         CAST(p.fechaPrestamo AS DATETIME) AS fecha "
                + "  FROM dbo.Prestamos p "
                + "  JOIN dbo.Materiales m ON m.idMaterial = p.idMaterial "
                + "  JOIN dbo.Usuarios u ON u.idUsuario = p.idUsuario "
                + "  UNION ALL "
                + "  SELECT 'Desecho: ' + m.nombre + ' - ' + u.nombre AS descripcion, "
                + "         CAST(d.fecha AS DATETIME) AS fecha "
                + "  FROM dbo.Desechos d "
                + "  JOIN dbo.Materiales m ON m.idMaterial = d.idMaterial "
                + "  JOIN dbo.Usuarios u ON u.idUsuario = d.idUsuario "
                + ") actividad ORDER BY fecha DESC";

        List<String[]> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("descripcion"),
                    rs.getDate("fecha").toString()
                });
            }
        }
        return lista;
    }
}