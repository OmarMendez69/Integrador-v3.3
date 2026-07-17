package utng.gtid2.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Desecho {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idDesecho;
    private String folio;
    private int idMaterial;
    private String materialNombre;
    private int cantidad;
    private double peso;
    private String motivo;
    private LocalDate fecha;
    private int idUsuario;
    private String usuarioNombre;
    private String descripcion;

    public Desecho() {
    }

    public int getIdDesecho() { return idDesecho; }
    public void setIdDesecho(int idDesecho) { this.idDesecho = idDesecho; }

    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int idMaterial) { this.idMaterial = idMaterial; }

    public String getMaterialNombre() { return materialNombre; }
    public void setMaterialNombre(String materialNombre) { this.materialNombre = materialNombre; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPeso() { return peso; }
    public void setPeso(double peso) { this.peso = peso; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO);
    }
}