package ni.edu.uam.citastecnicas.util;

import ni.edu.uam.citastecnicas.model.Cliente;
import ni.edu.uam.citastecnicas.model.CitaTecnica;
import java.time.LocalTime;
import java.time.LocalDate;

public class Validacion {
    public static final String[] ESTADOS = {"Pendiente", "En proceso", "Finalizada", "Cancelada"};

    private Validacion() {}

    public static String texto(String valor, String campo, int maximo) {
        if (valor == null) throw new IllegalArgumentException(campo + " es obligatorio.");
        String limpio = valor.trim();
        boolean tieneContenido = false;
        for (int i = 0; i < limpio.length(); i++) {
            char caracter = limpio.charAt(i);
            if (!Character.isWhitespace(caracter) && !Character.isSpaceChar(caracter)) {
                tieneContenido = true;
            }
            if (Character.isISOControl(caracter) && caracter != '\n' && caracter != '\r' && caracter != '\t') {
                throw new IllegalArgumentException(campo + " contiene un carácter no permitido.");
            }
        }
        if (!tieneContenido) throw new IllegalArgumentException(campo + " es obligatorio.");
        if (limpio.length() > maximo) {
            throw new IllegalArgumentException(campo + " admite hasta " + maximo + " caracteres.");
        }
        return limpio;
    }

    public static String nombre(String valor, String campo) {
        String limpio = texto(valor, campo, 80);
        // Letras, tildes, espacios, punto, apóstrofo y guion.
        if (!limpio.matches("[\\p{L}][\\p{L}\\p{M} .’'-]*")) {
            throw new IllegalArgumentException(campo + " debe contener letras; no números ni símbolos ajenos al nombre.");
        }
        return limpio;
    }

    public static String telefono(String valor) {
        String limpio = texto(valor, "Teléfono", 20);
        if (!limpio.matches("\\+?[0-9 ()-]+")) {
            throw new IllegalArgumentException("El teléfono admite números, espacios, guiones, paréntesis y + al inicio.");
        }
        String digitos = limpio.replaceAll("[^0-9]", "");
        if (digitos.length() < 7 || digitos.length() > 15) {
            throw new IllegalArgumentException("El teléfono debe contener entre 7 y 15 dígitos.");
        }
        int abiertos = 0;
        int inicioParentesis = -1;
        for (int i = 0; i < limpio.length(); i++) {
            char caracter = limpio.charAt(i);
            if (caracter == '(') {
                if (abiertos != 0) throw new IllegalArgumentException("Revisá los paréntesis del teléfono.");
                abiertos++;
                inicioParentesis = i;
            } else if (caracter == ')') {
                if (abiertos != 1) throw new IllegalArgumentException("Revisá los paréntesis del teléfono.");
                String contenido = limpio.substring(inicioParentesis + 1, i).replace(" ", "");
                if (!contenido.matches("[0-9]+")) {
                    throw new IllegalArgumentException("Los paréntesis del teléfono deben contener dígitos.");
                }
                abiertos--;
            }
        }
        if (abiertos != 0) throw new IllegalArgumentException("Revisá los paréntesis del teléfono.");
        return limpio;
    }

    public static String correo(String valor) {
        String limpio = texto(valor, "Correo", 120);
        if (!limpio.matches("[A-Za-z0-9._%+'-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+")) {
            throw new IllegalArgumentException("Ingresá un correo válido, por ejemplo nombre@correo.com.");
        }
        String[] partes = limpio.split("@");
        String usuario = partes[0];
        if (usuario.length() > 64 || usuario.startsWith(".") || usuario.endsWith(".")
                || usuario.contains("..")) {
            throw new IllegalArgumentException("Revisá la parte del correo antes de @.");
        }
        String[] dominio = partes[1].split("\\.");
        for (String parte : dominio) {
            if (parte.startsWith("-") || parte.endsWith("-") || parte.length() > 63) {
                throw new IllegalArgumentException("Revisá el dominio del correo.");
            }
        }
        return limpio;
    }

    public static LocalTime hora(String valor) {
        if (valor == null || !valor.trim().matches("[0-9]{2}:[0-9]{2}")) {
            throw new IllegalArgumentException("Ingresá la hora en formato HH:mm, por ejemplo 09:30.");
        }
        String[] partes = valor.trim().split(":");
        int horas = Integer.parseInt(partes[0]);
        int minutos = Integer.parseInt(partes[1]);
        if (horas > 23 || minutos > 59) {
            throw new IllegalArgumentException("La hora debe estar entre 00:00 y 23:59.");
        }
        return LocalTime.of(horas, minutos);
    }

    public static boolean estadoPermitido(String estado) {
        for (String opcion : ESTADOS) {
            if (opcion.equals(estado)) return true;
        }
        return false;
    }

    public static void id(int id, String entidad) {
        if (id <= 0) throw new IllegalArgumentException("Seleccioná un registro válido de " + entidad + ".");
    }

    public static void fecha(LocalDate fecha) {
        if (fecha == null) throw new IllegalArgumentException("La fecha es obligatoria.");
        if (fecha.getYear() < 1 || fecha.getYear() > 9999) {
            throw new IllegalArgumentException("El año de la cita debe estar entre 1 y 9999.");
        }
    }

    public static void cliente(Cliente c) {
        if (c == null) throw new IllegalArgumentException("Faltan los datos del cliente.");
        nombre(c.getNombres(), "Nombres");
        nombre(c.getApellidos(), "Apellidos");
        telefono(c.getTelefono());
        correo(c.getCorreo());
    }

    public static void cita(CitaTecnica c) {
        if (c == null) throw new IllegalArgumentException("Faltan los datos de la cita.");
        if (c.getClienteId() <= 0) throw new IllegalArgumentException("Seleccioná un cliente existente.");
        fecha(c.getFecha());
        if (c.getHora() == null) throw new IllegalArgumentException("La hora es obligatoria.");
        if (c.getHora().getSecond() != 0 || c.getHora().getNano() != 0) {
            throw new IllegalArgumentException("La hora se registra con precisión de minutos (HH:mm).");
        }
        texto(c.getTipoServicio(), "Servicio", 80);
        texto(c.getDescripcion(), "Descripción", 255);
        if (!estadoPermitido(c.getEstado())) throw new IllegalArgumentException("Seleccioná un estado permitido.");
    }
}
