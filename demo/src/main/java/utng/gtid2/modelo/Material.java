package utng.gtid2.modelo;

public class Material {

    private int idMaterial;
    private String nombre;
    private String categoria;
    private int cantidadTotal;
    private int cantidadDisponible;
    private String ubicacion;
    private double costoUnitario;
    private String estado;

    public Material() {
    }

    public Material(int idMaterial, String nombre, String categoria, int cantidadTotal,
                     int cantidadDisponible, String ubicacion, double costoUnitario, String estado) {
        this.idMaterial = idMaterial;
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidadTotal = cantidadTotal;
        this.cantidadDisponible = cantidadDisponible;
        this.ubicacion = ubicacion;
        this.costoUnitario = costoUnitario;
        this.estado = estado;
    }

    public int getIdMaterial() { return idMaterial; }
    public void setIdMaterial(int idMaterial) { this.idMaterial = idMaterial; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(int cantidadTotal) { this.cantidadTotal = cantidadTotal; }

    public int getCantidadDisponible() { return cantidadDisponible; }
    public void setCantidadDisponible(int cantidadDisponible) { this.cantidadDisponible = cantidadDisponible; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public double getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(double costoUnitario) { this.costoUnitario = costoUnitario; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}