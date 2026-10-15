package ni.edu.uam.citastecnicas;

import ni.edu.uam.citastecnicas.dao.*;
import ni.edu.uam.citastecnicas.db.ConexionDB;
import ni.edu.uam.citastecnicas.model.*;
import java.sql.*;
import java.time.*;
import java.util.List;

// Ejecutar en IntelliJ con DB_URL apuntando a gestion_citas_pruebas.
// No crea tablas ni modifica otros registros. No requiere JUnit.
public class PruebasPostgreSQL {
    private static int comprobaciones;
    private static void comprobar(boolean condicion, String caso) {
        if (!condicion) throw new IllegalStateException("FALLÓ: " + caso);
        comprobaciones++;
    }
    private static Cliente cliente(List<Cliente> clientes, int id) {
        for (Cliente c : clientes) if (c.getId() == id) return c;
        return null;
    }
    private static CitaTecnica cita(List<CitaTecnica> citas, int id) {
        for (CitaTecnica c : citas) if (c.getId() == id) return c;
        return null;
    }

    public static void main(String[] args) throws SQLException {
        try (Connection c = ConexionDB.conectar(); Statement s = c.createStatement();
             ResultSet r = s.executeQuery("SELECT current_database()")) {
            if (!r.next() || !r.getString(1).equals("gestion_citas_pruebas")) {
                throw new IllegalStateException("Detenido: usar solamente gestion_citas_pruebas.");
            }
        }
        comprobar(ConexionDB.comprobar(), "Statement SELECT 1");
        ClienteDAO clientes = new ClienteDAO();
        CitaTecnicaDAO citas = new CitaTecnicaDAO();
        String token = String.valueOf(System.currentTimeMillis());
        String correo = "qa" + token + "@example.com";
        String correoDos = "qb" + token + "@example.com";
        int idCliente = 0, idSegundo = 0, idCita = 0;
        LocalDate fecha = LocalDate.of(2026, 10, 15);
        try {
            idCliente = clientes.insertar(new Cliente(0, "Ana", "López Prueba", "8888-8888", correo, true));
            comprobar(cliente(clientes.buscar("lÓp"), idCliente) != null, "buscar apellido parcial sin mayúsculas");
            comprobar(cliente(clientes.buscar("aNa"), idCliente) != null, "buscar nombre parcial");
            comprobar(cliente(clientes.buscar(correo), idCliente) != null, "buscar correo");
            comprobar(cliente(clientes.buscar("' OR 1=1 --"), idCliente) == null, "texto SQL se trata como parámetro");
            comprobar(cliente(clientes.buscar("%"), idCliente) == null, "porcentaje es texto literal");
            comprobar(cliente(clientes.buscar("_"), idCliente) == null, "guion bajo es texto literal");
            try {
                clientes.insertar(new Cliente(0, "Otra", "Persona", "9999-9999", correo.toUpperCase(), true));
                throw new IllegalStateException("Permitió correo duplicado.");
            } catch (SQLException e) {
                comprobar("23505".equals(e.getSQLState()), "duplicado al insertar");
            }
            idSegundo = clientes.insertar(new Cliente(0, "Luis", "Mora Prueba", "9999-9999", correoDos, true));
            try {
                clientes.actualizar(new Cliente(idSegundo, "Luis", "Mora Prueba", "9999-9999", correo, true));
                throw new IllegalStateException("Permitió duplicado al editar.");
            } catch (SQLException e) {
                comprobar("23505".equals(e.getSQLState()), "duplicado al editar");
            }
            clientes.actualizar(new Cliente(idCliente, "Ana María", "López Prueba", "7777-7777", correo, false));
            Cliente actualizado = cliente(clientes.buscar(correo), idCliente);
            comprobar(actualizado != null, "cliente permanece al editar mismo correo");
            comprobar(actualizado.getTelefono().equals("7777-7777"), "teléfono persistido");
            comprobar(actualizado.getNombres().equals("Ana María"), "nombres actualizados");
            comprobar(!actualizado.isActivo(), "activo editable no sustituye eliminación");
            idCita = citas.insertar(new CitaTecnica(0, idCliente, "", fecha, LocalTime.of(9, 30), "Reparación", "Pantalla", "Pendiente"));
            CitaTecnica registrada = cita(citas.filtrar("Pendiente", fecha), idCita);
            comprobar(registrada != null, "insertar cita y filtrar por ambas condiciones");
            comprobar(registrada.getNombreCliente().equals("Ana María López Prueba"), "JOIN de nombre del cliente");
            comprobar(cita(citas.filtrar("Finalizada", fecha), idCita) == null, "estado que no coincide");
            comprobar(cita(citas.filtrar(null, fecha.plusDays(1)), idCita) == null, "fecha que no coincide");
            comprobar(cita(citas.filtrar("Pendiente", null), idCita) != null, "solo estado");
            comprobar(cita(citas.filtrar(null, fecha), idCita) != null, "solo fecha");
            comprobar(cita(citas.filtrar(null, null), idCita) != null, "sin filtros");
            citas.actualizar(new CitaTecnica(idCita, idSegundo, "", fecha.plusDays(1), LocalTime.of(10, 0), "Mantenimiento", "Limpieza", "Finalizada"));
            CitaTecnica modificada = cita(citas.filtrar("Finalizada", fecha.plusDays(1)), idCita);
            comprobar(modificada != null, "cambiar estado fecha y cliente");
            comprobar(modificada.getNombreCliente().equals("Luis Mora Prueba"), "nueva relación");
            comprobar(modificada.getHora().equals(LocalTime.of(10, 0)), "hora modificada");
            comprobar(modificada.getTipoServicio().equals("Mantenimiento"), "servicio modificado");
            comprobar(modificada.getDescripcion().equals("Limpieza"), "descripción modificada");
            try {
                clientes.eliminar(idSegundo);
                throw new IllegalStateException("Permitió borrar cliente con citas.");
            } catch (SQLException e) {
                comprobar("CLIENTE_CON_CITAS".equals(e.getSQLState()), "eliminación restringida");
            }
            comprobar(cliente(clientes.buscar(correoDos), idSegundo) != null, "cliente protegido permanece");
            // Cada DAO abrió/cerró su Connection: esta lectura usa otra conexión.
            comprobar(cita(citas.filtrar(null, null), idCita) != null, "persistencia entre conexiones");
            int eliminado = idCita;
            citas.eliminar(idCita); idCita = 0;
            comprobar(cita(citas.filtrar(null, null), eliminado) == null, "eliminar físicamente cita");
            try {
                citas.eliminar(eliminado);
                throw new IllegalStateException("No avisó que la cita ya no existe.");
            } catch (SQLException e) {
                comprobar("P0002".equals(e.getSQLState()), "registro eliminado por otra operación");
            }
            clientes.eliminar(idSegundo); idSegundo = 0;
            clientes.eliminar(idCliente); idCliente = 0;
            comprobar(clientes.buscar(correo).isEmpty(), "eliminar físicamente cliente");
            System.out.println("CORRECTO: " + comprobaciones + " comprobaciones con PostgreSQL real.");
        } finally {
            if (idCita != 0) citas.eliminar(idCita);
            if (idSegundo != 0) clientes.eliminar(idSegundo);
            if (idCliente != 0) clientes.eliminar(idCliente);
        }
    }
}
