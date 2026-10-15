package ni.edu.uam.citastecnicas.util;

import javafx.scene.control.*;
import javafx.stage.Window;
import java.sql.SQLException;

public class Alertas {
    private Alertas() {}

    public static void mostrar(Window ventana, Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.initOwner(ventana);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    public static boolean confirmar(Window ventana, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.initOwner(ventana);
        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        ButtonType eliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alerta.getButtonTypes().setAll(eliminar, cancelar);
        // Cancelar es la opción inicialmente enfocada: Enter no confirma un borrado accidental.
        ((Button) alerta.getDialogPane().lookupButton(eliminar)).setDefaultButton(false);
        ((Button) alerta.getDialogPane().lookupButton(cancelar)).setDefaultButton(true);
        return alerta.showAndWait().orElse(cancelar) == eliminar;
    }

    public static String mensaje(Throwable error) {
        if (error instanceof IllegalArgumentException) return error.getMessage();
        if (error instanceof SQLException) {
            SQLException sql = (SQLException) error;
            String estado = sql.getSQLState();
            if (estado == null) estado = "sin código";
            if (estado.equals("23505")) {
                return "Ese correo ya está registrado. Usá un correo diferente.";
            } else if (estado.equals("CLIENTE_CON_CITAS")) {
                return "No podés eliminar este cliente porque tiene citas asociadas. Eliminá primero sus citas, confirmando cada operación.";
            } else if (estado.equals("23503")) {
                return "El cliente ya no existe o tiene citas relacionadas. Actualizá la lista; no se puede eliminar un cliente con citas.";
            } else if (estado.equals("23514") || estado.equals("23502") || estado.equals("22001") || estado.equals("22021")) {
                return "Revisá campos obligatorios, longitudes, fecha, hora y estado permitido.";
            } else if (estado.equals("28P01") || estado.equals("28000")) {
                return "PostgreSQL rechazó las credenciales. Revisá el usuario y la contraseña en conf/db.properties.";
            } else if (estado.equals("3D000")) {
                return "No existe la base configurada. Creá gestion_citas y revisá db.url.";
            } else if (estado.equals("42P01") || estado.equals("42703")) {
                return "Faltan tablas o columnas. Ejecutá gestion_citas.sql en la base correcta.";
            } else if (estado.equals("42501")) {
                return "El usuario de PostgreSQL no tiene permisos para esa operación. Revisá los permisos con el administrador de la base.";
            } else if (estado.equals("CONFIG") || estado.equals("P0002")) {
                return sql.getMessage();
            } else {
                return "No se pudo completar la operación en PostgreSQL (código " + estado
                        + "). Revisá servicio, host, puerto y configuración. Si se perdió la conexión al guardar, actualizá antes de repetir.";
            }
        }
        return "No se pudo completar la operación. Revisá los datos e intentá nuevamente.";
    }
}
