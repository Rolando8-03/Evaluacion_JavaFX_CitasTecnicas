package ni.edu.uam.citastecnicas;

import ni.edu.uam.citastecnicas.model.*;
import ni.edu.uam.citastecnicas.util.*;
import java.sql.SQLException;
import java.time.*;

// Pruebas sencillas: ejecutar este main en IntelliJ; no requieren PostgreSQL ni JUnit.
public class PruebasValidacion {
    private static int comprobaciones;

    private static void comprobar(boolean condicion, String caso) {
        if (!condicion) throw new IllegalStateException("FALLÓ: " + caso);
        comprobaciones++;
    }

    private static void rechazar(Runnable accion, String caso) {
        boolean rechazado = false;
        try { accion.run(); }
        catch (IllegalArgumentException e) { rechazado = true; }
        comprobar(rechazado, caso);
    }

    public static void main(String[] args) {
        comprobar(Validacion.texto(" Ana ", "Nombres", 80).equals("Ana"), "trim");
        String[] vacios = {"", " ", "\n\t", "\u00a0", "\u2003", "\u202f"};
        for (String valor : vacios) rechazar(() -> Validacion.texto(valor, "Campo", 80), "vacío/invisible");
        rechazar(() -> Validacion.texto(null, "Campo", 80), "null");
        rechazar(() -> Validacion.texto("a\0b", "Campo", 80), "carácter nulo");
        rechazar(() -> Validacion.texto("a\u0001b", "Campo", 80), "carácter de control");
        int[] maximos = {20, 80, 120, 255};
        for (int maximo : maximos) {
            comprobar(Validacion.texto("a".repeat(maximo), "Campo", maximo).length() == maximo, "longitud exacta");
            rechazar(() -> Validacion.texto("a".repeat(maximo + 1), "Campo", maximo), "exceso de longitud");
        }
        String[] nombres = {"Ana", "María José", "Jean-Luc", "O'Connor", "D’Ávila", "Ángel", "López Mena"};
        for (String nombre : nombres) comprobar(Validacion.nombre(nombre, "Nombre").equals(nombre), "nombre válido");
        String[] malosNombres = {"123", "Ana1", "@Ana", "-Ana", "Ana\nMaría", "Ana_"};
        for (String nombre : malosNombres) rechazar(() -> Validacion.nombre(nombre, "Nombre"), "nombre inválido");
        String[] telefonos = {"8888-8888", "+505 8888-8888", "(505) 88888888", "1234567", "123456789012345"};
        for (String telefono : telefonos) comprobar(Validacion.telefono(telefono).equals(telefono), "teléfono válido");
        String[] malosTelefonos = {"abc", "123456", "1234567890123456", "8888+8888", "(505 88888888", "505)88888888", "((505))88888888", "()88888888", "( )88888888"};
        for (String telefono : malosTelefonos) rechazar(() -> Validacion.telefono(telefono), "teléfono inválido");
        String[] correos = {"ana@example.com", "JUAN@EMPRESA.COM", "juan+ventas@empresa.com", "a@sub.empresa.com", "a.b@empresa.com"};
        for (String correo : correos) comprobar(Validacion.correo(correo).equals(correo), "correo válido");
        String[] malosCorreos = {"a@b", "a@@b.com", "a b@c.com", "a@b..com", ".a@b.com", "a.@b.com", "a..b@c.com", "a@-b.com", "a@b-.com", "a@b_com.com", "a".repeat(65) + "@b.com"};
        for (String correo : malosCorreos) rechazar(() -> Validacion.correo(correo), "correo inválido");
        comprobar(Validacion.hora("00:00").equals(LocalTime.MIDNIGHT), "hora mínima");
        comprobar(Validacion.hora("23:59").equals(LocalTime.of(23, 59)), "hora máxima");
        comprobar(Validacion.hora(" 09:30 ").equals(LocalTime.of(9, 30)), "hora con espacios");
        String[] malasHoras = {"24:00", "09:60", "9:30", "", "ab", "12:30:00", "-1:00", "00:-1"};
        for (String hora : malasHoras) rechazar(() -> Validacion.hora(hora), "hora inválida");
        rechazar(() -> Validacion.hora(null), "hora null");
        rechazar(() -> Validacion.fecha(null), "fecha ausente");
        rechazar(() -> Validacion.fecha(LocalDate.of(0, 1, 1)), "año cero");
        rechazar(() -> Validacion.fecha(LocalDate.of(10000, 1, 1)), "año fuera de rango");
        Validacion.fecha(LocalDate.of(2024, 2, 29)); comprobaciones++;
        // La guía no prohíbe fechas pasadas: permite registrar atención histórica.
        Validacion.fecha(LocalDate.of(2020, 1, 1)); comprobaciones++;
        rechazar(() -> Validacion.id(0, "cliente"), "ID cero");
        rechazar(() -> Validacion.id(-1, "cita"), "ID negativo");
        Validacion.id(1, "cliente"); comprobaciones++;
        Validacion.cliente(new Cliente(0, "Ana", "López", "8888-8888", "a@example.com", false)); comprobaciones++;
        rechazar(() -> Validacion.cliente(null), "cliente null");
        for (String estado : Validacion.ESTADOS) {
            Validacion.cita(new CitaTecnica(0, 1, "Ana", LocalDate.now(), LocalTime.NOON, "Servicio", "Descripción", estado));
            comprobaciones++;
        }
        String[] estadosIncorrectos = {"", "Otro", "pendiente", " Pendiente ", null};
        for (String estado : estadosIncorrectos) {
            rechazar(() -> Validacion.cita(new CitaTecnica(0, 1, "Ana", LocalDate.now(), LocalTime.NOON, "Servicio", "Descripción", estado)), "estado inválido");
        }
        rechazar(() -> Validacion.cita(null), "cita null");
        rechazar(() -> Validacion.cita(new CitaTecnica(0, 0, "", LocalDate.now(), LocalTime.NOON, "S", "D", "Pendiente")), "cliente no seleccionado");
        rechazar(() -> Validacion.cita(new CitaTecnica(0, 1, "", LocalDate.now(), null, "S", "D", "Pendiente")), "hora ausente");
        rechazar(() -> Validacion.cita(new CitaTecnica(0, 1, "", LocalDate.now(), LocalTime.of(9, 30, 1), "S", "D", "Pendiente")), "segundos no admitidos");
        rechazar(() -> Validacion.cita(new CitaTecnica(0, 1, "", LocalDate.now(), LocalTime.NOON, " ", "D", "Pendiente")), "servicio vacío");
        rechazar(() -> Validacion.cita(new CitaTecnica(0, 1, "", LocalDate.now(), LocalTime.NOON, "S", " ", "Pendiente")), "descripción vacía");
        comprobar(Alertas.mensaje(new SQLException("detalle", "23505")).contains("correo"), "mensaje duplicado");
        comprobar(Alertas.mensaje(new SQLException("detalle", "CLIENTE_CON_CITAS")).contains("citas"), "mensaje FK");
        comprobar(Alertas.mensaje(new SQLException("clave secreta", "28P01")).contains("credenciales"), "mensaje autenticación");
        comprobar(!Alertas.mensaje(new SQLException("clave secreta", "28P01")).contains("clave secreta"), "sin divulgar error crudo");
        System.out.println("CORRECTO: " + comprobaciones + " comprobaciones de validación y mensajes.");
    }
}
