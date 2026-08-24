package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para el panel principal (dashboard). Agrupa las
 * consultas de resumen que alimentan las tarjetas de estadísticas, el
 * panel de alertas críticas y la lista de actividad reciente, sin
 * mezclarse con la lógica de ningún otro módulo.
 */
public class DashboardDAO {

    /**
     * @return el total de materiales registrados en el catálogo
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public int contarMateriales() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Materiales";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * @return el total de materiales actualmente en estado Crítico
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public int contarCriticos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Materiales WHERE estado = 'Crítico'";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * @return el total de usuarios registrados con rol Tecnico
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public int contarTecnicos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Usuarios WHERE rol = 'Tecnico'";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Suma el peso de todos los materiales desechados durante el mes y
     * el año actuales, según la fecha del servidor de base de datos.
     *
     * @return peso total desechado en el mes en curso (0 si no hay registros)
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Obtiene los materiales en estado Crítico, ordenados de menor a
     * mayor cantidad disponible, para alimentar el panel de alertas.
     *
     * @return lista de pares [nombre del material, cantidad disponible + "uds."]
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Combina los últimos préstamos y desechos registrados mediante
     * UNION ALL, para mostrar en el dashboard un feed único de la
     * actividad más reciente del inventario, ordenado por fecha.
     *
     * @return lista de los 10 movimientos más recientes, cada uno como
     *         un par [descripción, fecha]
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
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