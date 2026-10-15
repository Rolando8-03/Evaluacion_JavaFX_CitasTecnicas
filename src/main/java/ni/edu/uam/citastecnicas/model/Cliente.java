package ni.edu.uam.citastecnicas.model;

public final class Cliente {
    private final int id;
    private final String nombres, apellidos, telefono, correo;
    private final boolean activo;
    public Cliente(int id, String nombres, String apellidos, String telefono, String correo, boolean activo) {
        this.id=id; this.nombres=nombres; this.apellidos=apellidos;
        this.telefono=telefono; this.correo=correo; this.activo=activo;
    }
    public int getId() { return id; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public String getTelefono() { return telefono; }
    public String getCorreo() { return correo; }
    public boolean isActivo() { return activo; }
    public String getNombreCompleto() { return nombres + " " + apellidos; }
    @Override public String toString() { return getNombreCompleto() + " · #" + id; }
}
