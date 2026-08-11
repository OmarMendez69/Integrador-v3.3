package utng.gtid2.jome;

import utng.gtid2.modelo.Usuario;

public class Sesion {

    private static Usuario usuarioActual = null;

    public static void iniciar(Usuario usuario) {
        usuarioActual = usuario;
    }

    public static void cerrar() {
        usuarioActual = null;
    }

    public static Usuario getUsuario() {
        return usuarioActual;
    }

    public static String getNombre() {
        return usuarioActual != null ? usuarioActual.getNombre() : "";
    }

    public static String getRol() {
        return usuarioActual != null ? usuarioActual.getRol() : "";
    }

    public static boolean isAdmin() {
        return usuarioActual != null && usuarioActual.getRol().equalsIgnoreCase("Admin");
    }

    public static boolean isTecnico() {
        return usuarioActual != null && usuarioActual.getRol().equalsIgnoreCase("Tecnico");
    }

    public static boolean isUsuario() {
        return usuarioActual != null && usuarioActual.getRol().equalsIgnoreCase("Usuario");
    }

    public static boolean estaActiva() {
        return usuarioActual != null;
    }
}