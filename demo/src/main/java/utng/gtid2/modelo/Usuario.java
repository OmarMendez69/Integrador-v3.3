package utng.gtid2.modelo;

/**
 * Representa un usuario del sistema CGTI, con sus credenciales de acceso
 * y su rol dentro del área de soporte técnico.
 * <p>
 * El campo {@code password} guarda el hash generado con jBCrypt, nunca
 * la contraseña en texto plano; ese proceso vive en
 * {@code utng.gtid2.dao.UsuarioDAO}.
 */
public class Usuario {

    private int idUsuario;
    private String nombre;
    private String username;

    /** Hash de la contraseña (jBCrypt), nunca texto plano. */
    private String password;

    /** Rol del usuario: Admin, Tecnico o Usuario. */
    private String rol;

    public Usuario() {
    }

    /**
     * @param idUsuario identificador único del usuario
     * @param nombre    nombre completo del usuario
     * @param username  nombre de usuario para iniciar sesión
     * @param password  hash de la contraseña del usuario
     * @param rol       rol del usuario (Admin, Tecnico o Usuario)
     */
    public Usuario(int idUsuario, String nombre, String username, String password, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}