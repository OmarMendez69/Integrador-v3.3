package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {

    private final MaterialDAO materialDAO = new MaterialDAO();

    public List<Prestamo> listarTodos() throws SQLException {
        String sql = "SELECT p.idPrestamo, p.folio, p.idMaterial, m.nombre AS materialNombre, "
                + "p.idUsuario, u.nombre AS usuarioNombre, p.fechaPrestamo, p.fechaDevolucion, "
                + "p.observaciones, p.devuelto "
                + "FROM dbo.Prestamos p "
                + "JOIN dbo.Materiales m ON m.idMaterial = p.idMaterial "
                + "JOIN dbo.Usuarios u ON u.idUsuario = p.idUsuario "
                + "ORDER BY p.fechaPrestamo DESC";

        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestamos.add(mapear(rs));
            }
        }
        return prestamos;
    }

    public String generarSiguienteFolio() throws SQLException {
        String sql = "SELECT ISNULL(MAX(idPrestamo), 0) + 1 AS siguiente FROM dbo.Prestamos";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            int siguiente = rs.getInt("siguiente");
            return "F" + String.format("%03d", siguiente);
        }
    }

    public void registrarPrestamo(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO dbo.Prestamos (folio, idMaterial, idUsuario, fechaPrestamo, "
                + "fechaDevolucion, observaciones, devuelto) VALUES (?, ?, ?, ?, ?, ?, 0)";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setString(1, prestamo.getFolio());
                    ps.setInt(2, prestamo.getIdMaterial());
                    ps.setInt(3, prestamo.getIdUsuario());
                    ps.setDate(4, Date.valueOf(prestamo.getFechaPrestamo()));
                    ps.setDate(5, Date.valueOf(prestamo.getFechaDevolucion()));
                    ps.setString(6, prestamo.getObservaciones());
                    ps.executeUpdate();
                }

                materialDAO.ajustarDisponibleEnTransaccion(conexion, prestamo.getIdMaterial(), -1);

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public void registrarDevolucion(int idPrestamo) throws SQLException {
        String sqlSelect = "SELECT idMaterial, devuelto FROM dbo.Prestamos WHERE idPrestamo = ?";
        String sqlUpdate = "UPDATE dbo.Prestamos SET devuelto = 1 WHERE idPrestamo = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int idMaterial;
                boolean yaDevuelto;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El préstamo ya no existe.");
                        idMaterial = rs.getInt("idMaterial");
                        yaDevuelto = rs.getBoolean("devuelto");
                    }
                }

                if (!yaDevuelto) {
                    try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                        ps.setInt(1, idPrestamo);
                        ps.executeUpdate();
                    }
                    materialDAO.ajustarDisponibleEnTransaccion(conexion, idMaterial, 1);
                }

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public void eliminar(int idPrestamo) throws SQLException {
        String sqlSelect = "SELECT idMaterial, devuelto FROM dbo.Prestamos WHERE idPrestamo = ?";
        String sqlDelete = "DELETE FROM dbo.Prestamos WHERE idPrestamo = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int idMaterial;
                boolean yaDevuelto;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El préstamo ya no existe.");
                        idMaterial = rs.getInt("idMaterial");
                        yaDevuelto = rs.getBoolean("devuelto");
                    }
                }

                try (PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
                    ps.setInt(1, idPrestamo);
                    ps.executeUpdate();
                }

                if (!yaDevuelto) {
                    materialDAO.ajustarDisponibleEnTransaccion(conexion, idMaterial, 1);
                }

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(rs.getInt("idPrestamo"));
        prestamo.setFolio(rs.getString("folio"));
        prestamo.setIdMaterial(rs.getInt("idMaterial"));
        prestamo.setMaterialNombre(rs.getString("materialNombre"));
        prestamo.setIdUsuario(rs.getInt("idUsuario"));
        prestamo.setUsuarioNombre(rs.getString("usuarioNombre"));
        prestamo.setFechaPrestamo(rs.getDate("fechaPrestamo").toLocalDate());
        prestamo.setFechaDevolucion(rs.getDate("fechaDevolucion").toLocalDate());
        prestamo.setObservaciones(rs.getString("observaciones"));
        prestamo.setDevuelto(rs.getBoolean("devuelto"));
        return prestamo;
    }
}