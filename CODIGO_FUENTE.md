# Código fuente completo — Sistema de Control de Inventario CGTI

Este archivo contiene el código completo de la aplicación para facilitar su revisión.
Incluye las clases de **modelos**, **DAOs**, **conexión a base de datos** y **controladores**.

---

## 📁 conexion/

### `conexion/ConexionBD.java`
```java
package utng.gtid2.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Punto único de conexión a la base de datos SQL Server del sistema CGTI.
 * <p>
 * Aísla la URL, el driver y las credenciales de conexión en una sola
 * clase, para que si en un futuro se cambia de motor de base de datos,
 * solo sea necesario modificar esta clase sin afectar a los DAO ni a
 * los controladores que dependen de ella.
 */
public class ConexionBD {

    private static final String URL =
        "jdbc:sqlserver://localhost:1433;" +
        "databaseName=CGTI;" +
        "encrypt=true;" +
        "trustServerCertificate=true;";

    private static final String USUARIO = "sa";
    private static final String PASSWORD = "sql123";

    /**
     * Abre una nueva conexión a la base de datos CGTI en SQL Server.
     *
     * @return una {@link Connection} activa, o {@code null} si ocurrió
     *         un error al cargar el driver o al conectar (el error se
     *         imprime en consola para depuración)
     */
    public static Connection conectar() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
```

## 📁 modelo/

### `modelo/Desecho.java`
```java
package utng.gtid2.modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Representa la baja parcial de un material por daño o inutilización.
 * <p>
 * A diferencia de eliminar un material completo del catálogo, un
 * {@code Desecho} documenta cuánta cantidad se perdió, cuándo, por qué
 * motivo y bajo la responsabilidad de qué usuario, dejando trazabilidad
 * del movimiento en el inventario.
 */
public class Desecho {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int idDesecho;

    /** Folio autogenerado del desecho, formato DS-0001, DS-0002... */
    private String folio;

    private int idMaterial;
    private String materialNombre;
    private int cantidad;

    /** Peso total del material desechado, usado en el resumen del dashboard. */
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

    /**
     * Formatea la fecha del desecho como dd/MM/yyyy, para mostrarla en
     * las tablas de la interfaz en lugar del formato ISO de la base.
     */
    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO);
    }
}
```

### `modelo/Material.java`
```java
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
```

### `modelo/Prestamo.java`
```java
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
```

### `modelo/Proveedor.java`
```java
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

```

### `modelo/Usuario.java`
```java
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
```

## 📁 dao/

### `dao/DashboardDAO.java`
```java
package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para el panel principal (dashboard). Agrupa las
 * consultas de resumen que alimentan las tarjetas de estadísticas, el
 * panel de alertas críticas y la lista de actividad reciente, sin
 * mezclarse con la lógica de ningún otro módulo.
 */
public class DashboardDAO {

    /**
     * @return el total de materiales registrados en el catálogo
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public int contarMateriales() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Materiales";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * @return el total de materiales actualmente en estado Crítico
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public int contarCriticos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Materiales WHERE estado = 'Crítico'";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * @return el total de usuarios registrados con rol Tecnico
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public int contarTecnicos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Usuarios WHERE rol = 'Tecnico'";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    /**
     * Suma el peso de todos los materiales desechados durante el mes y
     * el año actuales, según la fecha del servidor de base de datos.
     *
     * @return peso total desechado en el mes en curso (0 si no hay registros)
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public double pesoDesechoMes() throws SQLException {
        String sql = "SELECT ISNULL(SUM(peso), 0) FROM dbo.Desechos "
                + "WHERE MONTH(fecha) = MONTH(GETDATE()) AND YEAR(fecha) = YEAR(GETDATE())";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getDouble(1);
        }
    }

    /**
     * Obtiene los materiales en estado Crítico, ordenados de menor a
     * mayor cantidad disponible, para alimentar el panel de alertas.
     *
     * @return lista de pares [nombre del material, cantidad disponible + "uds."]
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public List<String[]> listarCriticos() throws SQLException {
        String sql = "SELECT nombre, cantidadDisponible FROM dbo.Materiales "
                + "WHERE estado = 'Crítico' ORDER BY cantidadDisponible ASC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("nombre"),
                    rs.getInt("cantidadDisponible") + " uds."
                });
            }
        }
        return lista;
    }

    /**
     * Combina los últimos préstamos y desechos registrados mediante
     * UNION ALL, para mostrar en el dashboard un feed único de la
     * actividad más reciente del inventario, ordenado por fecha.
     *
     * @return lista de los 10 movimientos más recientes, cada uno como
     *         un par [descripción, fecha]
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public List<String[]> listarActividadReciente() throws SQLException {
        String sql = "SELECT TOP 10 descripcion, fecha FROM ("
                + "  SELECT 'Prestamo: ' + m.nombre + ' - ' + u.nombre AS descripcion, "
                + "         CAST(p.fechaPrestamo AS DATETIME) AS fecha "
                + "  FROM dbo.Prestamos p "
                + "  JOIN dbo.Materiales m ON m.idMaterial = p.idMaterial "
                + "  JOIN dbo.Usuarios u ON u.idUsuario = p.idUsuario "
                + "  UNION ALL "
                + "  SELECT 'Desecho: ' + m.nombre + ' - ' + u.nombre AS descripcion, "
                + "         CAST(d.fecha AS DATETIME) AS fecha "
                + "  FROM dbo.Desechos d "
                + "  JOIN dbo.Materiales m ON m.idMaterial = d.idMaterial "
                + "  JOIN dbo.Usuarios u ON u.idUsuario = d.idUsuario "
                + ") actividad ORDER BY fecha DESC";

        List<String[]> lista = new ArrayList<>();
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                    rs.getString("descripcion"),
                    rs.getDate("fecha").toString()
                });
            }
        }
        return lista;
    }
}
```

### `dao/DesechoDAO.java`
```java
package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Desecho;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad {@link Desecho}: bajas parciales de
 * material por daño. A diferencia de eliminar un material completo del
 * catálogo, este DAO descuenta tanto el total como el disponible del
 * material afectado, ya que la cantidad desechada deja de existir
 * físicamente en el inventario.
 */
public class DesechoDAO {

    /**
     * Obtiene todos los registros de desecho, con el nombre del material
     * y del usuario responsable resueltos mediante JOIN, ordenados por
     * fecha descendente.
     *
     * @return lista de registros de desecho
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public List<Desecho> listarTodos() throws SQLException {
        String sql = "SELECT d.idDesecho, d.folio, d.idMaterial, m.nombre AS materialNombre, "
                + "d.cantidad, d.peso, d.motivo, d.fecha, d.idUsuario, u.nombre AS usuarioNombre, d.descripcion "
                + "FROM dbo.Desechos d "
                + "JOIN dbo.Materiales m ON m.idMaterial = d.idMaterial "
                + "JOIN dbo.Usuarios u ON u.idUsuario = d.idUsuario "
                + "ORDER BY d.fecha DESC";

        List<Desecho> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Calcula el siguiente folio disponible para un nuevo desecho,
     * tomando el máximo id de desecho registrado y sumándole uno.
     *
     * @return folio autogenerado con formato "DS-" + cuatro dígitos (ej. DS-0001)
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public String generarSiguienteFolio() throws SQLException {
        String sql = "SELECT ISNULL(MAX(idDesecho), 0) + 1 AS siguiente FROM dbo.Desechos";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            int siguiente = rs.getInt("siguiente");
            return "DS-" + String.format("%04d", siguiente);
        }
    }

    /**
     * Registra un nuevo desecho y descuenta la cantidad dañada del
     * material afectado (total y disponible), dentro de una transacción.
     * Si el ajuste de stock falla, se revierte también la inserción.
     *
     * @param desecho registro de desecho a insertar
     * @throws SQLException si falla la inserción o el ajuste de stock
     */
    public void registrar(Desecho desecho) throws SQLException {
        String sqlInsert = "INSERT INTO dbo.Desechos (folio, idMaterial, cantidad, peso, motivo, fecha, idUsuario, descripcion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexion.prepareStatement(sqlInsert)) {
                    ps.setString(1, desecho.getFolio());
                    ps.setInt(2, desecho.getIdMaterial());
                    ps.setInt(3, desecho.getCantidad());
                    ps.setDouble(4, desecho.getPeso());
                    ps.setString(5, desecho.getMotivo());
                    ps.setDate(6, Date.valueOf(desecho.getFecha()));
                    ps.setInt(7, desecho.getIdUsuario());
                    ps.setString(8, desecho.getDescripcion());
                    ps.executeUpdate();
                }

                ajustarStockPorDesecho(conexion, desecho.getIdMaterial(), -desecho.getCantidad());

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Actualiza los datos editables de un desecho ya registrado (motivo,
     * fecha y descripción). No permite cambiar el material ni la
     * cantidad, para no descuadrar el stock ya ajustado.
     *
     * @param idDesecho   identificador del desecho a actualizar
     * @param motivo      nuevo motivo del desecho
     * @param fecha       nueva fecha del desecho
     * @param descripcion nueva descripción del desecho
     * @throws SQLException si ocurre un error al actualizar en la base de datos
     */
    public void actualizar(int idDesecho, String motivo, LocalDate fecha, String descripcion) throws SQLException {
        String sql = "UPDATE dbo.Desechos SET motivo = ?, fecha = ?, descripcion = ? WHERE idDesecho = ?";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, motivo);
            ps.setDate(2, Date.valueOf(fecha));
            ps.setString(3, descripcion);
            ps.setInt(4, idDesecho);
            ps.executeUpdate();
        }
    }

    /**
     * Elimina un registro de desecho y repone la cantidad correspondiente
     * al material afectado (total y disponible), dentro de una transacción.
     *
     * @param idDesecho identificador del desecho a eliminar
     * @throws SQLException si el registro no existe o falla el ajuste de stock
     */
    public void eliminar(int idDesecho) throws SQLException {
        String sqlSelect = "SELECT idMaterial, cantidad FROM dbo.Desechos WHERE idDesecho = ?";
        String sqlDelete = "DELETE FROM dbo.Desechos WHERE idDesecho = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int idMaterial;
                int cantidad;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idDesecho);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El registro ya no existe.");
                        idMaterial = rs.getInt("idMaterial");
                        cantidad = rs.getInt("cantidad");
                    }
                }

                try (PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
                    ps.setInt(1, idDesecho);
                    ps.executeUpdate();
                }

                ajustarStockPorDesecho(conexion, idMaterial, cantidad);

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * delta negativo = se desecha (resta total y disponible)
     * delta positivo = se elimina el registro de desecho (repone total y disponible)
     *
     * @param conexion   conexión con la transacción ya iniciada
     * @param idMaterial identificador del material a ajustar
     * @param delta      cantidad a aplicar al total y al disponible
     * @throws SQLException si el material no existe o si el ajuste
     *                       dejaría el total o el disponible en negativo
     */
    private void ajustarStockPorDesecho(Connection conexion, int idMaterial, int delta) throws SQLException {
        String sqlSelect = "SELECT cantidadTotal, cantidadDisponible FROM dbo.Materiales WHERE idMaterial = ?";

        int total;
        int disponible;
        try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("El material seleccionado ya no existe.");
                total = rs.getInt("cantidadTotal");
                disponible = rs.getInt("cantidadDisponible");
            }
        }

        int nuevoTotal = total + delta;
        int nuevoDisponible = disponible + delta;

        if (nuevoTotal < 0 || nuevoDisponible < 0) {
            throw new SQLException("No hay suficiente stock disponible para desechar esa cantidad.");
        }

        String estado = nuevoDisponible <= 10 ? "Crítico" : "Disponible";

        String sqlUpdate = "UPDATE dbo.Materiales SET cantidadTotal = ?, cantidadDisponible = ?, estado = ? WHERE idMaterial = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
            ps.setInt(1, nuevoTotal);
            ps.setInt(2, nuevoDisponible);
            ps.setString(3, estado);
            ps.setInt(4, idMaterial);
            ps.executeUpdate();
        }
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en un objeto
     * {@link Desecho}.
     *
     * @param rs resultado posicionado en la fila a mapear
     * @return el {@link Desecho} construido a partir de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Desecho mapear(ResultSet rs) throws SQLException {
        Desecho d = new Desecho();
        d.setIdDesecho(rs.getInt("idDesecho"));
        d.setFolio(rs.getString("folio"));
        d.setIdMaterial(rs.getInt("idMaterial"));
        d.setMaterialNombre(rs.getString("materialNombre"));
        d.setCantidad(rs.getInt("cantidad"));
        d.setPeso(rs.getDouble("peso"));
        d.setMotivo(rs.getString("motivo"));
        d.setFecha(rs.getDate("fecha").toLocalDate());
        d.setIdUsuario(rs.getInt("idUsuario"));
        d.setUsuarioNombre(rs.getString("usuarioNombre"));
        d.setDescripcion(rs.getString("descripcion"));
        return d;
    }
}
```

### `dao/MaterialDAO.java`
```java
package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Material;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad {@link Material}: catálogo del
 * inventario técnico. Concentra todas las operaciones CRUD sobre la
 * tabla dbo.Materiales, así como los ajustes de stock que usan otros
 * DAO (préstamos, desechos) para descontar o reponer disponibilidad
 * dentro de una misma transacción.
 */
public class MaterialDAO {

    /**
     * Obtiene todos los materiales del catálogo, ordenados por nombre.
     *
     * @return lista de materiales registrados (vacía si no hay ninguno)
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public List<Material> listarTodos() throws SQLException {
        String sql = "SELECT idMaterial, nombre, categoria, cantidadTotal, cantidadDisponible, "
                + "ubicacion, costoUnitario, estado FROM dbo.Materiales ORDER BY nombre";

        List<Material> materiales = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                materiales.add(mapearMaterial(rs));
            }
        }
        return materiales;
    }

    /**
     * Inserta un nuevo material en el catálogo.
     *
     * @param material material a registrar (el estado ya debe venir
     *                 calculado por el controlador antes de llamar este método)
     * @throws SQLException si ocurre un error al insertar en la base de datos
     */
    public void insertar(Material material) throws SQLException {
        String sql = "INSERT INTO dbo.Materiales (nombre, categoria, cantidadTotal, cantidadDisponible, "
                + "ubicacion, costoUnitario, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, material.getNombre());
            ps.setString(2, material.getCategoria());
            ps.setInt(3, material.getCantidadTotal());
            ps.setInt(4, material.getCantidadDisponible());
            ps.setString(5, material.getUbicacion());
            ps.setDouble(6, material.getCostoUnitario());
            ps.setString(7, material.getEstado());
            ps.executeUpdate();
        }
    }

    /**
     * Actualiza los datos de un material existente.
     *
     * @param material material con los datos actualizados; debe incluir
     *                 el {@code idMaterial} del registro a modificar
     * @throws SQLException si ocurre un error al actualizar en la base de datos
     */
    public void actualizar(Material material) throws SQLException {
        String sql = "UPDATE dbo.Materiales SET nombre = ?, categoria = ?, cantidadTotal = ?, "
                + "cantidadDisponible = ?, ubicacion = ?, costoUnitario = ?, estado = ? "
                + "WHERE idMaterial = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, material.getNombre());
            ps.setString(2, material.getCategoria());
            ps.setInt(3, material.getCantidadTotal());
            ps.setInt(4, material.getCantidadDisponible());
            ps.setString(5, material.getUbicacion());
            ps.setDouble(6, material.getCostoUnitario());
            ps.setString(7, material.getEstado());
            ps.setInt(8, material.getIdMaterial());
            ps.executeUpdate();
        }
    }

    /**
     * Elimina un material del catálogo de forma definitiva (baja total).
     *
     * @param idMaterial identificador del material a eliminar
     * @throws SQLException si ocurre un error al eliminar en la base de datos
     */
    public void eliminar(int idMaterial) throws SQLException {
        String sql = "DELETE FROM dbo.Materiales WHERE idMaterial = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idMaterial);
            ps.executeUpdate();
        }
    }

    /**
     * Reabastece un material existente: suma la cantidad tanto al total
     * como al disponible, y recalcula el estado (Crítico / Disponible).
     * No crea un registro nuevo, evita duplicados.
     *
     * @param idMaterial       identificador del material a reabastecer
     * @param cantidadAAgregar cantidad a sumar al stock; debe ser mayor a 0
     * @throws SQLException si la cantidad es inválida, el material ya no
     *                       existe, o falla la actualización (se hace
     *                       rollback de la transacción en ese caso)
     */
    public void reabastecer(int idMaterial, int cantidadAAgregar) throws SQLException {
        if (cantidadAAgregar <= 0) {
            throw new SQLException("La cantidad a reabastecer debe ser mayor a 0.");
        }

        String sqlSelect = "SELECT cantidadTotal, cantidadDisponible FROM dbo.Materiales WHERE idMaterial = ?";
        String sqlUpdate = "UPDATE dbo.Materiales SET cantidadTotal = ?, cantidadDisponible = ?, estado = ? "
                + "WHERE idMaterial = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int totalActual;
                int disponibleActual;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idMaterial);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("El material seleccionado ya no existe.");
                        }
                        totalActual = rs.getInt("cantidadTotal");
                        disponibleActual = rs.getInt("cantidadDisponible");
                    }
                }

                int nuevoTotal = totalActual + cantidadAAgregar;
                int nuevoDisponible = disponibleActual + cantidadAAgregar;
                String estado = nuevoDisponible <= 10 ? "Crítico" : "Disponible";

                try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                    ps.setInt(1, nuevoTotal);
                    ps.setInt(2, nuevoDisponible);
                    ps.setString(3, estado);
                    ps.setInt(4, idMaterial);
                    ps.executeUpdate();
                }

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en un objeto
     * {@link Material}.
     *
     * @param rs resultado posicionado en la fila a mapear
     * @return el {@link Material} construido a partir de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Material mapearMaterial(ResultSet rs) throws SQLException {
        return new Material(
                rs.getInt("idMaterial"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getInt("cantidadTotal"),
                rs.getInt("cantidadDisponible"),
                rs.getString("ubicacion"),
                rs.getDouble("costoUnitario"),
                rs.getString("estado")
        );
    }

    /**
     * Ajusta la cantidad disponible de un material dentro de una
     * transacción ya abierta por otro DAO (por ejemplo, al registrar un
     * préstamo o una devolución). No abre ni cierra la conexión, ni hace
     * commit: eso es responsabilidad de quien la invoca.
     *
     * @param conexion   conexión con la transacción ya iniciada
     * @param idMaterial identificador del material a ajustar
     * @param delta      cantidad a sumar al disponible (negativa para
     *                   descontar, positiva para reponer)
     * @throws SQLException si el material no existe o si el ajuste
     *                       dejaría el disponible en negativo
     */
    void ajustarDisponibleEnTransaccion(Connection conexion, int idMaterial, int delta) throws SQLException {
        String sqlSelect = "SELECT cantidadTotal, cantidadDisponible FROM dbo.Materiales WHERE idMaterial = ?";

        int total;
        int disponibleActual;
        try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
            ps.setInt(1, idMaterial);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El material seleccionado ya no existe.");
                }
                total = rs.getInt("cantidadTotal");
                disponibleActual = rs.getInt("cantidadDisponible");
            }
        }

        int nuevoDisponible = disponibleActual + delta;
        if (nuevoDisponible < 0) {
            throw new SQLException("No hay unidades disponibles de este material.");
        }
        if (nuevoDisponible > total) {
            nuevoDisponible = total;
        }
        String estado = nuevoDisponible <= 10 ? "Crítico" : "Disponible";

        String sqlUpdate = "UPDATE dbo.Materiales SET cantidadDisponible = ?, estado = ? WHERE idMaterial = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
            ps.setInt(1, nuevoDisponible);
            ps.setString(2, estado);
            ps.setInt(3, idMaterial);
            ps.executeUpdate();
        }
    }
}
```

### `dao/PrestamoDAO.java`
```java
package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Prestamo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad {@link Prestamo}. Además del CRUD
 * básico, coordina con {@link MaterialDAO} para mantener sincronizado
 * el stock disponible del material cada vez que se presta, se devuelve
 * o se elimina un préstamo, siempre dentro de una transacción.
 */
public class PrestamoDAO {

    private final MaterialDAO materialDAO = new MaterialDAO();

    /**
     * Obtiene todos los préstamos registrados, con el nombre del material
     * y del usuario responsable ya resueltos mediante JOIN, ordenados por
     * fecha de préstamo descendente.
     *
     * @return lista de préstamos registrados
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public List<Prestamo> listarTodos() throws SQLException {
        String sql = "SELECT p.idPrestamo, p.folio, p.idMaterial, m.nombre AS materialNombre, "
                + "p.idUsuario, u.nombre AS usuarioNombre, p.cantidad, p.fechaPrestamo, p.fechaDevolucion, "
                + "p.observaciones, p.devuelto "
                + "FROM dbo.Prestamos p "
                + "JOIN dbo.Materiales m ON m.idMaterial = p.idMaterial "
                + "JOIN dbo.Usuarios u ON u.idUsuario = p.idUsuario "
                + "ORDER BY p.fechaPrestamo DESC";

        List<Prestamo> prestamos = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                prestamos.add(mapear(rs));
            }
        }
        return prestamos;
    }

    /**
     * Calcula el siguiente folio disponible para un nuevo préstamo,
     * tomando el máximo id de préstamo registrado y sumándole uno.
     *
     * @return folio autogenerado con formato "F" + tres dígitos (ej. F001)
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public String generarSiguienteFolio() throws SQLException {
        String sql = "SELECT ISNULL(MAX(idPrestamo), 0) + 1 AS siguiente FROM dbo.Prestamos";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            rs.next();
            int siguiente = rs.getInt("siguiente");
            return "F" + String.format("%03d", siguiente);
        }
    }

    /**
     * Registra un nuevo préstamo y descuenta la cantidad prestada del
     * disponible del material, dentro de una sola transacción.
     * <p>
     * Si el ajuste de stock falla (por ejemplo, porque ya no hay
     * suficiente disponible), se revierte también la inserción del
     * préstamo mediante rollback, evitando dejar el inventario
     * inconsistente.
     *
     * @param prestamo préstamo a registrar; su cantidad se descuenta del
     *                 disponible del material asociado
     * @throws SQLException si falla la inserción o el ajuste de stock
     */
    public void registrarPrestamo(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO dbo.Prestamos (folio, idMaterial, idUsuario, cantidad, fechaPrestamo, "
                + "fechaDevolucion, observaciones, devuelto) VALUES (?, ?, ?, ?, ?, ?, ?, 0)";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conexion.prepareStatement(sql)) {
                    ps.setString(1, prestamo.getFolio());
                    ps.setInt(2, prestamo.getIdMaterial());
                    ps.setInt(3, prestamo.getIdUsuario());
                    ps.setInt(4, prestamo.getCantidad());
                    ps.setDate(5, Date.valueOf(prestamo.getFechaPrestamo()));
                    ps.setDate(6, Date.valueOf(prestamo.getFechaDevolucion()));
                    ps.setString(7, prestamo.getObservaciones());
                    ps.executeUpdate();
                }

                materialDAO.ajustarDisponibleEnTransaccion(conexion, prestamo.getIdMaterial(), -prestamo.getCantidad());

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Marca un préstamo como devuelto y repone la cantidad prestada al
     * disponible del material, dentro de una transacción.
     * <p>
     * Si el préstamo ya estaba marcado como devuelto, no hace nada
     * adicional (evita devolver el mismo préstamo dos veces y duplicar
     * el stock repuesto).
     *
     * @param idPrestamo identificador del préstamo a marcar como devuelto
     * @throws SQLException si el préstamo no existe o falla el ajuste de stock
     */
    public void registrarDevolucion(int idPrestamo) throws SQLException {
        String sqlSelect = "SELECT idMaterial, cantidad, devuelto FROM dbo.Prestamos WHERE idPrestamo = ?";
        String sqlUpdate = "UPDATE dbo.Prestamos SET devuelto = 1 WHERE idPrestamo = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int idMaterial;
                int cantidad;
                boolean yaDevuelto;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El préstamo ya no existe.");
                        idMaterial = rs.getInt("idMaterial");
                        cantidad = rs.getInt("cantidad");
                        yaDevuelto = rs.getBoolean("devuelto");
                    }
                }

                if (!yaDevuelto) {
                    try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                        ps.setInt(1, idPrestamo);
                        ps.executeUpdate();
                    }
                    materialDAO.ajustarDisponibleEnTransaccion(conexion, idMaterial, cantidad);
                }

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Elimina un préstamo del historial. Si el préstamo no había sido
     * devuelto todavía, repone su cantidad al disponible del material
     * antes de borrarlo, dentro de la misma transacción.
     *
     * @param idPrestamo identificador del préstamo a eliminar
     * @throws SQLException si el préstamo no existe o falla el ajuste de stock
     */
    public void eliminar(int idPrestamo) throws SQLException {
        String sqlSelect = "SELECT idMaterial, cantidad, devuelto FROM dbo.Prestamos WHERE idPrestamo = ?";
        String sqlDelete = "DELETE FROM dbo.Prestamos WHERE idPrestamo = ?";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int idMaterial;
                int cantidad;
                boolean yaDevuelto;
                try (PreparedStatement ps = conexion.prepareStatement(sqlSelect)) {
                    ps.setInt(1, idPrestamo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("El préstamo ya no existe.");
                        idMaterial = rs.getInt("idMaterial");
                        cantidad = rs.getInt("cantidad");
                        yaDevuelto = rs.getBoolean("devuelto");
                    }
                }

                try (PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
                    ps.setInt(1, idPrestamo);
                    ps.executeUpdate();
                }

                if (!yaDevuelto) {
                    materialDAO.ajustarDisponibleEnTransaccion(conexion, idMaterial, cantidad);
                }

                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        }
    }

    /**
     * Convierte la fila actual de un {@link ResultSet} en un objeto
     * {@link Prestamo}.
     *
     * @param rs resultado posicionado en la fila a mapear
     * @return el {@link Prestamo} construido a partir de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Prestamo mapear(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(rs.getInt("idPrestamo"));
        prestamo.setFolio(rs.getString("folio"));
        prestamo.setIdMaterial(rs.getInt("idMaterial"));
        prestamo.setMaterialNombre(rs.getString("materialNombre"));
        prestamo.setIdUsuario(rs.getInt("idUsuario"));
        prestamo.setUsuarioNombre(rs.getString("usuarioNombre"));
        prestamo.setCantidad(rs.getInt("cantidad"));
        prestamo.setFechaPrestamo(rs.getDate("fechaPrestamo").toLocalDate());
        prestamo.setFechaDevolucion(rs.getDate("fechaDevolucion").toLocalDate());
        prestamo.setObservaciones(rs.getString("observaciones"));
        prestamo.setDevuelto(rs.getBoolean("devuelto"));
        return prestamo;
    }
}
```

### `dao/ProveedorDAO.java`
```java
package utng.gtid2.dao;

import utng.gtid2.conexion.ConexionBD;
import utng.gtid2.modelo.Proveedor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public List<Proveedor> listarTodos() throws SQLException {
        String sql = "SELECT idProveedor, nombre, contacto, telefono, correo, estado "
                + "FROM dbo.Proveedores ORDER BY nombre";

        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                proveedores.add(mapearProveedor(rs));
            }
        }
        return proveedores;
    }

    public void insertar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO dbo.Proveedores (nombre, contacto, telefono, correo, estado) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getContacto());
            ps.setString(3, proveedor.getTelefono());
            ps.setString(4, proveedor.getCorreo());
            ps.setString(5, proveedor.getEstado());
            ps.executeUpdate();
        }
    }

    public void actualizar(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE dbo.Proveedores SET nombre = ?, contacto = ?, telefono = ?, "
                + "correo = ?, estado = ? WHERE idProveedor = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getContacto());
            ps.setString(3, proveedor.getTelefono());
            ps.setString(4, proveedor.getCorreo());
            ps.setString(5, proveedor.getEstado());
            ps.setInt(6, proveedor.getIdProveedor());
            ps.executeUpdate();
        }
    }

    public void eliminar(int idProveedor) throws SQLException {
        String sql = "DELETE FROM dbo.Proveedores WHERE idProveedor = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, idProveedor);
            ps.executeUpdate();
        }
    }

    private Proveedor mapearProveedor(ResultSet rs) throws SQLException {
        return new Proveedor(
                rs.getInt("idProveedor"),
                rs.getString("nombre"),
                rs.getString("contacto"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("estado")
        );
    }
}
```

### `dao/UsuarioDAO.java`
```java
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
```

## 📁 jome/ (controladores)

### `jome/AgregarProductoController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.modelo.Material;

import java.io.IOException;
import java.sql.SQLException;

public class AgregarProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCategoria;
    @FXML private TextField txtUbicacion;
    @FXML private TextField txtCantidadTotal;
    @FXML private TextField txtDisponible;   // deshabilitado: lo calcula el sistema
    @FXML private TextField txtCostoUnitario;
    @FXML private TextField txtEstado;       // deshabilitado: Crítico si Disponible <= 10
    @FXML private Button btnGuardar;
    @FXML private Label lblTitulo;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private boolean modoEdicion = false;
    private Material materialEnEdicion;

    public void cargarProducto(Material material) {
        modoEdicion = true;
        materialEnEdicion = material;

        txtCodigo.setText(String.valueOf(material.getIdMaterial()));
        txtNombre.setText(material.getNombre());
        txtCategoria.setText(material.getCategoria());
        txtUbicacion.setText(material.getUbicacion());
        txtCantidadTotal.setText(String.valueOf(material.getCantidadTotal()));
        txtDisponible.setText(String.valueOf(material.getCantidadDisponible()));
        txtCostoUnitario.setText(String.format("%.2f", material.getCostoUnitario()));
        txtEstado.setText(material.getEstado());

        lblTitulo.setText("Editar Producto");
        btnGuardar.setText("Actualizar");
    }

    @FXML
    private void guardarMaterial() {
        String nombre = txtNombre.getText().trim();
        String categoria = txtCategoria.getText().trim();
        String ubicacion = txtUbicacion.getText().trim();
        String textoCantidad = txtCantidadTotal.getText().trim();
        String textoCosto = txtCostoUnitario.getText().trim();

        if (nombre.isEmpty() || categoria.isEmpty() || textoCantidad.isEmpty() || textoCosto.isEmpty()) {
            mostrarAlerta("Completa nombre, categoría, cantidad total y costo unitario.");
            return;
        }

        int cantidadTotal;
        double costoUnitario;
        try {
            cantidadTotal = Integer.parseInt(textoCantidad);
            costoUnitario = Double.parseDouble(textoCosto);
        } catch (NumberFormatException e) {
            mostrarAlerta("Cantidad total debe ser entero y costo unitario un número (ej. 150.00).");
            return;
        }

        if (cantidadTotal < 0 || costoUnitario < 0) {
            mostrarAlerta("Cantidad total y costo unitario no pueden ser negativos.");
            return;
        }

        try {
            if (modoEdicion) {
                int prestados = materialEnEdicion.getCantidadTotal() - materialEnEdicion.getCantidadDisponible();
                int nuevoDisponible = Math.max(cantidadTotal - prestados, 0);
                String estado = nuevoDisponible <= 10 ? "Crítico" : "Disponible";

                Material material = new Material(materialEnEdicion.getIdMaterial(), nombre, categoria,
                        cantidadTotal, nuevoDisponible, ubicacion, costoUnitario, estado);
                materialDAO.actualizar(material);
            } else {
                String estado = cantidadTotal <= 10 ? "Crítico" : "Disponible";
                Material material = new Material(0, nombre, categoria, cantidadTotal,
                        cantidadTotal, ubicacion, costoUnitario, estado);
                materialDAO.insertar(material);
            }

            accionVolver();

        } catch (SQLException e) {
            mostrarAlerta("Error al guardar en la base de datos: " + e.getMessage());
        } catch (IOException e) {
            mostrarAlerta("Se guardó, pero no se pudo volver al catálogo: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @FXML
    private void accionCancelar() {
        txtNombre.clear();
        txtCategoria.clear();
        txtUbicacion.clear();
        txtCantidadTotal.clear();
        txtCostoUnitario.clear();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Catalogo.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) txtCodigo.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Catálogo de Insumos");
        stage.show();
    }
}
```

### `jome/AgregarProveedorController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.ProveedorDAO;
import utng.gtid2.modelo.Proveedor;

import java.io.IOException;
import java.sql.SQLException;

public class AgregarProveedorController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtContacto;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cbEstado;
    @FXML private Button btnGuardar;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private boolean modoEdicion = false;
    private int idProveedor;

    @FXML
    public void initialize() {
        cbEstado.getItems().setAll("Activo", "Inactivo");
        cbEstado.getSelectionModel().selectFirst();
    }

    public void cargarProveedor(Proveedor proveedor) {
        modoEdicion = true;
        idProveedor = proveedor.getIdProveedor();

        txtNombre.setText(proveedor.getNombre());
        txtContacto.setText(proveedor.getContacto());
        txtTelefono.setText(proveedor.getTelefono());
        txtCorreo.setText(proveedor.getCorreo());
        cbEstado.getSelectionModel().select(proveedor.getEstado());
        btnGuardar.setText("Actualizar");
        lblTitulo.setText("Editar Proveedor");
    }

    @FXML
    private void guardarProveedor() {
        String nombre = txtNombre.getText().trim();
        String contacto = txtContacto.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String estado = cbEstado.getValue();

        if (nombre.isEmpty() || contacto.isEmpty() || telefono.isEmpty() || estado == null) {
            mostrarAlerta("Completa todos los campos requeridos.");
            return;
        }

        try {
            if (modoEdicion) {
                Proveedor proveedor = new Proveedor(idProveedor, nombre, contacto, telefono, correo, estado);
                proveedorDAO.actualizar(proveedor);
            } else {
                Proveedor proveedor = new Proveedor(0, nombre, contacto, telefono, correo, estado);
                proveedorDAO.insertar(proveedor);
            }
            accionVolver();
        } catch (SQLException e) {
            mostrarAlerta("Error al guardar en la base de datos: " + e.getMessage());
        } catch (IOException e) {
            mostrarAlerta("Se guardó, pero no se pudo volver a la lista: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @FXML
    private void accionCancelar() {
        txtNombre.clear();
        txtContacto.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        cbEstado.getSelectionModel().selectFirst();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaProveedores.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Lista de Proveedores");
        stage.show();
    }
}
```

### `jome/AgregarUsuarioController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

public class AgregarUsuarioController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtUsername;
    @FXML private Label lblPassword;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Button btnGuardar;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private boolean modoEdicion = false;
    private int idUsuario;

    @FXML
    public void initialize() {
        cmbRol.setItems(FXCollections.observableArrayList("Admin", "Tecnico", "Usuario"));
    }

    public void cargarUsuario(Usuario usuario) {
        modoEdicion = true;
        idUsuario = usuario.getIdUsuario();

        txtNombre.setText(usuario.getNombre());
        txtUsername.setText(usuario.getUsername());
        cmbRol.setValue(usuario.getRol());
        btnGuardar.setText("Actualizar");
        lblTitulo.setText("Editar Usuario");

        // En modo edición no se permite tocar username ni password:
        // ambos son datos de acceso originales del primer registro.
        txtUsername.setDisable(true);
        lblPassword.setVisible(false);
        lblPassword.setManaged(false);
        txtPassword.setVisible(false);
        txtPassword.setManaged(false);
    }

    @FXML
    private void guardarUsuario() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String rol = cmbRol.getValue();

        if (nombre.isEmpty() || rol == null || rol.isEmpty() || (!modoEdicion && (username.isEmpty() || password.isEmpty()))) {
            mostrarAlerta("Completa todos los campos requeridos.");
            return;
        }

        try {
            if (modoEdicion) {
                usuarioDAO.actualizarNombreRol(idUsuario, nombre, rol);
            } else {
                Usuario usuario = new Usuario(0, nombre, username, password, rol);
                usuarioDAO.insertar(usuario);
            }
            accionVolver();
        } catch (SQLException e) {
            mostrarAlerta("Error al guardar en la base de datos: " + e.getMessage());
        } catch (IOException e) {
            mostrarAlerta("Se guardó, pero no se pudo volver a la lista: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @FXML
    private void accionCancelar() {
        txtNombre.clear();
        txtUsername.clear();
        txtPassword.clear();
        cmbRol.setValue(null);
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaUsuarios.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Lista de Usuarios");
        stage.show();
    }
}
```

### `jome/App.java`
```java
package utng.gtid2.jome;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("Pantalla_Login"), 640, 480);
        scene.getStylesheets().add(getClass().getResource("/utng/gtid2/styles/estilos.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
```

### `jome/AsignacionesController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AsignacionesController {

    @FXML
    private TextField txtFolio;

    @FXML
    private TextField txtTecAsignado;

    @FXML
    private TextField txtMaterial;

    @FXML
    private TextField txtFecha;

    @FXML
    private TextField txtObservaciones;

    @FXML
    private Button btnGuardar;

    private boolean modoEdicion = false;

    public void cargarAsignacion(String folio, String tecnico, String material, String fecha) {
        modoEdicion = true;
        txtFolio.setText(folio);
        txtTecAsignado.setText(tecnico);
        txtMaterial.setText(material);
        txtFecha.setText(fecha);
        btnGuardar.setText("Actualizar");
    }

    @FXML
    private void mostrarInformacion() {
        // por ahora no hace nada
    }

    @FXML
    private void accionCancelar() {
        txtFolio.clear();
        txtTecAsignado.clear();
        txtMaterial.clear();
        txtFecha.clear();
        txtObservaciones.clear();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaAsignaciones.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) txtFolio.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Asignaciones");
        stage.show();
    }
}
```

### `jome/BajaInsumoController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;

import java.io.IOException;
import java.sql.SQLException;

public class BajaInsumoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCategoria;
    @FXML private Button btnVolver;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private int idMaterial;

    public void cargarProducto(int idMaterial, String nombre, String categoria) {
        this.idMaterial = idMaterial;
        txtCodigo.setText(String.valueOf(idMaterial));
        txtNombre.setText(nombre);
        txtCategoria.setText(categoria);
    }

    @FXML
    private void eliminarDefinitivamente() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres eliminar \"" + txtNombre.getText() + "\"? Esta acción no se puede deshacer.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    materialDAO.eliminar(idMaterial);
                    accionVolver();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el insumo: " + e.getMessage());
                } catch (IOException e) {
                    mostrarError("Se eliminó, pero no se pudo volver al catálogo: " + e.getMessage());
                }
            }
        });
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @FXML
    private void accionCancelar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtCategoria.clear();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Catalogo.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Catálogo de Insumos");
        stage.show();
    }
}
```

### `jome/CatalogoController.java`
```java
package utng.gtid2.jome;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.modelo.Material;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class CatalogoController {

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private TableView<Material> tablaMateriales;
    @FXML private TableColumn<Material, Integer> colId;
    @FXML private TableColumn<Material, String> colNombre;
    @FXML private TableColumn<Material, String> colCategoria;
    @FXML private TableColumn<Material, Integer> colCantidadTotal;
    @FXML private TableColumn<Material, Integer> colCantidadDisponible;
    @FXML private TableColumn<Material, String> colUbicacion;
    @FXML private TableColumn<Material, Double> colCostoUnitario;
    @FXML private TableColumn<Material, String> colEstado;
    @FXML private Label lblUsuario;
    @FXML private Label lblTotalMateriales;
    @FXML private Label lblDisponibles;
    @FXML private Label lblPrestados;
    @FXML private Label lblStockBajo;
    @FXML private Label lblUltimaActualizacion;
    @FXML private Button btnVolver;
    @FXML private Button btnAgregarMaterial;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminarMaterial;
    @FXML private Button btnReabastecer;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final ObservableList<Material> listaCompleta = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idMaterial"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colCantidadTotal.setCellValueFactory(new PropertyValueFactory<>("cantidadTotal"));
        colCantidadDisponible.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colCostoUnitario.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colCostoUnitario.setCellFactory(col -> new TableCell<Material, Double>() {
            @Override
            protected void updateItem(Double valor, boolean vacio) {
                super.updateItem(valor, vacio);
                setText(vacio || valor == null ? null : String.format("$%.2f", valor));
            }
        });

        cmbFiltroEstado.setItems(FXCollections.observableArrayList("Disponible", "Crítico"));

        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, viejo, nuevo) -> aplicarFiltros());

        // Aplicar restricciones según rol
        aplicarRestriccionesPorRol();

        // Mostrar usuario en sesión
        if (Sesion.estaActiva()) {
            lblUsuario.setText("👤 " + Sesion.getNombre() + " (" + Sesion.getRol() + ")");
        }

        cargarDatos();
    }

    private void aplicarRestriccionesPorRol() {
        if (Sesion.isUsuario()) {
            btnAgregarMaterial.setVisible(false);
            btnAgregarMaterial.setManaged(false);
            btnActualizar.setVisible(false);
            btnActualizar.setManaged(false);
            btnEliminarMaterial.setVisible(false);
            btnEliminarMaterial.setManaged(false);
            btnReabastecer.setVisible(false);
            btnReabastecer.setManaged(false);
        }
    }

    public void setUsuario(String nombreUsuario) {
        lblUsuario.setText("👤 " + nombreUsuario);
    }

    private void cargarDatos() {
        try {
            listaCompleta.setAll(materialDAO.listarTodos());

            ObservableList<String> categorias = listaCompleta.stream()
                    .map(Material::getCategoria)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            cmbFiltroCategoria.setItems(categorias);

            aplicarFiltros();
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el catálogo: " + e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        String categoria = cmbFiltroCategoria.getValue();
        String estado = cmbFiltroEstado.getValue();

        ObservableList<Material> filtrada = listaCompleta.stream()
                .filter(m -> texto.isEmpty() || m.getNombre().toLowerCase().contains(texto))
                .filter(m -> categoria == null || categoria.equals(m.getCategoria()))
                .filter(m -> estado == null || estado.equals(m.getEstado()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tablaMateriales.setItems(filtrada);
        actualizarResumen();
    }

    private void actualizarResumen() {
        int total = listaCompleta.size();
        long disponibles = listaCompleta.stream().filter(m -> "Disponible".equals(m.getEstado())).count();
        long stockBajo = listaCompleta.stream().filter(m -> "Crítico".equals(m.getEstado())).count();
        int prestados = listaCompleta.stream()
                .mapToInt(m -> m.getCantidadTotal() - m.getCantidadDisponible())
                .sum();

        lblTotalMateriales.setText("Total: " + total + " materiales");
        lblDisponibles.setText("Disponibles: " + disponibles);
        lblPrestados.setText("Prestados: " + prestados);
        lblStockBajo.setText("Stock bajo: " + stockBajo);
        lblUltimaActualizacion.setText("Última actualización: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    @FXML
    private void handleAgregarMaterial() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarProducto.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Agregar Producto");
        stage.show();
    }

    @FXML
    private void handleActualizar() throws IOException {
        Material seleccionado = tablaMateriales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un material de la tabla para actualizar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarProducto.fxml"));
        Parent root = loader.load();

        AgregarProductoController controller = loader.getController();
        controller.cargarProducto(seleccionado);

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar Producto");
        stage.show();
    }

    @FXML
    private void handleEliminarMaterial() throws IOException {
        Material seleccionado = tablaMateriales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un material de la tabla para eliminar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_BajaInsumo.fxml"));
        Parent root = loader.load();

        BajaInsumoController controller = loader.getController();
        controller.cargarProducto(seleccionado.getIdMaterial(), seleccionado.getNombre(), seleccionado.getCategoria());

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Eliminar Producto");
        stage.show();
    }

    @FXML
    private void handleReabastecer() throws IOException {
        Material seleccionado = tablaMateriales.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un material de la tabla para reabastecer.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Reabastecer.fxml"));
        Parent root = loader.load();

        ReabastecerController controller = loader.getController();
        controller.cargarMaterial(seleccionado);

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Reabastecer Material");
        stage.show();
    }

    @FXML
    private void handleLimpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroCategoria.getSelectionModel().clearSelection();
        cmbFiltroEstado.getSelectionModel().clearSelection();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
```

### `jome/ListaAsignacionesController.java`
```java
package utng.gtid2.jome;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import utng.gtid2.dao.PrestamoDAO;
import utng.gtid2.modelo.Prestamo;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class ListaAsignacionesController {

    @FXML private Button btnVolver;
    @FXML private Button btnRegistrarPrestamo;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Prestamo> tablaAsignaciones;
    @FXML private TableColumn<Prestamo, String> colFolio;
    @FXML private TableColumn<Prestamo, String> colInsumo;
    @FXML private TableColumn<Prestamo, Integer> colCantidad;
    @FXML private TableColumn<Prestamo, String> colTecnico;
    @FXML private TableColumn<Prestamo, String> colFechaPrestamo;
    @FXML private TableColumn<Prestamo, String> colFechaDevolucion;
    @FXML private TableColumn<Prestamo, String> colEstado;
    @FXML private TableColumn<Prestamo, String> colObservaciones;
    @FXML private TableColumn<Prestamo, Void> colAccion;
    @FXML private Label lblTotalAsignaciones;
    @FXML private Label lblActivos;
    @FXML private Label lblVencidos;
    @FXML private Label lblDevueltos;
    @FXML private Label lblUsuario;

    private final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private final ObservableList<Prestamo> listaCompleta = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colFolio.setCellValueFactory(new PropertyValueFactory<>("folio"));
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("materialNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTecnico.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        colFechaPrestamo.setCellValueFactory(new PropertyValueFactory<>("fechaPrestamoTexto"));
        colFechaDevolucion.setCellValueFactory(new PropertyValueFactory<>("fechaDevolucionTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estadoTexto"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        configurarColumnaAccion();
        aplicarRestriccionesPorRol();

        // Mostrar usuario y rol en sesión
        if (Sesion.estaActiva()) {
            lblUsuario.setText("👤 " + Sesion.getNombre() + " (" + Sesion.getRol() + ")");
        }

        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());

        cargarDatos();
    }

    private void aplicarRestriccionesPorRol() {
        if (Sesion.isUsuario()) {
            btnRegistrarPrestamo.setVisible(false);
            btnRegistrarPrestamo.setManaged(false);
            colAccion.setVisible(false);
        }
    }

    private void configurarColumnaAccion() {
        colAccion.setCellFactory(col -> new TableCell<Prestamo, Void>() {
            private final Button btnDevolver = new Button("Registrar Devolución");
            private final Button btnEliminar = new Button("🗑");
            private final HBox contenedor = new HBox(6);

            {
                btnDevolver.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-font-size: 10px; -fx-background-radius: 4; -fx-cursor: hand;");
                contenedor.setAlignment(Pos.CENTER);

                btnDevolver.setOnAction(e -> registrarDevolucion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarPrestamo(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean vacio) {
                super.updateItem(item, vacio);
                if (vacio) {
                    setGraphic(null);
                    return;
                }

                Prestamo prestamo = getTableView().getItems().get(getIndex());
                contenedor.getChildren().clear();

                if (!prestamo.isDevuelto()) {
                    contenedor.getChildren().add(btnDevolver);
                }
                contenedor.getChildren().add(btnEliminar);
                setGraphic(contenedor);
            }
        });
    }

    private void cargarDatos() {
        try {
            listaCompleta.setAll(prestamoDAO.listarTodos());
            aplicarFiltro();
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el historial: " + e.getMessage());
        }
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();

        ObservableList<Prestamo> filtrada = listaCompleta.stream()
                .filter(p -> texto.isEmpty()
                        || p.getFolio().toLowerCase().contains(texto)
                        || p.getMaterialNombre().toLowerCase().contains(texto)
                        || p.getUsuarioNombre().toLowerCase().contains(texto))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tablaAsignaciones.setItems(filtrada);
        actualizarContadores();
    }

    private void actualizarContadores() {
        int total = listaCompleta.size();
        long activos = listaCompleta.stream().filter(p -> "Activo".equals(p.getEstadoTexto())).count();
        long vencidos = listaCompleta.stream().filter(p -> "Vencido".equals(p.getEstadoTexto())).count();
        long devueltos = listaCompleta.stream().filter(Prestamo::isDevuelto).count();

        lblTotalAsignaciones.setText("Total: " + total + " préstamos");
        lblActivos.setText("Activos: " + activos);
        lblVencidos.setText("Vencidos: " + vencidos);
        lblDevueltos.setText("Devueltos: " + devueltos);
    }

    private void registrarDevolucion(Prestamo prestamo) {
        try {
            prestamoDAO.registrarDevolucion(prestamo.getIdPrestamo());
            cargarDatos();
        } catch (SQLException e) {
            mostrarError("No se pudo registrar la devolución: " + e.getMessage());
        }
    }

    private void eliminarPrestamo(Prestamo prestamo) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el préstamo con folio \"" + prestamo.getFolio() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    prestamoDAO.eliminar(prestamo.getIdPrestamo());
                    cargarDatos();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el préstamo: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    @FXML
    private void accionAbrirPrestamo() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Prestamo.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnRegistrarPrestamo.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Registro de Préstamo");
        stage.show();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
```

### `jome/ListaDesechoController.java`
```java
package utng.gtid2.jome;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import utng.gtid2.dao.DesechoDAO;
import utng.gtid2.modelo.Desecho;

import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class ListaDesechoController {

    @FXML private Button btnResultado;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Desecho> tablaDesecho;
    @FXML private TableColumn<Desecho, String> colFolio;
    @FXML private TableColumn<Desecho, String> colInsumo;
    @FXML private TableColumn<Desecho, Integer> colCantidad;
    @FXML private TableColumn<Desecho, Double> colPeso;
    @FXML private TableColumn<Desecho, String> colMotivo;
    @FXML private TableColumn<Desecho, String> colFecha;
    @FXML private TableColumn<Desecho, String> colResponsable;
    @FXML private TableColumn<Desecho, String> colDescripcion;
    @FXML private Label lblTotalDesecho;
    @FXML private Label lblPesoTotal;
    @FXML private Label lblUsuario;

    private final DesechoDAO desechoDAO = new DesechoDAO();
    private final ObservableList<Desecho> listaCompleta = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colFolio.setCellValueFactory(new PropertyValueFactory<>("folio"));
        colInsumo.setCellValueFactory(new PropertyValueFactory<>("materialNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPeso.setCellValueFactory(new PropertyValueFactory<>("peso"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaTexto"));
        colResponsable.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colPeso.setCellFactory(col -> new TableCell<Desecho, Double>() {
            @Override
            protected void updateItem(Double valor, boolean vacio) {
                super.updateItem(valor, vacio);
                setText(vacio || valor == null ? null : String.format("%.2f kg", valor));
            }
        });

        // Mostrar usuario y rol en sesión
        if (Sesion.estaActiva()) {
            lblUsuario.setText("👤 " + Sesion.getNombre() + " (" + Sesion.getRol() + ")");
        }

        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> aplicarFiltro());

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            listaCompleta.setAll(desechoDAO.listarTodos());
            aplicarFiltro();
        } catch (SQLException e) {
            mostrarError("No se pudo cargar el historial de desecho: " + e.getMessage());
        }
    }

    private void aplicarFiltro() {
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();

        ObservableList<Desecho> filtrada = listaCompleta.stream()
                .filter(d -> texto.isEmpty()
                        || d.getFolio().toLowerCase().contains(texto)
                        || d.getMaterialNombre().toLowerCase().contains(texto)
                        || d.getMotivo().toLowerCase().contains(texto))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        tablaDesecho.setItems(filtrada);
        actualizarResumen();
    }

    private void actualizarResumen() {
        int total = listaCompleta.size();
        double pesoTotal = listaCompleta.stream().mapToDouble(Desecho::getPeso).sum();

        lblTotalDesecho.setText("Total: " + total + " registros");
        lblPesoTotal.setText(String.format("Desecho acumulado: %.2f kg", pesoTotal));
    }

    @FXML
    private void accionAgregar() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_RegistroDesecho.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Nuevo Registro de Desecho");
        stage.show();
    }

    @FXML
    private void accionEditar() throws IOException {
        Desecho seleccionado = tablaDesecho.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un registro de la tabla para editar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_RegistroDesecho.fxml"));
        Parent root = loader.load();

        RegistroDesechoController controller = loader.getController();
        controller.cargarDesecho(seleccionado);

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar Registro de Desecho");
        stage.show();
    }

    @FXML
    private void accionEliminar() {
        Desecho seleccionado = tablaDesecho.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un registro de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el registro \"" + seleccionado.getFolio() + "\"? El stock del insumo se repondrá en el catálogo.",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    desechoDAO.eliminar(seleccionado.getIdDesecho());
                    cargarDatos();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el registro: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
```

### `jome/ListaProveedoresController.java`
```java
package utng.gtid2.jome;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import utng.gtid2.dao.ProveedorDAO;
import utng.gtid2.modelo.Proveedor;

import java.io.IOException;
import java.sql.SQLException;

public class ListaProveedoresController {

    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Integer> colId;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colContacto;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEstado;
    @FXML private Button btnResultado;
    @FXML private Button btnVolver;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colContacto.setCellValueFactory(new PropertyValueFactory<>("contacto"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            ObservableList<Proveedor> proveedores = FXCollections.observableArrayList(proveedorDAO.listarTodos());
            tablaProveedores.setItems(proveedores);
        } catch (SQLException e) {
            mostrarError("No se pudo cargar la lista de proveedores: " + e.getMessage());
        }
    }

    @FXML
    private void accionAgregar() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarProveedor.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Agregar Proveedor");
        stage.show();
    }

    @FXML
    private void accionEditar() throws IOException {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un proveedor de la tabla para editar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarProveedor.fxml"));
        Parent root = loader.load();

        AgregarProveedorController controller = loader.getController();
        controller.cargarProveedor(seleccionado);

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar Proveedor");
        stage.show();
    }

    @FXML
    private void accionEliminar() {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un proveedor de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres eliminar a \"" + seleccionado.getNombre() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    proveedorDAO.eliminar(seleccionado.getIdProveedor());
                    cargarDatos();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el proveedor: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
```

### `jome/ListaUsuariosController.java`
```java
package utng.gtid2.jome;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

public class ListaUsuariosController {

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private Button btnResultado;
    @FXML private Button btnVolver;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        cargarDatos();
    }

    private void cargarDatos() {
        try {
            ObservableList<Usuario> usuarios = FXCollections.observableArrayList(usuarioDAO.listarTodos());
            tablaUsuarios.setItems(usuarios);
        } catch (SQLException e) {
            mostrarError("No se pudo cargar la lista de usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void accionAgregar() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarUsuario.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Agregar Usuario");
        stage.show();
    }

    @FXML
    private void accionEditar() throws IOException {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un usuario de la tabla para editar.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_AgregarUsuario.fxml"));
        Parent root = loader.load();

        AgregarUsuarioController controller = loader.getController();
        controller.cargarUsuario(seleccionado);

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar Usuario");
        stage.show();
    }

    @FXML
    private void accionEliminar() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un usuario de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres eliminar a \"" + seleccionado.getNombre() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);

        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                try {
                    usuarioDAO.eliminar(seleccionado.getIdUsuario());
                    cargarDatos();
                } catch (SQLException e) {
                    mostrarError("No se pudo eliminar el usuario: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnResultado.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
```

### `jome/LoginController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Label lblMensaje;

    @FXML
    private void accionLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Validación de campos vacíos
        if (username.isEmpty() || password.isEmpty()) {
            lblMensaje.setText("Por favor completa todos los campos.");
            lblMensaje.setStyle("-fx-text-fill: #ff4444;");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.verificarCredenciales(username, password);

            if (usuario == null) {
                lblMensaje.setText("Usuario o contraseña incorrectos.");
                lblMensaje.setStyle("-fx-text-fill: #ff4444;");
                return;
            }

            // Guardar sesión
            Sesion.iniciar(usuario);

            // Navegar al dashboard
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
            Parent root = loader.load();

            PrimaryController controller = loader.getController();
            controller.setUsuario(Sesion.getNombre());

            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel Principal");
            stage.show();

        } catch (SQLException e) {
            e.printStackTrace();
            lblMensaje.setText("Error de conexión con la base de datos.");
            lblMensaje.setStyle("-fx-text-fill: #ff4444;");
        } catch (IOException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al abrir el panel principal.");
            lblMensaje.setStyle("-fx-text-fill: #ff4444;");
        }
    }

    @FXML
    private void accionRegistrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("from_RegistroUsuarios.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblMensaje.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Usuario");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblMensaje.setText("Error al abrir el registro.");
            lblMensaje.setStyle("-fx-text-fill: #ff4444;");
        }
    }
}
```

### `jome/PrestamoController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.dao.PrestamoDAO;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Material;
import utng.gtid2.modelo.Prestamo;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PrestamoController implements Initializable {

    @FXML private TextField txtFolio;
    @FXML private ComboBox<String> cmbInsumo;
    @FXML private TextField txtCantidad;
    @FXML private Label lblDisponibleInfo;
    @FXML private ComboBox<String> cmbResponsable;
    @FXML private DatePicker dpFechaPrestamo;
    @FXML private DatePicker dpFechaDevolucion;
    @FXML private TextField txtObservaciones;
    @FXML private Label lblError;
    @FXML private Button btnVolver;
    @FXML private Button btnVerHistorial;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PrestamoDAO prestamoDAO = new PrestamoDAO();

    private final Map<String, Material> materialesPorNombre = new HashMap<>();
    private final Map<String, Usuario> usuariosPorNombre = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
        cargarInsumos();
        cargarResponsables();
        cargarSiguienteFolio();

        txtCantidad.setText("1");

        cmbInsumo.valueProperty().addListener((obs, viejo, nuevo) -> actualizarDisponibleInfo());
    }

    private void cargarInsumos() {
        try {
            cmbInsumo.getItems().clear();
            materialesPorNombre.clear();
            for (Material material : materialDAO.listarTodos()) {
                if (material.getCantidadDisponible() > 0) {
                    cmbInsumo.getItems().add(material.getNombre());
                    materialesPorNombre.put(material.getNombre(), material);
                }
            }
        } catch (SQLException e) {
            lblError.setText("No se pudo cargar el catálogo: " + e.getMessage());
        }
    }

    private void actualizarDisponibleInfo() {
        String nombreInsumo = cmbInsumo.getValue();
        if (nombreInsumo == null) {
            lblDisponibleInfo.setText("");
            return;
        }
        Material material = materialesPorNombre.get(nombreInsumo);
        if (material != null) {
            lblDisponibleInfo.setText("Disp: " + material.getCantidadDisponible());
        }
    }

    private void cargarResponsables() {
        try {
            cmbResponsable.getItems().clear();
            usuariosPorNombre.clear();
            for (Usuario usuario : usuarioDAO.listarTodos()) {
                cmbResponsable.getItems().add(usuario.getNombre());
                usuariosPorNombre.put(usuario.getNombre(), usuario);
            }

            if (Sesion.estaActiva()) {
                String nombreSesion = Sesion.getNombre();
                if (cmbResponsable.getItems().contains(nombreSesion)) {
                    cmbResponsable.setValue(nombreSesion);
                }
                cmbResponsable.setDisable(true);
            }

        } catch (SQLException e) {
            lblError.setText("No se pudieron cargar los usuarios: " + e.getMessage());
        }
    }

    private void cargarSiguienteFolio() {
        try {
            txtFolio.setText(prestamoDAO.generarSiguienteFolio());
        } catch (SQLException e) {
            txtFolio.setText("F001");
        }
    }

    @FXML
    private void mostrarInformacion() {
        String folio = txtFolio.getText();
        String nombreInsumo = cmbInsumo.getValue();
        String nombreResponsable = cmbResponsable.getValue();
        LocalDate fechaPrestamo = dpFechaPrestamo.getValue();
        LocalDate fechaDevolucion = dpFechaDevolucion.getValue();
        String textoCantidad = txtCantidad.getText() == null ? "" : txtCantidad.getText().trim();

        if (nombreInsumo == null || nombreResponsable == null || fechaPrestamo == null) {
            lblError.setText("Completa Insumo, Responsable y Fecha de Préstamo antes de registrar.");
            return;
        }

        if (textoCantidad.isEmpty()) {
            lblError.setText("Indica la cantidad a prestar.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(textoCantidad);
        } catch (NumberFormatException e) {
            lblError.setText("La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            lblError.setText("La cantidad debe ser mayor a 0.");
            return;
        }

        Material material = materialesPorNombre.get(nombreInsumo);
        Usuario usuario = usuariosPorNombre.get(nombreResponsable);

        if (material == null || usuario == null) {
            lblError.setText("Selecciona un insumo y un responsable válidos de la lista.");
            return;
        }

        if (cantidad > material.getCantidadDisponible()) {
            lblError.setText("Solo hay " + material.getCantidadDisponible() + " unidades disponibles de \""
                    + material.getNombre() + "\".");
            return;
        }

        if (fechaDevolucion == null) {
            fechaDevolucion = fechaPrestamo.plusDays(7);
        }

        Prestamo prestamo = new Prestamo();
        prestamo.setFolio(folio);
        prestamo.setIdMaterial(material.getIdMaterial());
        prestamo.setIdUsuario(usuario.getIdUsuario());
        prestamo.setCantidad(cantidad);
        prestamo.setFechaPrestamo(fechaPrestamo);
        prestamo.setFechaDevolucion(fechaDevolucion);
        prestamo.setObservaciones(txtObservaciones.getText().trim());

        try {
            prestamoDAO.registrarPrestamo(prestamo);
            lblError.setText("");

            Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
            confirmacion.setTitle("Préstamo registrado");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("Préstamo de " + cantidad + " unidad(es) de \"" + nombreInsumo + "\" registrado correctamente.");
            confirmacion.showAndWait();

            accionCancelar();
            cargarInsumos();
            cargarSiguienteFolio();

        } catch (SQLException e) {
            lblError.setText("Error al registrar el préstamo: " + e.getMessage());
        }
    }

    @FXML
    private void accionCancelar() {
        cmbInsumo.getSelectionModel().clearSelection();
        txtCantidad.setText("1");
        lblDisponibleInfo.setText("");
        dpFechaPrestamo.setValue(null);
        dpFechaDevolucion.setValue(null);
        txtObservaciones.clear();
        lblError.setText("");
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }

    @FXML
    private void accionVerHistorial() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaAsignaciones.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVerHistorial.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Historial de Préstamos");
        stage.show();
    }
}
```

### `jome/PrimaryController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utng.gtid2.dao.DashboardDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class PrimaryController {

    @FXML private Label lblTitulo;
    @FXML private Label lblId;
    @FXML private Label lblRolUsuario;
    @FXML private Label lblInsumosRegistrados;
    @FXML private Label lblInsumosCriticos;
    @FXML private Label lblTecnicosActivos;
    @FXML private Label lblDesechoMes;
    @FXML private HBox hboxAlerta;
    @FXML private Label lblAlertaTexto;
    @FXML private VBox vboxListaCriticos;
    @FXML private VBox vboxActividad;
    @FXML private ScrollPane scrollCriticos;
    @FXML private ScrollPane scrollActividad;
    @FXML private Button btnAsignaciones;
    @FXML private Button btnDesecho;
    @FXML private Button btnUsuarios;
    @FXML private Button btnProveedores;
    @FXML private Button btnReportes;

    @FXML
    public void initialize() {
        if (Sesion.estaActiva()) {
            lblId.setText("Bienvenido, " + Sesion.getNombre());
            lblRolUsuario.setText(Sesion.getRol());
        }
        aplicarRestriccionesPorRol();
        cargarDashboard();
    }

    public void setUsuario(String nombreUsuario) {
        lblId.setText("Bienvenido, " + nombreUsuario);
        if (Sesion.estaActiva()) {
            lblRolUsuario.setText(Sesion.getRol());
        }
    }

    private void aplicarRestriccionesPorRol() {
        if (Sesion.isUsuario()) {
            btnAsignaciones.setVisible(false);
            btnAsignaciones.setManaged(false);
            btnDesecho.setVisible(false);
            btnDesecho.setManaged(false);
            btnUsuarios.setVisible(false);
            btnUsuarios.setManaged(false);
            btnProveedores.setVisible(false);
            btnProveedores.setManaged(false);
            btnReportes.setVisible(false);
            btnReportes.setManaged(false);

        } else if (Sesion.isTecnico()) {
            btnUsuarios.setVisible(false);
            btnUsuarios.setManaged(false);
            btnProveedores.setVisible(false);
            btnProveedores.setManaged(false);
            btnReportes.setVisible(false);
            btnReportes.setManaged(false);
        }
    }

    private void cargarDashboard() {
        try {
            DashboardDAO dao = new DashboardDAO();

            int total    = dao.contarMateriales();
            int criticos = dao.contarCriticos();
            int tecnicos = dao.contarTecnicos();
            double peso  = dao.pesoDesechoMes();

            lblInsumosRegistrados.setText(String.valueOf(total));
            lblInsumosCriticos.setText(String.valueOf(criticos));
            lblTecnicosActivos.setText(String.valueOf(tecnicos));
            lblDesechoMes.setText(String.format("%.1f kg", peso));

            if (criticos > 0) {
                lblAlertaTexto.setText("Alerta de desabasto: " + criticos + " insumo(s) en nivel critico");
                hboxAlerta.setVisible(true);
                hboxAlerta.setManaged(true);

                vboxListaCriticos.getChildren().clear();
                List<String[]> listaCriticos = dao.listarCriticos();

                for (int i = 0; i < listaCriticos.size(); i++) {
                    String[] item = listaCriticos.get(i);
                    HBox fila = crearFila(item[0], item[1], i, true);
                    vboxListaCriticos.getChildren().add(fila);
                }

                if (criticos > 3) {
                    scrollCriticos.setPrefHeight(120.0);
                    scrollCriticos.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                } else {
                    scrollCriticos.setPrefHeight(-1.0);
                    scrollCriticos.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                }

            } else {
                hboxAlerta.setVisible(false);
                hboxAlerta.setManaged(false);
            }

            vboxActividad.getChildren().clear();
            List<String[]> actividad = dao.listarActividadReciente();

            if (actividad.isEmpty()) {
                Label sinActividad = new Label("No hay actividad reciente registrada.");
                sinActividad.setStyle("-fx-font-size: 12px; -fx-text-fill: #9CA3AF; -fx-padding: 12 0 12 0;");
                vboxActividad.getChildren().add(sinActividad);
            } else {
                for (int i = 0; i < actividad.size(); i++) {
                    String[] item = actividad.get(i);
                    HBox fila = crearFila(item[0], item[1], i, false);
                    vboxActividad.getChildren().add(fila);
                }
            }

            if (actividad.size() > 4) {
                scrollActividad.setPrefHeight(220.0);
                scrollActividad.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            } else {
                scrollActividad.setPrefHeight(-1.0);
                scrollActividad.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            lblInsumosRegistrados.setText("--");
            lblInsumosCriticos.setText("--");
            lblTecnicosActivos.setText("--");
            lblDesechoMes.setText("--");
        }
    }

    private HBox crearFila(String izquierda, String derecha, int indice, boolean esCritico) {
        HBox fila = new HBox();
        String bg = indice % 2 != 0 ? "-fx-background-color: #F9FAFB;" : "";
        fila.setStyle("-fx-padding: 8 12 8 12; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 1; " + bg);

        Label lblIzq = new Label(izquierda);
        lblIzq.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        HBox.setHgrow(lblIzq, Priority.ALWAYS);

        Label lblDer = new Label(derecha);
        lblDer.setStyle(esCritico
                ? "-fx-font-size: 12px; -fx-text-fill: #EF4444; -fx-font-weight: bold;"
                : "-fx-font-size: 12px; -fx-text-fill: #9CA3AF;");

        fila.getChildren().addAll(lblIzq, lblDer);
        return fila;
    }

    @FXML private void irAInicio() { cargarDashboard(); }

    @FXML
    private void irACatalogo() throws IOException {
        cambiarPantalla("from_Catalogo.fxml", "Catalogo de Insumos");
    }

    @FXML
    private void irAAsignaciones() throws IOException {
        cambiarPantalla("from_ListaAsignaciones.fxml", "Asignaciones");
    }

    @FXML
    private void irADesecho() throws IOException {
        cambiarPantalla("from_ListaDesecho.fxml", "Registro de Desecho");
    }

    @FXML
    private void irAUsuarios() throws IOException {
        cambiarPantalla("from_ListaUsuarios.fxml", "Usuarios");
    }

    @FXML
    private void irAProveedores() throws IOException {
        cambiarPantalla("from_ListaProveedores.fxml", "Proveedores");
    }

    @FXML
    private void irAPrestamo() throws IOException {
        cambiarPantalla("from_Prestamo.fxml", "Prestamo");
    }

    @FXML
    private void irAReportes() throws IOException {
        cambiarPantalla("from_Reportes.fxml", "Reportes");
    }

    @FXML
    private void cerrarSesion() throws IOException {
        Sesion.cerrar();
        cambiarPantalla("Pantalla_Login.fxml", "Login");
    }

    private void cambiarPantalla(String nombreFxml, String tituloVentana) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(nombreFxml));
        Parent root = loader.load();

        Stage stage = (Stage) lblId.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(tituloVentana);
        stage.show();
    }
}
```

### `jome/ReabastecerController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.modelo.Material;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReabastecerController {

    @FXML private Label lblNombreMaterial;
    @FXML private Label lblDisponibleActual;
    @FXML private Label lblTotalActual;
    @FXML private Label lblEstadoActual;

    @FXML private TextField txtCantidadAAgregar;
    @FXML private DatePicker dpFechaEntrada;
    @FXML private TextArea txtObservaciones;
    @FXML private Label lblPreview;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final MaterialDAO materialDAO = new MaterialDAO();
    private Material materialSeleccionado;

    public void cargarMaterial(Material material) {
        this.materialSeleccionado = material;

        lblNombreMaterial.setText(material.getNombre());
        lblDisponibleActual.setText(String.valueOf(material.getCantidadDisponible()));
        lblTotalActual.setText(String.valueOf(material.getCantidadTotal()));
        lblEstadoActual.setText(material.getEstado());
        lblEstadoActual.setStyle("-fx-font-size: 14px; -fx-font-weight: BOLD; -fx-text-fill: "
                + ("Crítico".equals(material.getEstado()) ? "#C0392B" : "#2E7D32") + ";");

        dpFechaEntrada.setValue(LocalDate.now());

        txtCantidadAAgregar.textProperty().addListener((obs, viejo, nuevo) -> actualizarPreview());
        actualizarPreview();
    }

    private void actualizarPreview() {
        if (materialSeleccionado == null) return;

        String texto = txtCantidadAAgregar.getText() == null ? "" : txtCantidadAAgregar.getText().trim();
        if (texto.isEmpty()) {
            lblPreview.setText("");
            return;
        }

        try {
            int cantidad = Integer.parseInt(texto);
            if (cantidad <= 0) {
                lblPreview.setText("");
                return;
            }
            int nuevoDisponible = materialSeleccionado.getCantidadDisponible() + cantidad;
            int nuevoTotal = materialSeleccionado.getCantidadTotal() + cantidad;
            lblPreview.setText("Nuevo stock: " + nuevoDisponible + " disponibles de " + nuevoTotal + " en total.");
        } catch (NumberFormatException e) {
            lblPreview.setText("");
        }
    }

    @FXML
    private void guardarReabastecimiento() {
        lblError.setText("");

        if (materialSeleccionado == null) {
            lblError.setText("No se seleccionó ningún material.");
            return;
        }

        String textoCantidad = txtCantidadAAgregar.getText() == null ? "" : txtCantidadAAgregar.getText().trim();
        if (textoCantidad.isEmpty()) {
            lblError.setText("Indica la cantidad a añadir.");
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(textoCantidad);
        } catch (NumberFormatException e) {
            lblError.setText("La cantidad debe ser un número entero.");
            return;
        }

        if (cantidad <= 0) {
            lblError.setText("La cantidad debe ser mayor a 0.");
            return;
        }

        if (dpFechaEntrada.getValue() == null) {
            lblError.setText("Selecciona la fecha de entrada.");
            return;
        }

        try {
            materialDAO.reabastecer(materialSeleccionado.getIdMaterial(), cantidad);

            Alert confirmacion = new Alert(Alert.AlertType.INFORMATION);
            confirmacion.setTitle("Reabastecimiento registrado");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("Se añadieron " + cantidad + " unidades a \""
                    + materialSeleccionado.getNombre() + "\".");
            confirmacion.showAndWait();

            accionVolver();

        } catch (SQLException e) {
            lblError.setText("Error al reabastecer: " + e.getMessage());
        } catch (IOException e) {
            lblError.setText("Se reabasteció, pero no se pudo volver al catálogo: " + e.getMessage());
        }
    }

    @FXML
    private void accionCancelar() {
        try {
            accionVolver();
        } catch (IOException e) {
            lblError.setText("No se pudo volver al catálogo: " + e.getMessage());
        }
    }

    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_Catalogo.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Gestión de Materiales");
        stage.show();
    }
}
```

### `jome/RegistroDesechoController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import utng.gtid2.dao.DesechoDAO;
import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Desecho;
import utng.gtid2.modelo.Material;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class RegistroDesechoController implements Initializable {

    @FXML private TextField txtFolio;
    @FXML private ComboBox<String> cmbInsumo;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPeso;
    @FXML private TextField txtMotivo;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cmbResponsable;
    @FXML private TextArea txtDescripcion;
    @FXML private Label lblError;
    @FXML private Button btnGuardar;
    @FXML private Button btnVolver;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MaterialDAO materialDAO = new MaterialDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final DesechoDAO desechoDAO = new DesechoDAO();

    private final Map<String, Material> materialesPorNombre = new HashMap<>();
    private final Map<String, Usuario> usuariosPorNombre = new HashMap<>();

    private boolean modoEdicion = false;
    private int idDesecho;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
        cargarInsumos();
        cargarResponsables();
        cargarSiguienteFolio();
    }

    private void cargarInsumos() {
        try {
            cmbInsumo.getItems().clear();
            materialesPorNombre.clear();
            for (Material material : materialDAO.listarTodos()) {
                if (material.getCantidadDisponible() > 0) {
                    cmbInsumo.getItems().add(material.getNombre());
                    materialesPorNombre.put(material.getNombre(), material);
                }
            }
        } catch (SQLException e) {
            lblError.setText("No se pudo cargar el catálogo: " + e.getMessage());
        }
    }

    private void cargarResponsables() {
        try {
            cmbResponsable.getItems().clear();
            usuariosPorNombre.clear();
            for (Usuario usuario : usuarioDAO.listarTodos()) {
                cmbResponsable.getItems().add(usuario.getNombre());
                usuariosPorNombre.put(usuario.getNombre(), usuario);
            }

            if (Sesion.estaActiva()) {
                String nombreSesion = Sesion.getNombre();
                if (cmbResponsable.getItems().contains(nombreSesion)) {
                    cmbResponsable.setValue(nombreSesion);
                }
                cmbResponsable.setDisable(true);
            }

        } catch (SQLException e) {
            lblError.setText("No se pudieron cargar los usuarios: " + e.getMessage());
        }
    }

    private void cargarSiguienteFolio() {
        try {
            txtFolio.setText(desechoDAO.generarSiguienteFolio());
        } catch (SQLException e) {
            txtFolio.setText("DS-0001");
        }
    }

    public void cargarDesecho(Desecho desecho) {
        modoEdicion = true;
        idDesecho = desecho.getIdDesecho();

        txtFolio.setText(desecho.getFolio());
        cmbInsumo.setValue(desecho.getMaterialNombre());
        txtCantidad.setText(String.valueOf(desecho.getCantidad()));
        txtPeso.setText(String.valueOf(desecho.getPeso()));
        txtMotivo.setText(desecho.getMotivo());

        String fechaTexto = desecho.getFechaTexto();
        if (fechaTexto != null && !fechaTexto.isBlank()) {
            try {
                dpFecha.setValue(LocalDate.parse(fechaTexto, FORMATO_FECHA));
            } catch (Exception e) {
                dpFecha.setValue(null);
            }
        }

        cmbResponsable.setValue(desecho.getUsuarioNombre());
        txtDescripcion.setText(desecho.getDescripcion());

        cmbInsumo.setDisable(true);
        txtCantidad.setDisable(true);
        txtPeso.setDisable(true);
        cmbResponsable.setDisable(true);

        btnGuardar.setText("Actualizar");
    }

    @FXML
    private void mostrarInformacion() {
        String nombreInsumo = cmbInsumo.getValue();
        String textoCantidad = txtCantidad.getText().trim();
        String textoPeso = txtPeso.getText().trim();
        String motivo = txtMotivo.getText().trim();
        LocalDate fecha = dpFecha.getValue();
        String nombreResponsable = cmbResponsable.getValue();

        if (nombreInsumo == null || textoCantidad.isEmpty() || textoPeso.isEmpty()
                || motivo.isEmpty() || fecha == null || nombreResponsable == null) {
            lblError.setText("Completa Insumo, Cantidad, Peso, Motivo, Fecha y Responsable antes de guardar.");
            return;
        }

        try {
            if (modoEdicion) {
                desechoDAO.actualizar(idDesecho, motivo, fecha, txtDescripcion.getText().trim());
            } else {
                int cantidad;
                double peso;
                try {
                    cantidad = Integer.parseInt(textoCantidad);
                    peso = Double.parseDouble(textoPeso);
                } catch (NumberFormatException e) {
                    lblError.setText("Cantidad debe ser entero y Peso un número (ej. 1.5).");
                    return;
                }

                if (cantidad <= 0 || peso < 0) {
                    lblError.setText("Cantidad debe ser mayor a 0 y Peso no puede ser negativo.");
                    return;
                }

                Material material = materialesPorNombre.get(nombreInsumo);
                Usuario usuario = usuariosPorNombre.get(nombreResponsable);
                if (material == null || usuario == null) {
                    lblError.setText("Selecciona un insumo y un responsable válidos de la lista.");
                    return;
                }

                Desecho desecho = new Desecho();
                desecho.setFolio(txtFolio.getText());
                desecho.setIdMaterial(material.getIdMaterial());
                desecho.setCantidad(cantidad);
                desecho.setPeso(peso);
                desecho.setMotivo(motivo);
                desecho.setFecha(fecha);
                desecho.setIdUsuario(usuario.getIdUsuario());
                desecho.setDescripcion(txtDescripcion.getText().trim());

                desechoDAO.registrar(desecho);
            }

            lblError.setText("");
            accionVolver();

        } catch (SQLException e) {
            lblError.setText("Error al guardar en la base de datos: " + e.getMessage());
        } catch (IOException e) {
            lblError.setText("Se guardó, pero no se pudo volver al historial: " + e.getMessage());
        }
    }

    @FXML
    private void accionCancelar() {
        cmbInsumo.getSelectionModel().clearSelection();
        txtCantidad.clear();
        txtPeso.clear();
        txtMotivo.clear();
        dpFecha.setValue(null);
        txtDescripcion.clear();
        lblError.setText("");
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("from_ListaDesecho.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Historial de Desecho");
        stage.show();
    }
}
```

### `jome/RegistroUsuarioController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Usuario;

import java.io.IOException;
import java.sql.SQLException;

public class RegistroUsuarioController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private Button btnResultado;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblRegistro;

    @FXML
    private void mostrarInformacion() {
        String nombre = txtNombre.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        // Validación de campos vacíos
        if (nombre.isEmpty() || username.isEmpty() || password.isEmpty()) {
            lblRegistro.setText("Por favor completa todos los campos.");
            lblRegistro.setStyle("-fx-text-fill: #ff4444;");
            return;
        }

        // Validación de longitud mínima de contraseña
        if (password.length() < 4) {
            lblRegistro.setText("La contraseña debe tener al menos 4 caracteres.");
            lblRegistro.setStyle("-fx-text-fill: #ff4444;");
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();

            // Validar que el username no exista ya
            if (dao.existeUsername(username)) {
                lblRegistro.setText("El nombre de usuario ya está en uso.");
                lblRegistro.setStyle("-fx-text-fill: #ff4444;");
                return;
            }

            // Rol por defecto: Usuario
            Usuario nuevo = new Usuario(0, nombre, username, password, "Usuario");
            dao.insertar(nuevo);

            lblRegistro.setText("¡Usuario registrado correctamente!");
            lblRegistro.setStyle("-fx-text-fill: #2E7D32;");

            // Limpiar campos
            txtNombre.clear();
            txtUsername.clear();
            txtPassword.clear();

        } catch (SQLException e) {
            e.printStackTrace();
            lblRegistro.setText("Error de conexión con la base de datos.");
            lblRegistro.setStyle("-fx-text-fill: #ff4444;");
        }
    }

    @FXML
    private void accionVolverLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Pantalla_Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            lblRegistro.setText("Error al volver al login.");
            lblRegistro.setStyle("-fx-text-fill: #ff4444;");
        }
    }
}
```

### `jome/ReporteDetalleController.java`
```java
package utng.gtid2.jome;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ReporteDetalleController {

    @FXML private Label lblTitulo;
    @FXML private TextArea txtContenido;
    @FXML private Label lblMensaje;
    @FXML private Button btnCerrar;
    @FXML private Button btnSimularDescarga;

    /**
     * Recibe el título y el contenido en texto plano a mostrar.
     * Llamado desde ReportesController justo después de cargar el FXML.
     */
    public void setDatos(String titulo, String contenido) {
        lblTitulo.setText(titulo);
        txtContenido.setText(contenido);
    }

    @FXML
    private void accionSimularDescarga() {
        // pendiente: lógica real de generación/guardado de PDF
        lblMensaje.setText("Simulación: el PDF se generaría aquí.");
    }

    @FXML
    private void accionCerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }
}
```

### `jome/ReportesController.java`
```java
package utng.gtid2.jome;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utng.gtid2.dao.DashboardDAO;
import utng.gtid2.dao.DesechoDAO;
import utng.gtid2.dao.MaterialDAO;
import utng.gtid2.dao.PrestamoDAO;
import utng.gtid2.dao.UsuarioDAO;
import utng.gtid2.modelo.Desecho;
import utng.gtid2.modelo.Material;
import utng.gtid2.modelo.Prestamo;
import utng.gtid2.modelo.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportesController {

    @FXML private Button btnVolver;
    @FXML private VBox vboxResumen;
    @FXML private VBox vboxCatalogo;
    @FXML private VBox vboxAsignaciones;
    @FXML private VBox vboxDesecho;
    @FXML private VBox vboxUsuarios;

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final MaterialDAO  materialDAO  = new MaterialDAO();
    private final PrestamoDAO  prestamoDAO  = new PrestamoDAO();
    private final DesechoDAO   desechoDAO   = new DesechoDAO();
    private final UsuarioDAO   usuarioDAO   = new UsuarioDAO();

    private List<Material> materiales;
    private List<Prestamo> prestamos;
    private List<Desecho>  desechos;
    private List<Usuario>  usuarios;
    private int    totalMateriales;
    private int    criticos;
    private int    tecnicos;
    private double pesoMes;

    private static final BaseColor C_HEADER_BG  = new BaseColor(243, 244, 246);
    private static final BaseColor C_HEADER_TEXT = new BaseColor(107, 114, 128);
    private static final BaseColor C_TEXT        = new BaseColor(55,  65,  81);
    private static final BaseColor C_ROJO        = new BaseColor(239, 68,  68);
    private static final BaseColor C_MORADO      = new BaseColor(183, 33, 255);
    private static final BaseColor C_GRIS        = new BaseColor(156, 163, 175);

    @FXML
    public void initialize() {
        cargarDatos();
    }

    private void cargarDatos() {
        try {
            totalMateriales = dashboardDAO.contarMateriales();
            criticos        = dashboardDAO.contarCriticos();
            tecnicos        = dashboardDAO.contarTecnicos();
            pesoMes         = dashboardDAO.pesoDesechoMes();
            materiales      = materialDAO.listarTodos();
            prestamos       = prestamoDAO.listarTodos();
            desechos        = desechoDAO.listarTodos();
            usuarios        = usuarioDAO.listarTodos();

            poblarResumen();
            poblarCatalogo();
            poblarAsignaciones();
            poblarDesecho();
            poblarUsuarios();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Poblar FXML ───────────────────────────────────────────────────────────

    private void poblarResumen() {
        vboxResumen.getChildren().clear();
        agregarFilaResumen("Insumos registrados:",       String.valueOf(totalMateriales), false);
        agregarFilaResumen("Insumos en estado crítico:", String.valueOf(criticos),        true);
        agregarFilaResumen("Técnicos activos:",          String.valueOf(tecnicos),        false);
        agregarFilaResumen("Desecho del mes:",           String.format("%.1f kg", pesoMes), false);
    }

    private void agregarFilaResumen(String clave, String valor, boolean rojo) {
        HBox fila = new HBox();
        Label lClave = new Label(clave);
        lClave.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        HBox.setHgrow(lClave, Priority.ALWAYS);
        Label lValor = new Label(valor);
        lValor.setStyle(rojo
                ? "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #EF4444;"
                : "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        fila.getChildren().addAll(lClave, lValor);
        vboxResumen.getChildren().add(fila);
    }

    private void poblarCatalogo() {
        vboxCatalogo.getChildren().clear();
        vboxCatalogo.getChildren().add(headerFila(
                new String[]{"ID", "Nombre", "Categoría", "Total", "Disponible", "Estado"},
                new double[]{50, 155, 105, 65, 80, 80}));
        for (int i = 0; i < materiales.size(); i++) {
            Material m = materiales.get(i);
            String color = "Crítico".equals(m.getEstado()) ? "#EF4444" : "#374151";
            vboxCatalogo.getChildren().add(dataFila(new String[]{
                    String.valueOf(m.getIdMaterial()), m.getNombre(), m.getCategoria(),
                    String.valueOf(m.getCantidadTotal()), String.valueOf(m.getCantidadDisponible()), m.getEstado()
            }, new double[]{50, 155, 105, 65, 80, 80}, i, color));
        }
    }

    private void poblarAsignaciones() {
        vboxAsignaciones.getChildren().clear();
        vboxAsignaciones.getChildren().add(headerFila(
                new String[]{"Folio", "Insumo", "Responsable", "Cantidad", "F. Préstamo", "F. Devolución", "Estado"},
                new double[]{55, 115, 115, 60, 80, 80, 60}));
        for (int i = 0; i < prestamos.size(); i++) {
            Prestamo p = prestamos.get(i);
            String color = "Vencido".equals(p.getEstadoTexto()) ? "#EF4444" : "#374151";
            vboxAsignaciones.getChildren().add(dataFila(new String[]{
                    p.getFolio(), p.getMaterialNombre(), p.getUsuarioNombre(),
                    String.valueOf(p.getCantidad()),
                    p.getFechaPrestamoTexto(), p.getFechaDevolucionTexto(), p.getEstadoTexto()
            }, new double[]{55, 115, 115, 60, 80, 80, 60}, i, color));
        }
    }

    private void poblarDesecho() {
        vboxDesecho.getChildren().clear();
        vboxDesecho.getChildren().add(headerFila(
                new String[]{"Folio", "Insumo", "Motivo", "Cantidad", "Peso (kg)", "Fecha"},
                new double[]{60, 130, 130, 65, 70, 90}));
        for (int i = 0; i < desechos.size(); i++) {
            Desecho d = desechos.get(i);
            vboxDesecho.getChildren().add(dataFila(new String[]{
                    d.getFolio(), d.getMaterialNombre(), d.getMotivo(),
                    String.valueOf(d.getCantidad()), String.format("%.2f", d.getPeso()), d.getFechaTexto()
            }, new double[]{60, 130, 130, 65, 70, 90}, i, "#374151"));
        }
    }

    private void poblarUsuarios() {
        vboxUsuarios.getChildren().clear();
        vboxUsuarios.getChildren().add(headerFila(
                new String[]{"ID", "Nombre", "Username", "Rol"},
                new double[]{60, 200, 150, 90}));
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario u = usuarios.get(i);
            vboxUsuarios.getChildren().add(dataFila(new String[]{
                    String.valueOf(u.getIdUsuario()), u.getNombre(), u.getUsername(), u.getRol()
            }, new double[]{60, 200, 150, 90}, i, "#374151"));
        }
    }

    private HBox headerFila(String[] cols, double[] anchos) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: #F3F4F6; -fx-padding: 6 16 6 16;");
        for (int i = 0; i < cols.length; i++) {
            Label l = new Label(cols[i]);
            l.setPrefWidth(anchos[i]);
            l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");
            row.getChildren().add(l);
        }
        return row;
    }

    private HBox dataFila(String[] vals, double[] anchos, int idx, String color) {
        HBox row = new HBox();
        String bg = idx % 2 != 0 ? "-fx-background-color: #F9FAFB; " : "";
        row.setStyle(bg + "-fx-padding: 6 16 6 16; -fx-border-color: transparent transparent #F3F4F6 transparent; -fx-border-width: 1;");
        for (int i = 0; i < vals.length; i++) {
            Label l = new Label(vals[i] != null ? vals[i] : "");
            l.setPrefWidth(anchos[i]);
            l.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
            row.getChildren().add(l);
        }
        return row;
    }

    // ── Descargas PDF ─────────────────────────────────────────────────────────

    @FXML
    private void descargarResumen() {
        File f = pedirRuta("Resumen_General");
        if (f == null) return;
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Resumen General");
            PdfPTable t = new PdfPTable(2);
            t.setWidthPercentage(70);
            t.setWidths(new float[]{4, 1});
            hCell(t, "Indicador"); hCell(t, "Valor");
            dCell(t, "Insumos registrados",       false); dCell(t, String.valueOf(totalMateriales), false);
            dCell(t, "Insumos en estado crítico", false); dCell(t, String.valueOf(criticos),        true);
            dCell(t, "Técnicos activos",           false); dCell(t, String.valueOf(tecnicos),        false);
            dCell(t, "Desecho del mes",            false); dCell(t, String.format("%.1f kg", pesoMes), false);
            doc.add(t);
            ok("Resumen General guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    @FXML
    private void descargarCatalogo() {
        File f = pedirRuta("Catalogo_Insumos");
        if (f == null) return;
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Catálogo de Insumos");
            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1, 3, 2, 1, 1, 2});
            for (String h : new String[]{"ID", "Nombre", "Categoría", "Total", "Disponible", "Estado"}) hCell(t, h);
            for (Material m : materiales) {
                dCell(t, String.valueOf(m.getIdMaterial()),          false);
                dCell(t, m.getNombre(),                              false);
                dCell(t, m.getCategoria(),                           false);
                dCell(t, String.valueOf(m.getCantidadTotal()),       false);
                dCell(t, String.valueOf(m.getCantidadDisponible()),  false);
                dCell(t, m.getEstado(), "Crítico".equals(m.getEstado()));
            }
            doc.add(t);
            ok("Catálogo guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    @FXML
    private void descargarAsignaciones() {
        File f = pedirRuta("Historial_Prestamos");
        if (f == null) return;
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Historial de Préstamos");
            PdfPTable t = new PdfPTable(7);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1, 2, 2, 1, 2, 2, 1});
            for (String h : new String[]{"Folio", "Insumo", "Responsable", "Cantidad", "F. Préstamo", "F. Devolución", "Estado"}) hCell(t, h);
            for (Prestamo p : prestamos) {
                dCell(t, p.getFolio(),                false);
                dCell(t, p.getMaterialNombre(),       false);
                dCell(t, p.getUsuarioNombre(),        false);
                dCell(t, String.valueOf(p.getCantidad()), false);
                dCell(t, p.getFechaPrestamoTexto(),   false);
                dCell(t, p.getFechaDevolucionTexto(), false);
                dCell(t, p.getEstadoTexto(), "Vencido".equals(p.getEstadoTexto()));
            }
            doc.add(t);
            ok("Historial de Préstamos guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    @FXML
    private void descargarDesecho() {
        File f = pedirRuta("Registro_Desecho");
        if (f == null) return;
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Registro de Desecho");
            PdfPTable t = new PdfPTable(6);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1, 2, 2, 1, 1, 2});
            for (String h : new String[]{"Folio", "Insumo", "Motivo", "Cantidad", "Peso (kg)", "Fecha"}) hCell(t, h);
            for (Desecho d : desechos) {
                dCell(t, d.getFolio(),                        false);
                dCell(t, d.getMaterialNombre(),               false);
                dCell(t, d.getMotivo(),                       false);
                dCell(t, String.valueOf(d.getCantidad()),     false);
                dCell(t, String.format("%.2f", d.getPeso()), false);
                dCell(t, d.getFechaTexto(),                   false);
            }
            doc.add(t);
            ok("Registro de Desecho guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    @FXML
    private void descargarUsuarios() {
        File f = pedirRuta("Usuarios_Registrados");
        if (f == null) return;
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Usuarios Registrados");
            PdfPTable t = new PdfPTable(4);
            t.setWidthPercentage(100);
            t.setWidths(new float[]{1, 3, 2, 1});
            for (String h : new String[]{"ID", "Nombre", "Username", "Rol"}) hCell(t, h);
            for (Usuario u : usuarios) {
                dCell(t, String.valueOf(u.getIdUsuario()), false);
                dCell(t, u.getNombre(),                    false);
                dCell(t, u.getUsername(),                  false);
                dCell(t, u.getRol(),                       false);
            }
            doc.add(t);
            ok("Usuarios guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    @FXML
    private void descargarReporte() {
        File f = pedirRuta("Reporte_General_CGTI");
        if (f == null) return;
        Document doc = new Document(PageSize.A4.rotate());
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(f));
            doc.open();
            encabezado(doc, "Reporte General del Sistema de Inventario");

            titulo(doc, "Resumen General");
            PdfPTable tRes = new PdfPTable(2);
            tRes.setWidthPercentage(60);
            tRes.setWidths(new float[]{4, 1});
            hCell(tRes, "Indicador"); hCell(tRes, "Valor");
            dCell(tRes, "Insumos registrados",       false); dCell(tRes, String.valueOf(totalMateriales), false);
            dCell(tRes, "Insumos en estado crítico", false); dCell(tRes, String.valueOf(criticos),        true);
            dCell(tRes, "Técnicos activos",           false); dCell(tRes, String.valueOf(tecnicos),        false);
            dCell(tRes, "Desecho del mes",            false); dCell(tRes, String.format("%.1f kg", pesoMes), false);
            doc.add(tRes);

            titulo(doc, "Catálogo de Insumos");
            PdfPTable tCat = new PdfPTable(6);
            tCat.setWidthPercentage(100);
            tCat.setWidths(new float[]{1, 3, 2, 1, 1, 2});
            for (String h : new String[]{"ID", "Nombre", "Categoría", "Total", "Disponible", "Estado"}) hCell(tCat, h);
            for (Material m : materiales) {
                dCell(tCat, String.valueOf(m.getIdMaterial()),         false);
                dCell(tCat, m.getNombre(),                             false);
                dCell(tCat, m.getCategoria(),                          false);
                dCell(tCat, String.valueOf(m.getCantidadTotal()),      false);
                dCell(tCat, String.valueOf(m.getCantidadDisponible()), false);
                dCell(tCat, m.getEstado(), "Crítico".equals(m.getEstado()));
            }
            doc.add(tCat);

            titulo(doc, "Historial de Préstamos");
            PdfPTable tPre = new PdfPTable(7);
            tPre.setWidthPercentage(100);
            tPre.setWidths(new float[]{1, 2, 2, 1, 2, 2, 1});
            for (String h : new String[]{"Folio", "Insumo", "Responsable", "Cantidad", "F. Préstamo", "F. Devolución", "Estado"}) hCell(tPre, h);
            for (Prestamo p : prestamos) {
                dCell(tPre, p.getFolio(),                false);
                dCell(tPre, p.getMaterialNombre(),       false);
                dCell(tPre, p.getUsuarioNombre(),        false);
                dCell(tPre, String.valueOf(p.getCantidad()), false);
                dCell(tPre, p.getFechaPrestamoTexto(),   false);
                dCell(tPre, p.getFechaDevolucionTexto(), false);
                dCell(tPre, p.getEstadoTexto(), "Vencido".equals(p.getEstadoTexto()));
            }
            doc.add(tPre);

            titulo(doc, "Registro de Desecho");
            PdfPTable tDes = new PdfPTable(6);
            tDes.setWidthPercentage(100);
            tDes.setWidths(new float[]{1, 2, 2, 1, 1, 2});
            for (String h : new String[]{"Folio", "Insumo", "Motivo", "Cantidad", "Peso (kg)", "Fecha"}) hCell(tDes, h);
            for (Desecho d : desechos) {
                dCell(tDes, d.getFolio(),                        false);
                dCell(tDes, d.getMaterialNombre(),               false);
                dCell(tDes, d.getMotivo(),                       false);
                dCell(tDes, String.valueOf(d.getCantidad()),     false);
                dCell(tDes, String.format("%.2f", d.getPeso()), false);
                dCell(tDes, d.getFechaTexto(),                   false);
            }
            doc.add(tDes);

            titulo(doc, "Usuarios Registrados");
            PdfPTable tUsr = new PdfPTable(4);
            tUsr.setWidthPercentage(100);
            tUsr.setWidths(new float[]{1, 3, 2, 1});
            for (String h : new String[]{"ID", "Nombre", "Username", "Rol"}) hCell(tUsr, h);
            for (Usuario u : usuarios) {
                dCell(tUsr, String.valueOf(u.getIdUsuario()), false);
                dCell(tUsr, u.getNombre(),                    false);
                dCell(tUsr, u.getUsername(),                  false);
                dCell(tUsr, u.getRol(),                       false);
            }
            doc.add(tUsr);

            ok("Reporte General guardado correctamente.");
        } catch (Exception e) {
            err(e);
        } finally {
            if (doc.isOpen()) doc.close();
        }
    }

    // ── Helpers PDF ───────────────────────────────────────────────────────────

    private File pedirRuta(String nombre) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar reporte");
        fc.setInitialFileName(nombre + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fc.showSaveDialog(btnVolver.getScene().getWindow());
    }

    private void encabezado(Document doc, String texto) throws DocumentException {
        // Logo institucional
        try {
            URL logoUrl = getClass().getResource("/utng/gtid2/images/logosUTNG.png");
            if (logoUrl != null) {
                Image logo = Image.getInstance(logoUrl);
                logo.scaleToFit(300, 60);
                logo.setAlignment(Element.ALIGN_CENTER);
                doc.add(logo);
            }
        } catch (Exception e) {
            // Si no carga el logo, continúa sin él
        }

        Font fTitulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, C_MORADO);
        Paragraph pTit = new Paragraph("CGTI  |  " + texto, fTitulo);
        pTit.setAlignment(Element.ALIGN_CENTER);
        pTit.setSpacingBefore(6);
        doc.add(pTit);

        Font fFecha = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, C_GRIS);
        Paragraph pFecha = new Paragraph("Generado el: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fFecha);
        pFecha.setAlignment(Element.ALIGN_CENTER);
        doc.add(pFecha);
        doc.add(Chunk.NEWLINE);
    }

    private void titulo(Document doc, String texto) throws DocumentException {
        Font f = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(31, 41, 55));
        Paragraph p = new Paragraph(texto, f);
        p.setSpacingBefore(14);
        p.setSpacingAfter(4);
        doc.add(p);
    }

    private void hCell(PdfPTable tabla, String texto) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, C_HEADER_TEXT);
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setBackgroundColor(C_HEADER_BG);
        c.setPadding(6);
        tabla.addCell(c);
    }

    private void dCell(PdfPTable tabla, String texto, boolean rojo) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, rojo ? C_ROJO : C_TEXT);
        PdfPCell c = new PdfPCell(new Phrase(texto != null ? texto : "", f));
        c.setPadding(6);
        tabla.addCell(c);
    }

    private void ok(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void err(Exception e) {
        Alert a = new Alert(Alert.AlertType.ERROR,
                "No se pudo generar el PDF: " + e.getMessage(), ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    private void accionVolver() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Principal.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) btnVolver.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Panel Principal");
        stage.show();
    }
}
```

### `jome/SecondaryController.java`
```java
package utng.gtid2.jome;

import java.io.IOException;
import javafx.fxml.FXML;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}
```

### `jome/Sesion.java`
```java
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
```
