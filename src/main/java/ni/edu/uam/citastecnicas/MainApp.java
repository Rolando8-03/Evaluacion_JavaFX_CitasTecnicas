package ni.edu.uam.citastecnicas;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.citastecnicas.controller.MainController;

public final class MainApp extends Application {
    private MainController controller;
    @Override public void start(Stage stage) throws Exception {
        FXMLLoader loader=new FXMLLoader(MainApp.class.getResource("/fxml/main-view.fxml"));
        Scene scene=new Scene(loader.load(),1180,800);
        scene.getStylesheets().add(MainApp.class.getResource("/css/app.css").toExternalForm());
        controller=loader.getController();
        stage.setTitle("TecnoAsistencia | Gestión de citas técnicas");
        stage.setMinWidth(1140); stage.setMinHeight(800); stage.setScene(scene);
        stage.setOnCloseRequest(event -> controller.cerrar());
        stage.show(); controller.iniciar();
    }
    @Override public void stop() { if (controller!=null) controller.cerrar(); }
    public static void main(String[] args) { launch(args); }
}
