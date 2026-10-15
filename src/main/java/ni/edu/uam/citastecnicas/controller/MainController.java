package ni.edu.uam.citastecnicas.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;
import javafx.stage.Window;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.net.URL;
import ni.edu.uam.citastecnicas.dao.*;
import ni.edu.uam.citastecnicas.db.ConexionDB;
import ni.edu.uam.citastecnicas.model.*;
import ni.edu.uam.citastecnicas.util.Alertas;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MainController {
    @FXML private AnchorPane contenido, clientes, citas;
    @FXML private Button btnClientes, btnCitas;
    @FXML private ImageView imgLogo;
    @FXML private ProgressIndicator indicador;
    @FXML private ClienteController clientesController;
    @FXML private CitaController citasController;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final CitaTecnicaDAO citaDAO = new CitaTecnicaDAO();
    private Thread hiloOperacion;
    private boolean ocupado;
    private boolean cerrado;

    @FXML
    private void initialize() {
        clientesController.configurar(this);
        citasController.configurar(this);
        mostrarClientes();

        URL logo = MainController.class.getResource(
            "/images/logo-tecnoasistencia.png"
        );

        if (logo != null) {
            imgLogo.setImage(new Image(logo.toExternalForm()));
        }
    }

    @FXML
    public void mostrarClientes() {
        clientes.setVisible(true);
        citas.setVisible(false);
        btnClientes.getStyleClass().remove("nav-selected");
        btnClientes.getStyleClass().add("nav-selected");
        btnCitas.getStyleClass().remove("nav-selected");
    }

    @FXML
    public void mostrarCitas() {
        clientes.setVisible(false);
        citas.setVisible(true);
        btnCitas.getStyleClass().remove("nav-selected");
        btnCitas.getStyleClass().add("nav-selected");
        btnClientes.getStyleClass().remove("nav-selected");
    }

    public void iniciar() {
        actualizar();
    }

    @FXML
    public void actualizar() {
        // Capturar los controles antes de entrar al hilo de trabajo.
        String busqueda = clientesController.busqueda();
        String estado = citasController.estadoFiltro();
        LocalDate fecha = citasController.fechaFiltro();

        ejecutarEnSegundoPlano(() -> {
            try {
                if (!ConexionDB.comprobar()) {
                    throw new SQLException("No se pudo comprobar la conexión.");
                }
                List<Cliente> todos = clienteDAO.buscar("");
                List<Cliente> visibles = todos;
                if (!busqueda.isEmpty()) {
                    visibles = clienteDAO.buscar(busqueda);
                }
                List<CitaTecnica> citas = citaDAO.filtrar(estado, fecha);
                // Una variable final permite utilizar el resultado dentro de runLater.
                final List<Cliente> resultado = visibles;
                Platform.runLater(() -> {
                    if (cerrado) return;
                    clientesController.mostrar(resultado);
                    citasController.mostrar(citas, todos);
                    liberarControles();

                });
            } catch (SQLException e) {
                operacionFallida(e);
            }
        });
    }

    public void refrescar() {
        actualizar();
    }

    public void ejecutarEnSegundoPlano(Runnable trabajo) {
        if (ocupado || cerrado) return;
        ocupado = true;
        contenido.setDisable(true);
        btnClientes.setDisable(true);
        btnCitas.setDisable(true);
        indicador.setVisible(true);

        // Thread y Runnable, igual que la práctica de hilos.
        hiloOperacion = new Thread(() -> {
            try {
                if (!Thread.currentThread().isInterrupted()) {
                    trabajo.run();
                }
            } catch (Exception e) {
                operacionFallida(e);
            }
        });
        hiloOperacion.setName("Hilo-JDBC-Citas");
        hiloOperacion.setDaemon(true);
        hiloOperacion.start();
    }

    public void operacionCorrecta(String mensaje, Runnable limpiarFormulario) {
        Platform.runLater(() -> {
            if (cerrado) return;
            limpiarFormulario.run();
            liberarControles();
            Alertas.mostrar(ventana(), Alert.AlertType.INFORMATION,
                    "Operación realizada", mensaje);
            // El guardado ya se confirmó: un error de recarga no lo deshace.
            actualizar();
        });
    }

    public void operacionFallida(Exception error) {
        Platform.runLater(() -> {
            if (cerrado) return;
            liberarControles();
            Alert.AlertType tipo = Alert.AlertType.ERROR;
            if (error instanceof IllegalArgumentException) tipo = Alert.AlertType.WARNING;
            if (error instanceof SQLException) {
                String codigo = ((SQLException) error).getSQLState();
                if ("CLIENTE_CON_CITAS".equals(codigo) || "CITA_DUPLICADA".equals(codigo)) {
                    tipo = Alert.AlertType.WARNING;
                }
            }
            Alertas.mostrar(ventana(), tipo, "Operación rechazada", Alertas.mensaje(error));
        });
    }

    private void liberarControles() {
        ocupado = false;
        contenido.setDisable(false);
        btnClientes.setDisable(false);
        btnCitas.setDisable(false);
        indicador.setVisible(false);
    }

    public Window ventana() {
        return contenido.getScene().getWindow();
    }

    public void validar(Runnable accion) {
        try {
            accion.run();
        } catch (IllegalArgumentException e) {
            Alertas.mostrar(ventana(), Alert.AlertType.WARNING, "Revisá los datos", e.getMessage());
        }
    }

    public void cerrar() {
        cerrado = true;
        if (hiloOperacion != null && hiloOperacion.isAlive()) {
            hiloOperacion.interrupt();
        }
    }
}
