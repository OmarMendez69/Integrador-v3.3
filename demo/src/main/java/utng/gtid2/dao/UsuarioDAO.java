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

/**
 * Acceso a datos para la entidad {@link Usuario}. Concentra tanto el
 * CRUD de usuarios como la lógica de seguridad: el hashing de
 * contraseñas con jBCrypt al registrar, y la verificación de
 * credenciales al iniciar sesión. Las contraseñas nunca se manejan ni
 * se devuelven en texto plano fuera de esta clase.
 */
public class UsuarioDAO {

    /**
     * Obtiene todos los usuarios registrados, sin incluir su contraseña
     * (el campo se devuelve vacío por seguridad).
     *
     * @return lista de usuarios registrados, ordenados por nombre
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Registra un nuevo usuario, hasheando su contraseña con jBCrypt
     * antes de guardarla. El hash incluye un salt aleatorio generado en
     * cada llamada, por lo que dos usuarios con la misma contraseña
     * nunca tendrán el mismo hash almacenado.
     *
     * @param usuario usuario a registrar; su contraseña debe venir en
     *                texto plano, se hashea dentro de este método
     * @throws SQLException si ocurre un error al insertar en la base de datos
     */
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

    /**
     * Actualiza el nombre y el rol de un usuario existente. No modifica
     * el username ni la contraseña, que quedan bloqueados en modo
     * edición desde la interfaz para no debilitar la seguridad de acceso.
     *
     * @param idUsuario identificador del usuario a actualizar
     * @param nombre    nuevo nombre del usuario
     * @param rol       nuevo rol del usuario
     * @throws SQLException si ocurre un error al actualizar en la base de datos
     */
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

    /**
     * Elimina un usuario del sistema de forma definitiva.
     *
     * @param idUsuario identificador del usuario a eliminar
     * @throws SQLException si ocurre un error al eliminar en la base de datos
     */
    public void eliminar(int idUsuario) throws SQLException {
        String sql = "DELETE FROM dbo.Usuarios WHERE idUsuario = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    /**
     * Verifica si ya existe un usuario registrado con el username dado,
     * usado en el registro para no permitir nombres de usuario duplicados.
     *
     * @param username nombre de usuario a verificar
     * @return {@code true} si ya existe un usuario con ese username
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Verifica las credenciales de inicio de sesión de un usuario,
     * comparando la contraseña ingresada contra el hash guardado
     * mediante {@code BCrypt.checkpw}, sin exponer el hash ni la
     * contraseña en el objeto devuelto.
     *
     * @param username nombre de usuario ingresado
     * @param password contraseña en texto plano ingresada
     * @return el {@link Usuario} autenticado (sin contraseña) si las
     *         credenciales son correctas, o {@code null} si el usuario
     *         no existe o la contraseña no coincide
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
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