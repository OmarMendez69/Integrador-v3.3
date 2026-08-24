package utng.gtid2.jome;

import utng.gtid2.modelo.Usuario;

/**
 * Mantiene el estado del usuario que inició sesión en la aplicación.
 * <p>
 * Al ser una clase de estado estático, permite que cualquier
 * controlador consulte quién es el usuario activo y su rol sin
 * necesidad de pasar el objeto {@link Usuario} de pantalla en pantalla.
 */
public class Sesion {

    private static Usuario usuarioActual = null;

    /**
     * Guarda el usuario que acaba de iniciar sesión.
     *
     * @param usuario usuario autenticado
     */
    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    /** Cierra la sesión activa, eliminando la referencia al usuario. */
    public static void cerrar() {
        usuarioActual = null;
    }

    /**
     * @return el usuario con la sesión activa, o {@code null} si no hay
     *         ninguna sesión iniciada
     */
    public static Usuario getUsuario() {
        return usuarioActual;
    }

    /**
     * @return el nombre del usuario activo, o cadena vacía si no hay
     *         sesión iniciada
     */
    public static String getNombre() {
        return usuarioActual != null ? usuarioActual.getNombre() : "";
    }

    /**
     * @return el rol del usuario activo, o cadena vacía si no hay
     *         sesión iniciada
     */
    public static String getRol() {
        return usuarioActual != null ? usuarioActual.getRol() : "";
    }

    /** @return {@code true} si el usuario activo tiene rol Admin */
    public static boolean isAdmin() {
        return usuarioActual != null && usuarioActual.getRol().equalsIgnoreCase("Admin");
    }

    /** @return {@code true} si el usuario activo tiene rol Tecnico */
    public static boolean isTecnico() {
        return usuarioActual != null && usuarioActual.getRol().equalsIgnoreCase("Tecnico");
    }

    /** @return {@code true} si el usuario activo tiene rol Usuario */
    public static boolean isUsuario() {
        return usuarioActual != null && usuarioActual.getRol().equalsIgnoreCase("Usuario");
    }

    /** @return {@code true} si hay una sesión activa */
    public static boolean estaActiva() {
        return usuarioActual != null;
    }
}