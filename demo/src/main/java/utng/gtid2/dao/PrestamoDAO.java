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

/**
 * Acceso a datos para la entidad {@link Prestamo}. Además del CRUD
 * básico, coordina con {@link MaterialDAO} para mantener sincronizado
 * el stock disponible del material cada vez que se presta, se devuelve
 * o se elimina un préstamo, siempre dentro de una transacción.
 */
public class PrestamoDAO {

    private final MaterialDAO materialDAO = new MaterialDAO();

    /**
     * Obtiene todos los préstamos registrados, con el nombre del material
     * y del usuario responsable ya resueltos mediante JOIN, ordenados por
     * fecha de préstamo descendente.
     *
     * @return lista de préstamos registrados
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public List<Prestamo> listarTodos() throws SQLException {
        String sql = "SELECT p.idPrestamo, p.folio, p.idMaterial, m.nombre AS materialNombre, "
                + "p.idUsuario, u.nombre AS usuarioNombre, p.cantidad, p.fechaPrestamo, p.fechaDevolucion, "
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

    /**
     * Calcula el siguiente folio disponible para un nuevo préstamo,
     * tomando el máximo id de préstamo registrado y sumándole uno.
     *
     * @return folio autogenerado con formato "F" + tres dígitos (ej. F001)
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Registra un nuevo préstamo y descuenta la cantidad prestada del
     * disponible del material, dentro de una sola transacción.
     * <p>
     * Si el ajuste de stock falla (por ejemplo, porque ya no hay
     * suficiente disponible), se revierte también la inserción del
     * préstamo mediante rollback, evitando dejar el inventario
     * inconsistente.
     *
     * @param prestamo préstamo a registrar; su cantidad se descuenta del
     *                 disponible del material asociado
     * @throws SQLException si falla la inserción o el ajuste de stock
     */
    public void registrarPrestamo(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO dbo.Prestamos (folio, idMaterial, idUsuario, cantidad, fechaPrestamo, "
                + "fechaDevolucion, observaciones, devuelto) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setString(1, prestamo.getFolio());
                    ps.setInt(2, prestamo.getIdMaterial());
                    ps.setInt(3, prestamo.getIdUsuario());
                    ps.setInt(4, prestamo.getCantidad());
                    ps.setDate(5, Date.valueOf(prestamo.getFechaPrestamo()));
                    ps.setDate(6, Date.valueOf(prestamo.getFechaDevolucion()));
                    ps.setString(7, prestamo.getObservaciones());
                    ps.executeUpdate();
                }

                materialDAO.ajustarDisponibleEnTransaccion(conexion, prestamo.getIdMaterial(), -prestamo.getCantidad());

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Marca un préstamo como devuelto y repone la cantidad prestada al
     * disponible del material, dentro de una transacción.
     * <p>
     * Si el préstamo ya estaba marcado como devuelto, no hace nada
     * adicional (evita devolver el mismo préstamo dos veces y duplicar
     * el stock repuesto).
     *
     * @param idPrestamo identificador del préstamo a marcar como devuelto
     * @throws SQLException si el préstamo no existe o falla el ajuste de stock
     */
    public void registrarDevolucion(int idPrestamo) throws SQLException {
        String sqlSelect = "SELECT idMaterial, cantidad, devuelto FROM dbo.Prestamos WHERE idPrestamo = ?";
        String sqlUpdate = "UPDATE dbo.Prestamos SET devuelto = 1 WHERE idPrestamo = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int idMaterial;
                int cantidad;
                boolean yaDevuelto;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El préstamo ya no existe.");
                        idMaterial = rs.getInt("idMaterial");
                        cantidad = rs.getInt("cantidad");
                        yaDevuelto = rs.getBoolean("devuelto");
                    }
                }

                if (!yaDevuelto) {
                    try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                        ps.setInt(1, idPrestamo);
                        ps.executeUpdate();
                    }
                    materialDAO.ajustarDisponibleEnTransaccion(conexion, idMaterial, cantidad);
                }

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Elimina un préstamo del historial. Si el préstamo no había sido
     * devuelto todavía, repone su cantidad al disponible del material
     * antes de borrarlo, dentro de la misma transacción.
     *
     * @param idPrestamo identificador del préstamo a eliminar
     * @throws SQLException si el préstamo no existe o falla el ajuste de stock
     */
    public void eliminar(int idPrestamo) throws SQLException {
        String sqlSelect = "SELECT idMaterial, cantidad, devuelto FROM dbo.Prestamos WHERE idPrestamo = ?";
        String sqlDelete = "DELETE FROM dbo.Prestamos WHERE idPrestamo = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int idMaterial;
                int cantidad;
                boolean yaDevuelto;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El préstamo ya no existe.");
                        idMaterial = rs.getInt("idMaterial");
                        cantidad = rs.getInt("cantidad");
                        yaDevuelto = rs.getBoolean("devuelto");
                    }
                }

                try (PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
                    ps.setInt(1, idPrestamo);
                    ps.executeUpdate();
                }

                if (!yaDevuelto) {
                    materialDAO.ajustarDisponibleEnTransaccion(conexion, idMaterial, cantidad);
                }

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en un objeto
     * {@link Prestamo}.
     *
     * @param rs resultado posicionado en la fila a mapear
     * @return el {@link Prestamo} construido a partir de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(rs.getInt("idPrestamo"));
        prestamo.setFolio(rs.getString("folio"));
        prestamo.setIdMaterial(rs.getInt("idMaterial"));
        prestamo.setMaterialNombre(rs.getString("materialNombre"));
        prestamo.setIdUsuario(rs.getInt("idUsuario"));
        prestamo.setUsuarioNombre(rs.getString("usuarioNombre"));
        prestamo.setCantidad(rs.getInt("cantidad"));
        prestamo.setFechaPrestamo(rs.getDate("fechaPrestamo").toLocalDate());
        prestamo.setFechaDevolucion(rs.getDate("fechaDevolucion").toLocalDate());
        prestamo.setObservaciones(rs.getString("observaciones"));
        prestamo.setDevuelto(rs.getBoolean("devuelto"));
        return prestamo;
    }
}