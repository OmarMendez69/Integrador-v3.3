package utng.gtid2.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Representa un préstamo de material técnico realizado a un usuario.
 * <p>
 * A diferencia de un POJO simple, esta clase resuelve por sí misma su
 * propio estado ({@link #getEstadoTexto()}): en vez de depender de una
 * columna guardada en la base de datos, compara la fecha de devolución
 * contra la fecha actual del sistema cada vez que se consulta, así el
 * dato siempre está actualizado sin intervención manual.
 */
public class Prestamo {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idPrestamo;

    /** Folio autogenerado del préstamo, formato F001, F002... */
    private String folio;

    private int idMaterial;
    private String materialNombre;
    private int idUsuario;
    private String usuarioNombre;
    private int cantidad;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private String observaciones;

    /** Indica si el préstamo ya fue devuelto físicamente. */
    private boolean devuelto;

    public Prestamo() {
    }

    public int getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(int idPrestamo) { this.idPrestamo = idPrestamo; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int idMaterial) { this.idMaterial = idMaterial; }

    public String getMaterialNombre() { return materialNombre; }
    public void setMaterialNombre(String materialNombre) { this.materialNombre = materialNombre; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }

    /**
     * Formatea la fecha de préstamo como dd/MM/yyyy, para mostrarla en
     * las tablas de la interfaz en lugar del formato ISO de la base.
     */
    public String getFechaPrestamoTexto() {
        return fechaPrestamo == null ? "" : fechaPrestamo.format(FORMATO);
    }

    /**
     * Formatea la fecha de devolución como dd/MM/yyyy, para mostrarla en
     * las tablas de la interfaz en lugar del formato ISO de la base.
     */
    public String getFechaDevolucionTexto() {
        return fechaDevolucion == null ? "" : fechaDevolucion.format(FORMATO);
    }

    /**
     * Calcula el estado del préstamo comparando sus propios datos contra
     * la fecha del sistema. No se guarda en la base de datos.
     *
     * @return "Devuelto" si ya fue devuelto, "Vencido" si la fecha de
     *         devolución ya pasó sin devolverse, o "Activo" en cualquier
     *         otro caso
     */
    public String getEstadoTexto() {
        if (devuelto) return "Devuelto";
        if (fechaDevolucion != null && fechaDevolucion.isBefore(LocalDate.now())) return "Vencido";
        return "Activo";
    }
}