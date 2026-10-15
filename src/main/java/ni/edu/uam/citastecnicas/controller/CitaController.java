package ni.edu.uam.citastecnicas.controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.citastecnicas.dao.CitaTecnicaDAO;
import ni.edu.uam.citastecnicas.model.*;
import ni.edu.uam.citastecnicas.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.sql.SQLException;

public final class CitaController {
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private ComboBox<String> cmbEstado,cmbFiltroEstado;
    @FXML private DatePicker dpFecha,dpFiltroFecha;
    @FXML private TextField txtHora,txtServicio;
    @FXML private TextArea txtDescripcion;
    @FXML private Label lblFormulario,lblTotal;
    @FXML private Button btnEditar,btnEliminar;
    @FXML private TableView<CitaTecnica> tblCitas;
    @FXML private TableColumn<CitaTecnica,Number> colId;
    @FXML private TableColumn<CitaTecnica,String> colCliente,colFecha,colHora,colServicio,colEstado;
    private MainController principal;
    private int seleccionado;
    private final CitaTecnicaDAO dao=new CitaTecnicaDAO();
    @FXML private void initialize() {
        cmbEstado.setItems(FXCollections.observableArrayList(Validacion.ESTADOS));
        cmbFiltroEstado.getItems().add("Todos"); cmbFiltroEstado.getItems().addAll(Validacion.ESTADOS);
        cmbFiltroEstado.setValue("Todos");
        // Elegir en calendario evita interpretar silenciosamente fechas mal escritas.
        dpFecha.setEditable(false); dpFiltroFecha.setEditable(false);
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colCliente.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCliente()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHora().format(DateTimeFormatter.ofPattern("HH:mm"))));
        colServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoServicio()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        tblCitas.setPlaceholder(new Label("No hay citas para estos filtros."));
        tblCitas.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> { if (b!=null) seleccionar(b); });
        nuevo();
    }
    public void configurar(MainController principal) { this.principal=principal; }
    public String estadoFiltro() { String e=cmbFiltroEstado.getValue(); return "Todos".equals(e)?null:e; }
    public LocalDate fechaFiltro() { return dpFiltroFecha.getValue(); }
    public void mostrar(List<CitaTecnica> citas,List<Cliente> clientes) {
        Cliente previo=cmbCliente.getValue(); int id=seleccionado;
        cmbCliente.setItems(FXCollections.observableArrayList(clientes));
        if (previo != null) cmbCliente.setValue(buscarCliente(previo.getId()));
        tblCitas.setItems(FXCollections.observableArrayList(citas)); lblTotal.setText(citas.size()+" citas encontradas");
        if (id!=0) {
            CitaTecnica encontrada = null;
            for (CitaTecnica cita : citas) {
                if (cita.getId() == id) { encontrada = cita; break; }
            }
            if (encontrada==null) nuevo(); else tblCitas.getSelectionModel().select(encontrada);
        }
    }
    private Cliente buscarCliente(int id) {
        for (Cliente cliente : cmbCliente.getItems()) {
            if (cliente.getId() == id) return cliente;
        }
        return null;
    }
    private void seleccionar(CitaTecnica c) {
        seleccionado=c.getId();
        cmbCliente.setValue(buscarCliente(c.getClienteId()));
        dpFecha.setValue(c.getFecha()); txtHora.setText(c.getHora().format(DateTimeFormatter.ofPattern("HH:mm")));
        txtServicio.setText(c.getTipoServicio()); txtDescripcion.setText(c.getDescripcion()); cmbEstado.setValue(c.getEstado());
        lblFormulario.setText("Editando cita #"+seleccionado); btnEditar.setDisable(false); btnEliminar.setDisable(false);
    }
    @FXML public void nuevo() {
        seleccionado=0; cmbCliente.setValue(null); dpFecha.setValue(null); txtHora.clear();
        txtServicio.clear(); txtDescripcion.clear(); cmbEstado.setValue("Pendiente");
        lblFormulario.setText("Nueva cita técnica"); tblCitas.getSelectionModel().clearSelection();
        btnEditar.setDisable(true); btnEliminar.setDisable(true);
    }
    private CitaTecnica datos(int id) {
        Cliente cl=cmbCliente.getValue();
        if (cl==null) throw new IllegalArgumentException("Seleccioná un cliente existente. Si la lista está vacía, registrá primero un cliente.");
        CitaTecnica c=new CitaTecnica(id,cl.getId(),cl.getNombreCompleto(),dpFecha.getValue(),Validacion.hora(txtHora.getText()),
            txtServicio.getText(),txtDescripcion.getText(),cmbEstado.getValue());
        Validacion.cita(c); return c;
    }
    @FXML private void guardar() { principal.validar(() -> procesar("registrar")); }
    @FXML private void editar() { principal.validar(() -> procesar("editar")); }
    @FXML private void eliminar() { principal.validar(() -> procesar("eliminar")); }

    private void procesar(String accion) {
        if (accion.equals("registrar") && seleccionado != 0) {
            throw new IllegalArgumentException("Para modificar esta cita usá Guardar cambios. Para crear otra, presioná Nueva / limpiar.");
        }
        if (!accion.equals("registrar") && seleccionado == 0) {
            throw new IllegalArgumentException("Seleccioná una cita de la tabla.");
        }
        int id = seleccionado;
        CitaTecnica cita = null;
        if (!accion.equals("eliminar")) cita = datos(id);
        if (accion.equals("eliminar") && !Alertas.confirmar(principal.ventana(),
                "¿Eliminar físicamente la cita #" + id + "? Esta acción no se puede deshacer.")) {
            return;
        }
        final CitaTecnica datosCita = cita;
        principal.ejecutarEnSegundoPlano("Procesando cita…", () -> {
            try {
                String mensaje;
                if (accion.equals("registrar")) {
                    dao.insertar(datosCita);
                    mensaje = "Cita registrada correctamente.";
                } else if (accion.equals("editar")) {
                    dao.actualizar(datosCita);
                    mensaje = "Cita actualizada correctamente.";
                } else {
                    dao.eliminar(id);
                    mensaje = "Cita eliminada correctamente.";
                }
                principal.operacionCorrecta(mensaje, () -> nuevo());
            } catch (SQLException | IllegalArgumentException e) {
                principal.operacionFallida(e);
            }
        });
    }
    @FXML private void filtrar() { principal.refrescar(); }
    @FXML private void limpiarFiltros() { cmbFiltroEstado.setValue("Todos"); dpFiltroFecha.setValue(null); principal.refrescar(); }
}
