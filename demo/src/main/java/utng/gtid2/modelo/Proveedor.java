package utng.gtid2.modelo;

public class Proveedor {

    private int idProveedor;
    private String nombre;
    private String contacto;
    private String telefono;
    private String correo;
    private String estado;

    public Proveedor() {
    }

    public Proveedor(int idProveedor, String nombre, String contacto, String telefono,
                      String correo, String estado) {
        this.idProveedor = idProveedor;
        this.nombre = nombre;
        this.contacto = contacto;
        this.telefono = telefono;
        this.correo = correo;
        this.estado = estado;
    }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
