package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad {@link Material}: catálogo del
 * inventario técnico. Concentra todas las operaciones CRUD sobre la
 * tabla dbo.Materiales, así como los ajustes de stock que usan otros
 * DAO (préstamos, desechos) para descontar o reponer disponibilidad
 * dentro de una misma transacción.
 */
public class MaterialDAO {

    /**
     * Obtiene todos los materiales del catálogo, ordenados por nombre.
     *
     * @return lista de materiales registrados (vacía si no hay ninguno)
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Inserta un nuevo material en el catálogo.
     *
     * @param material material a registrar (el estado ya debe venir
     *                 calculado por el controlador antes de llamar este método)
     * @throws SQLException si ocurre un error al insertar en la base de datos
     */
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

    /**
     * Actualiza los datos de un material existente.
     *
     * @param material material con los datos actualizados; debe incluir
     *                 el {@code idMaterial} del registro a modificar
     * @throws SQLException si ocurre un error al actualizar en la base de datos
     */
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

    /**
     * Elimina un material del catálogo de forma definitiva (baja total).
     *
     * @param idMaterial identificador del material a eliminar
     * @throws SQLException si ocurre un error al eliminar en la base de datos
     */
    public void eliminar(int idMaterial) throws SQLException {
        String sql = "DELETE FROM dbo.Materiales WHERE idMaterial = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMaterial);
            ps.executeUpdate();
        }
    }

    /**
     * Reabastece un material existente: suma la cantidad tanto al total
     * como al disponible, y recalcula el estado (Crítico / Disponible).
     * No crea un registro nuevo, evita duplicados.
     *
     * @param idMaterial       identificador del material a reabastecer
     * @param cantidadAAgregar cantidad a sumar al stock; debe ser mayor a 0
     * @throws SQLException si la cantidad es inválida, el material ya no
     *                       existe, o falla la actualización (se hace
     *                       rollback de la transacción en ese caso)
     */
    public void reabastecer(int idMaterial, int cantidadAAgregar) throws SQLException {
        if (cantidadAAgregar <= 0) {
            throw new SQLException("La cantidad a reabastecer debe ser mayor a 0.");
        }

        String sqlSelect = "SELECT cantidadTotal, cantidadDisponible FROM dbo.Materiales WHERE idMaterial = ?";
        String sqlUpdate = "UPDATE dbo.Materiales SET cantidadTotal = ?, cantidadDisponible = ?, estado = ? "
                + "WHERE idMaterial = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int totalActual;
                int disponibleActual;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idMaterial);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("El material seleccionado ya no existe.");
                        }
                        totalActual = rs.getInt("cantidadTotal");
                        disponibleActual = rs.getInt("cantidadDisponible");
                    }
                }

                int nuevoTotal = totalActual + cantidadAAgregar;
                int nuevoDisponible = disponibleActual + cantidadAAgregar;
                String estado = nuevoDisponible <= 10 ? "Crítico" : "Disponible";

                try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, nuevoTotal);
                    ps.setInt(2, nuevoDisponible);
                    ps.setString(3, estado);
                    ps.setInt(4, idMaterial);
                    ps.executeUpdate();
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
     * {@link Material}.
     *
     * @param rs resultado posicionado en la fila a mapear
     * @return el {@link Material} construido a partir de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
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

    /**
     * Ajusta la cantidad disponible de un material dentro de una
     * transacción ya abierta por otro DAO (por ejemplo, al registrar un
     * préstamo o una devolución). No abre ni cierra la conexión, ni hace
     * commit: eso es responsabilidad de quien la invoca.
     *
     * @param conexion   conexión con la transacción ya iniciada
     * @param idMaterial identificador del material a ajustar
     * @param delta      cantidad a sumar al disponible (negativa para
     *                   descontar, positiva para reponer)
     * @throws SQLException si el material no existe o si el ajuste
     *                       dejaría el disponible en negativo
     */
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