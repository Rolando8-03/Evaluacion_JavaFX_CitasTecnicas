package ni.edu.uam.citastecnicas.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.*;
import javafx.stage.Window;
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
    @FXML private Label lblConexion, lblActividad;
    @FXML private Button btnActualizar;
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

        ejecutarEnSegundoPlano("Cargando registros…", () -> {
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
                    lblConexion.setText("PostgreSQL conectado");
                    lblConexion.getStyleClass().remove("offline");
                    lblActividad.setText("Datos actualizados · " + todos.size()
                            + " clientes · " + citas.size() + " citas en esta consulta");
                });
            } catch (SQLException e) {
                operacionFallida(e);
            }
        });
    }

    public void refrescar() {
        actualizar();
    }

    public void ejecutarEnSegundoPlano(String mensaje, Runnable trabajo) {
        if (ocupado || cerrado) return;
        ocupado = true;
        contenido.setDisable(true);
        btnClientes.setDisable(true);
        btnCitas.setDisable(true);
        btnActualizar.setDisable(true);
        indicador.setVisible(true);
        lblActividad.setText(mensaje);

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
            lblActividad.setText("Operación pendiente de revisar · actualizá antes de repetir");
            if (error instanceof SQLException) {
                SQLException sql = (SQLException) error;
                String codigo = sql.getSQLState();
                if (codigo == null || codigo.startsWith("08") || codigo.startsWith("28")
                        || codigo.equals("3D000") || codigo.equals("CONFIG")) {
                    lblConexion.setText("Revisá la conexión");
                    if (!lblConexion.getStyleClass().contains("offline")) {
                        lblConexion.getStyleClass().add("offline");
                    }
                }
            }
            Alertas.mostrar(ventana(), Alert.AlertType.ERROR,
                    "No se completó la operación", Alertas.mensaje(error));
        });
    }

    private void liberarControles() {
        ocupado = false;
        contenido.setDisable(false);
        btnClientes.setDisable(false);
        btnCitas.setDisable(false);
        btnActualizar.setDisable(false);
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
