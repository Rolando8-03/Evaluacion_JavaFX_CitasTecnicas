package ni.edu.uam.citastecnicas.model;

import java.time.LocalDate;
import java.time.LocalTime;

public final class CitaTecnica {
    private final int id, clienteId;
    private final String nombreCliente, tipoServicio, descripcion, estado;
    private final LocalDate fecha;
    private final LocalTime hora;
    public CitaTecnica(int id, int clienteId, String nombreCliente, LocalDate fecha,
                      LocalTime hora, String tipoServicio, String descripcion, String estado) {
        this.id=id; this.clienteId=clienteId; this.nombreCliente=nombreCliente;
        this.fecha=fecha; this.hora=hora; this.tipoServicio=tipoServicio;
        this.descripcion=descripcion; this.estado=estado;
    }
    public int getId() { return id; }
    public int getClienteId() { return clienteId; }
    public String getNombreCliente() { return nombreCliente; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHora() { return hora; }
    public String getTipoServicio() { return tipoServicio; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
}
