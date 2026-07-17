package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Desecho;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DesechoDAO {

    public List<Desecho> listarTodos() throws SQLException {
        String sql = "SELECT d.idDesecho, d.folio, d.idMaterial, m.nombre AS materialNombre, "
                + "d.cantidad, d.peso, d.motivo, d.fecha, d.idUsuario, u.nombre AS usuarioNombre, d.descripcion "
                + "FROM dbo.Desechos d "
                + "JOIN dbo.Materiales m ON m.idMaterial = d.idMaterial "
                + "JOIN dbo.Usuarios u ON u.idUsuario = d.idUsuario "
                + "ORDER BY d.fecha DESC";

        List<Desecho> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public String generarSiguienteFolio() throws SQLException {
        String sql = "SELECT ISNULL(MAX(idDesecho), 0) + 1 AS siguiente FROM dbo.Desechos";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int siguiente = rs.getInt("siguiente");
            return "DS-" + String.format("%04d", siguiente);
        }
    }

    public void registrar(Desecho desecho) throws SQLException {
        String sqlInsert = "INSERT INTO dbo.Desechos (folio, idMaterial, cantidad, peso, motivo, fecha, idUsuario, descripcion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexion.prepareStatement(sqlInsert)) {
                    ps.setString(1, desecho.getFolio());
                    ps.setInt(2, desecho.getIdMaterial());
                    ps.setInt(3, desecho.getCantidad());
                    ps.setDouble(4, desecho.getPeso());
                    ps.setString(5, desecho.getMotivo());
                    ps.setDate(6, Date.valueOf(desecho.getFecha()));
                    ps.setInt(7, desecho.getIdUsuario());
                    ps.setString(8, desecho.getDescripcion());
                    ps.executeUpdate();
                }

                ajustarStockPorDesecho(conexion, desecho.getIdMaterial(), -desecho.getCantidad());

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    public void actualizar(int idDesecho, String motivo, LocalDate fecha, String descripcion) throws SQLException {
        String sql = "UPDATE dbo.Desechos SET motivo = ?, fecha = ?, descripcion = ? WHERE idDesecho = ?";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, motivo);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setString(3, descripcion);
            ps.setInt(4, idDesecho);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idDesecho) throws SQLException {
        String sqlSelect = "SELECT idMaterial, cantidad FROM dbo.Desechos WHERE idDesecho = ?";
        String sqlDelete = "DELETE FROM dbo.Desechos WHERE idDesecho = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int idMaterial;
                int cantidad;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idDesecho);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El registro ya no existe.");
                        idMaterial = rs.getInt("idMaterial");
                        cantidad = rs.getInt("cantidad");
                    }
                }

                try (PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
                    ps.setInt(1, idDesecho);
                    ps.executeUpdate();
                }

                ajustarStockPorDesecho(conexion, idMaterial, cantidad);

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * delta negativo = se desecha (resta total y disponible)
     * delta positivo = se elimina el registro de desecho (repone total y disponible)
     */
    private void ajustarStockPorDesecho(Connection conexion, int idMaterial, int delta) throws SQLException {
        String sqlSelect = "SELECT cantidadTotal, cantidadDisponible FROM dbo.Materiales WHERE idMaterial = ?";

        int total;
        int disponible;
        try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("El material seleccionado ya no existe.");
                total = rs.getInt("cantidadTotal");
                disponible = rs.getInt("cantidadDisponible");
            }
        }

        int nuevoTotal = total + delta;
        int nuevoDisponible = disponible + delta;

        if (nuevoTotal < 0 || nuevoDisponible < 0) {
            throw new SQLException("No hay suficiente stock disponible para desechar esa cantidad.");
        }

        String estado = nuevoDisponible <= 10 ? "Crítico" : "Disponible";

        String sqlUpdate = "UPDATE dbo.Materiales SET cantidadTotal = ?, cantidadDisponible = ?, estado = ? WHERE idMaterial = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
            ps.setInt(1, nuevoTotal);
            ps.setInt(2, nuevoDisponible);
            ps.setString(3, estado);
            ps.setInt(4, idMaterial);
            ps.executeUpdate();
        }
    }

    private Desecho mapear(ResultSet rs) throws SQLException {
        Desecho d = new Desecho();
        d.setIdDesecho(rs.getInt("idDesecho"));
        d.setFolio(rs.getString("folio"));
        d.setIdMaterial(rs.getInt("idMaterial"));
        d.setMaterialNombre(rs.getString("materialNombre"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setPeso(rs.getDouble("peso"));
        d.setMotivo(rs.getString("motivo"));
        d.setFecha(rs.getDate("fecha").toLocalDate());
        d.setIdUsuario(rs.getInt("idUsuario"));
        d.setUsuarioNombre(rs.getString("usuarioNombre"));
        d.setDescripcion(rs.getString("descripcion"));
        return d;
    }
}