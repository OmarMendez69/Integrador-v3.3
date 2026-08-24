package utng.gtid2.modelo;

/**
 * Representa un material o insumo del inventario técnico de CGTI.
 * <p>
 * Es un POJO: solo encapsula el estado de un material mediante getters
 * y setters. No contiene lógica de acceso a datos ni de negocio; el
 * cálculo del estado (Disponible/Crítico) y su persistencia se manejan
 * en {@code utng.gtid2.dao.MaterialDAO}.
 */
public class Material {

    private int idMaterial;
    private String nombre;
    private String categoria;
    private int cantidadTotal;
    private int cantidadDisponible;
    private String ubicacion;
    private double costoUnitario;

    /** Estado calculado del stock: "Disponible" o "Crítico". */
    private String estado;

    public Material() {
    }

    /**
     * @param idMaterial         identificador único del material
     * @param nombre             nombre descriptivo del material
     * @param categoria          categoría del material
     * @param cantidadTotal      cantidad total registrada
     * @param cantidadDisponible cantidad actualmente disponible
     * @param ubicacion          ubicación física del material
     * @param costoUnitario      costo unitario del material
     * @param estado             estado del stock ("Disponible" o "Crítico")
     */
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