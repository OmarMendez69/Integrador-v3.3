package utng.gtid2.dao;

import org.mindrot.jbcrypt.BCrypt;
import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT idUsuario, nombre, username, rol FROM dbo.Usuarios ORDER BY nombre";

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(new Usuario(
                        rs.getInt("idUsuario"),
                        rs.getString("nombre"),
                        rs.getString("username"),
                        "",
                        rs.getString("rol")
                ));
            }
        }
        return usuarios;
    }

    public void insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO dbo.Usuarios (nombre, username, password, rol) VALUES (?, ?, ?, ?)";

        String passwordHasheada = BCrypt.hashpw(usuario.getPassword(), BCrypt.gensalt());

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getUsername());
            ps.setString(3, passwordHasheada);
            ps.setString(4, usuario.getRol());
            ps.executeUpdate();
        }
    }

    public void actualizarNombreRol(int idUsuario, String nombre, String rol) throws SQLException {
        String sql = "UPDATE dbo.Usuarios SET nombre = ?, rol = ? WHERE idUsuario = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, rol);
            ps.setInt(3, idUsuario);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idUsuario) throws SQLException {
        String sql = "DELETE FROM dbo.Usuarios WHERE idUsuario = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public boolean existeUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Usuarios WHERE username = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public Usuario verificarCredenciales(String username, String password) throws SQLException {
        String sql = "SELECT idUsuario, nombre, username, password, rol FROM dbo.Usuarios WHERE username = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashGuardado = rs.getString("password");

                    if (BCrypt.checkpw(password, hashGuardado)) {
                        return new Usuario(
                                rs.getInt("idUsuario"),
                                rs.getString("nombre"),
                                rs.getString("username"),
                                "",
                                rs.getString("rol")
                        );
                    }
                }
            }
        }
        return null;
    }
}