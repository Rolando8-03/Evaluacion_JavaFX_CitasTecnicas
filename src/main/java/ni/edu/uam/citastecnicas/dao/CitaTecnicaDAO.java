package ni.edu.uam.citastecnicas.dao;

import ni.edu.uam.citastecnicas.db.ConexionDB;
import ni.edu.uam.citastecnicas.model.CitaTecnica;
import ni.edu.uam.citastecnicas.util.Validacion;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public final class CitaTecnicaDAO {
    public List<CitaTecnica> filtrar(String estado, LocalDate fecha) throws SQLException {
        if (estado != null && !Validacion.estadoPermitido(estado)) {
            throw new IllegalArgumentException("Seleccioná un estado permitido para filtrar.");
        }
        if (fecha != null) Validacion.fecha(fecha);
        String sql = "SELECT ct.*, cl.nombres || ' ' || cl.apellidos AS nombre_cliente "
                + "FROM public.cita_tecnica ct JOIN public.cliente cl ON cl.id = ct.cliente_id WHERE 1=1";
        // Solo se agregan fragmentos SQL fijos; los valores siempre son parámetros.
        if (estado != null) sql += " AND ct.estado = ?";
        if (fecha != null) sql += " AND ct.fecha = ?";
        sql += " ORDER BY ct.fecha, ct.hora, ct.id";
        List<CitaTecnica> citas = new ArrayList<>();
        try (Connection c = ConexionDB.conectar(); PreparedStatement s = c.prepareStatement(sql)) {
            int parametro = 1;
            if (estado != null) s.setString(parametro++, estado);
            if (fecha != null) s.setDate(parametro, java.sql.Date.valueOf(fecha));
            try (ResultSet r=s.executeQuery()) {
                while (r.next()) citas.add(new CitaTecnica(r.getInt("id"),r.getInt("cliente_id"),r.getString("nombre_cliente"),
                    r.getDate("fecha").toLocalDate(),r.getTime("hora").toLocalTime(),r.getString("tipo_servicio"),
                    r.getString("descripcion"),r.getString("estado")));
            }
        }
        return citas;
    }
    public int insertar(CitaTecnica cita) throws SQLException {
        Validacion.cita(cita);
        if (cita.getId() != 0) throw new IllegalArgumentException("Un registro nuevo debe tener ID 0 antes de guardarse.");
        try (Connection c=ConexionDB.conectar(); PreparedStatement s=c.prepareStatement(
            "INSERT INTO public.cita_tecnica(cliente_id,fecha,hora,tipo_servicio,descripcion,estado) VALUES (?,?,?,?,?,?) RETURNING id")) {
            comprobarDisponibilidad(c, cita);
            parametros(s,cita);
            try (ResultSet r=s.executeQuery()) { if (r.next()) return r.getInt(1); }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new SQLException("El cliente ya tiene una cita en ese horario.", "CITA_DUPLICADA", e);
            }
            throw e;
        }
        throw new SQLException("No se obtuvo el ID de la cita.");
    }
    public void actualizar(CitaTecnica cita) throws SQLException {
        Validacion.cita(cita);
        Validacion.id(cita.getId(), "cita");
        try (Connection c=ConexionDB.conectar(); PreparedStatement s=c.prepareStatement(
            "UPDATE public.cita_tecnica SET cliente_id=?,fecha=?,hora=?,tipo_servicio=?,descripcion=?,estado=? WHERE id=?")) {
            comprobarDisponibilidad(c, cita);
            parametros(s,cita); s.setInt(7,cita.getId()); comprobarFila(s.executeUpdate());
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new SQLException("El cliente ya tiene una cita en ese horario.", "CITA_DUPLICADA", e);
            }
            throw e;
        }
    }
    public void eliminar(int id) throws SQLException {
        Validacion.id(id, "cita");
        try (Connection c=ConexionDB.conectar(); PreparedStatement s=c.prepareStatement("DELETE FROM public.cita_tecnica WHERE id=?")) {
            s.setInt(1,id); comprobarFila(s.executeUpdate());
        }
    }
    private void comprobarDisponibilidad(Connection conexion, CitaTecnica cita) throws SQLException {
        String sql = "SELECT id FROM public.cita_tecnica WHERE cliente_id=? AND fecha=? AND hora=? AND id<>? LIMIT 1";
        try (PreparedStatement s = conexion.prepareStatement(sql)) {
            s.setInt(1, cita.getClienteId());
            s.setDate(2, java.sql.Date.valueOf(cita.getFecha()));
            s.setTime(3, Time.valueOf(cita.getHora()));
            s.setInt(4, cita.getId());
            try (ResultSet r = s.executeQuery()) {
                if (r.next()) {
                    throw new SQLException("El cliente ya tiene una cita en ese horario.", "CITA_DUPLICADA");
                }
            }
        }
    }

    private void parametros(PreparedStatement s,CitaTecnica c) throws SQLException {
        s.setInt(1,c.getClienteId()); s.setDate(2,java.sql.Date.valueOf(c.getFecha())); s.setTime(3,Time.valueOf(c.getHora()));
        s.setString(4,c.getTipoServicio().trim()); s.setString(5,c.getDescripcion().trim()); s.setString(6,c.getEstado());
    }
    private void comprobarFila(int filas) throws SQLException {
        if (filas!=1) throw new SQLException("La cita ya no existe. Actualizá la tabla.","P0002");
    }
}
