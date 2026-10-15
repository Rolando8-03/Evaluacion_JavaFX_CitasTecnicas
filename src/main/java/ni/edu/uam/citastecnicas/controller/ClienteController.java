package ni.edu.uam.citastecnicas.controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ni.edu.uam.citastecnicas.dao.ClienteDAO;
import ni.edu.uam.citastecnicas.model.Cliente;
import ni.edu.uam.citastecnicas.util.*;
import java.util.List;
import java.sql.SQLException;

public final class ClienteController {
    @FXML private TextField txtNombres,txtApellidos,txtTelefono,txtCorreo,txtBuscar;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblFormulario,lblTotal;
    @FXML private Button btnEditar,btnEliminar;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente,Number> colId;
    @FXML private TableColumn<Cliente,String> colNombres,colApellidos,colTelefono,colCorreo,colActivo;
    private MainController principal;
    private int seleccionado;
    private final ClienteDAO dao=new ClienteDAO();
    @FXML private void initialize() {
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        colNombres.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombres()));
        colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellidos()));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colCorreo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCorreo()));
        colActivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActivo()?"Activo":"Inactivo"));
        tblClientes.setPlaceholder(new Label("No hay clientes para esta búsqueda."));
        tblClientes.getSelectionModel().selectedItemProperty().addListener((o,a,b) -> { if (b!=null) seleccionar(b); });
        nuevo();
    }
    public void configurar(MainController principal) { this.principal=principal; }
    public String busqueda() { return txtBuscar.getText()==null ? "" : txtBuscar.getText().trim(); }
    public void mostrar(List<Cliente> clientes) {
        int id=seleccionado;
        tblClientes.setItems(FXCollections.observableArrayList(clientes));
        lblTotal.setText(clientes.size() + " clientes encontrados");
        if (id!=0) {
            Cliente encontrado = null;
            for (Cliente cliente : clientes) {
                if (cliente.getId() == id) { encontrado = cliente; break; }
            }
            if (encontrado==null) nuevo(); else tblClientes.getSelectionModel().select(encontrado);
        }
    }
    private void seleccionar(Cliente c) {
        seleccionado=c.getId(); txtNombres.setText(c.getNombres()); txtApellidos.setText(c.getApellidos());
        txtTelefono.setText(c.getTelefono()); txtCorreo.setText(c.getCorreo()); chkActivo.setSelected(c.isActivo());
        lblFormulario.setText("Editando cliente #" + seleccionado); btnEditar.setDisable(false); btnEliminar.setDisable(false);
    }
    @FXML public void nuevo() {
        seleccionado=0; txtNombres.clear(); txtApellidos.clear(); txtTelefono.clear(); txtCorreo.clear();
        chkActivo.setSelected(true); lblFormulario.setText("Nuevo cliente");
        tblClientes.getSelectionModel().clearSelection(); btnEditar.setDisable(true); btnEliminar.setDisable(true);
    }
    private Cliente datos(int id) {
        Cliente c=new Cliente(id,txtNombres.getText(),txtApellidos.getText(),txtTelefono.getText(),txtCorreo.getText(),chkActivo.isSelected());
        Validacion.cliente(c); return c;
    }
    @FXML private void guardar() { principal.validar(() -> procesar("registrar")); }
    @FXML private void editar() { principal.validar(() -> procesar("editar")); }
    @FXML private void eliminar() { principal.validar(() -> procesar("eliminar")); }

    private void procesar(String accion) {
        if (accion.equals("registrar") && seleccionado != 0) {
            throw new IllegalArgumentException("Para modificar este cliente usá Guardar cambios. Para crear otro, presioná Nuevo / limpiar.");
        }
        if (!accion.equals("registrar") && seleccionado == 0) {
            throw new IllegalArgumentException("Seleccioná un cliente de la tabla.");
        }
        int id = seleccionado;
        Cliente cliente = null;
        if (!accion.equals("eliminar")) cliente = datos(id);
        if (accion.equals("eliminar") && !Alertas.confirmar(principal.ventana(),
            "¿Eliminar físicamente al cliente #" + id + "? Si tiene citas, la eliminación será rechazada.")) {
            return;
        }
        final Cliente datosCliente = cliente;
        principal.ejecutarEnSegundoPlano("Procesando cliente…", () -> {
            try {
                String mensaje;
                if (accion.equals("registrar")) {
                    dao.insertar(datosCliente);
                    mensaje = "Cliente registrado correctamente.";
                } else if (accion.equals("editar")) {
                    dao.actualizar(datosCliente);
                    mensaje = "Cliente actualizado correctamente.";
                } else {
                    dao.eliminar(id);
                    mensaje = "Cliente eliminado correctamente.";
                }
                principal.operacionCorrecta(mensaje, () -> nuevo());
            } catch (SQLException | IllegalArgumentException e) {
                principal.operacionFallida(e);
            }
        });
    }
    @FXML private void buscar() { principal.refrescar(); }
    @FXML private void limpiarBusqueda() { txtBuscar.clear(); principal.refrescar(); }
}
