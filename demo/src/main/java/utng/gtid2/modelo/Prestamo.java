package utng.gtid2.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Prestamo {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idPrestamo;
    private String folio;
    private int idMaterial;
    private String materialNombre;
    private int idUsuario;
    private String usuarioNombre;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private String observaciones;
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

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }

    // Para mostrar en la tabla (dd/MM/yyyy en vez de yyyy-MM-dd)
    public String getFechaPrestamoTexto() {
        return fechaPrestamo == null ? "" : fechaPrestamo.format(FORMATO);
    }

    public String getFechaDevolucionTexto() {
        return fechaDevolucion == null ? "" : fechaDevolucion.format(FORMATO);
    }

    // Estado derivado: no se guarda en BD, se calcula al vuelo
    public String getEstadoTexto() {
        if (devuelto) return "Devuelto";
        if (fechaDevolucion != null && fechaDevolucion.isBefore(LocalDate.now())) return "Vencido";
        return "Activo";
    }
}