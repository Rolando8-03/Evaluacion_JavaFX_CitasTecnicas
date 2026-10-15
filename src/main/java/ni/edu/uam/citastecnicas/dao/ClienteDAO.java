package ni.edu.uam.citastecnicas.dao;

import ni.edu.uam.citastecnicas.db.ConexionDB;
import ni.edu.uam.citastecnicas.model.Cliente;
import ni.edu.uam.citastecnicas.util.Validacion;
import java.sql.*;
import java.util.*;

public final class ClienteDAO {
    public List<Cliente> buscar(String texto) throws SQLException {
        String sql="SELECT id,nombres,apellidos,telefono,correo,activo FROM public.cliente "
            + "WHERE nombres ILIKE ? ESCAPE '!' OR apellidos ILIKE ? ESCAPE '!' OR correo ILIKE ? ESCAPE '!' ORDER BY id ASC";
        // Los comodines escritos por el usuario se tratan como texto literal.
        String patron="%" + (texto==null ? "" : texto.trim()).replace("!","!!").replace("%","!%").replace("_","!_") + "%";
        List<Cliente> clientes=new ArrayList<>();
        try (Connection c=ConexionDB.conectar(); PreparedStatement s=c.prepareStatement(sql)) {
            for (int i=1;i<=3;i++) s.setString(i,patron);
            try (ResultSet r=s.executeQuery()) {
                while (r.next()) clientes.add(new Cliente(r.getInt("id"),r.getString("nombres"),r.getString("apellidos"),
                    r.getString("telefono"),r.getString("correo"),r.getBoolean("activo")));
            }
        }
        return clientes;
    }
    public int insertar(Cliente cliente) throws SQLException {
        Validacion.cliente(cliente);
        if (cliente.getId() != 0) throw new IllegalArgumentException("Un registro nuevo debe tener ID 0 antes de guardarse.");
        comprobarCorreo(cliente.getCorreo(), 0);
        String sql="INSERT INTO public.cliente(nombres,apellidos,telefono,correo,activo) VALUES (?,?,?,?,?) RETURNING id";
        try (Connection c=ConexionDB.conectar(); PreparedStatement s=c.prepareStatement(sql)) {
            parametros(s,cliente);
            try (ResultSet r=s.executeQuery()) { if (r.next()) return r.getInt(1); }
        }
        throw new SQLException("No se obtuvo el ID del cliente.");
    }
    public void actualizar(Cliente cliente) throws SQLException {
        Validacion.cliente(cliente);
        Validacion.id(cliente.getId(), "cliente");
        comprobarCorreo(cliente.getCorreo(), cliente.getId());
        try (Connection c=ConexionDB.conectar(); PreparedStatement s=c.prepareStatement(
            "UPDATE public.cliente SET nombres=?,apellidos=?,telefono=?,correo=?,activo=? WHERE id=?")) {
            parametros(s,cliente); s.setInt(6,cliente.getId()); comprobarFila(s.executeUpdate());
        }
    }
    public void eliminar(int id) throws SQLException {
        Validacion.id(id, "cliente");
        // Primero explicar la restricción; la FK sigue protegiendo ante cambios simultáneos.
        try (Connection c = ConexionDB.conectar(); PreparedStatement s = c.prepareStatement(
                "SELECT COUNT(*) FROM public.cita_tecnica WHERE cliente_id=?")) {
            s.setInt(1, id);
            try (ResultSet r = s.executeQuery()) {
                if (r.next() && r.getInt(1) > 0) {
                    String mensaje;
                    if (r.getInt(1) == 1) {
                        mensaje = "El cliente tiene una cita registrada y por eso se rechaza su eliminación.";
                    } else {
                        mensaje = "El cliente tiene " + r.getInt(1) + " citas registradas y por eso se rechaza su eliminación.";
                    }
                    throw new SQLException(mensaje, "CLIENTE_CON_CITAS");
                }
            }
        }
        try (Connection c=ConexionDB.conectar(); PreparedStatement s=c.prepareStatement("DELETE FROM public.cliente WHERE id=?")) {
            s.setInt(1,id);
            try {
                comprobarFila(s.executeUpdate());
            } catch (SQLException e) {
                if ("23503".equals(e.getSQLState()) || "23001".equals(e.getSQLState())) {
                    throw new SQLException("El cliente tiene una o más citas registradas y por eso se rechaza su eliminación.", "CLIENTE_CON_CITAS", e);
                }
                throw e;
            }
        }
    }
    private void comprobarCorreo(String correo, int idActual) throws SQLException {
        // Excluir el ID actual permite guardar su mismo correo al editar.
        String sql = "SELECT id FROM public.cliente WHERE lower(btrim(correo)) = lower(btrim(?)) AND id <> ?";
        try (Connection c = ConexionDB.conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, correo);
            s.setInt(2, idActual);
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) throw new SQLException("Correo repetido.", "23505");
            }
        }
    }
    private void parametros(PreparedStatement s,Cliente c) throws SQLException {
        s.setString(1,c.getNombres().trim()); s.setString(2,c.getApellidos().trim());
        s.setString(3,c.getTelefono().trim()); s.setString(4,c.getCorreo().trim().toLowerCase(Locale.ROOT));
        s.setBoolean(5,c.isActivo());
    }
    private void comprobarFila(int filas) throws SQLException {
        if (filas!=1) throw new SQLException("El cliente ya no existe. Actualizá la tabla.","P0002");
    }
}
