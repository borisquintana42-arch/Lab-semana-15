package cr.ac.una.est.minidiscord;

import cr.ac.una.est.minidiscord.business.model.User;
import cr.ac.una.est.minidiscord.business.service.ChatService;
import cr.ac.una.est.minidiscord.network.ChatProtocol;
import cr.ac.una.est.minidiscord.presentation.controller.ChatViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class App extends Application {
    private static final String HOJA_ESTILOS = "/cr/ac/una/est/minidiscord/presentation/view/discord.css";

    @Override
    public void start(Stage stage) throws IOException {

        User usuario = pedirUsuario();
        if (usuario == null) return;

        String host = pedirHost();
        if (host == null) return;

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(
                "/cr/ac/una/est/minidiscord/presentation/view/chat-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960, 600);
        scene.getStylesheets().add(App.class.getResource(HOJA_ESTILOS).toExternalForm());

        ChatViewController controller = fxmlLoader.getController();
        controller.setCurrentUser(usuario);

        stage.setTitle("Mini-Discord UNA");
        stage.setMinWidth(640);
        stage.setMinHeight(400);
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> controller.closeConnection());
        stage.show();

        controller.connectToServer(host, ChatProtocol.PUERTO);
    }

    private User pedirUsuario() {
        ChatService servicio = new ChatService();
        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("Mini-Discord UNA");
        dialogo.setHeaderText("Elija su nombre de usuario");
        dialogo.setContentText("Usuario:");
        dialogo.getDialogPane().getStylesheets().add(App.class.getResource(HOJA_ESTILOS).toExternalForm());

        while (true) {
            Optional<String> respuesta = dialogo.showAndWait();
            if (respuesta.isEmpty()) return null;
            try {
                return servicio.createUser(respuesta.get());
            } catch (IllegalArgumentException e) {
                dialogo.setHeaderText(e.getMessage());
            }
        }
    }

    private String pedirHost() {
        TextInputDialog dialogo = new TextInputDialog("127.0.0.1");
        dialogo.setTitle("Mini-Discord UNA");
        dialogo.setHeaderText("¿Dónde está el servidor?");
        dialogo.setContentText("IP o nombre del equipo (127.0.0.1 para esta computadora):");
        dialogo.getDialogPane().getStylesheets().add(App.class.getResource(HOJA_ESTILOS).toExternalForm());
        Optional<String> respuesta = dialogo.showAndWait();
        if (respuesta.isEmpty() || respuesta.get().isBlank()) return null;
        return respuesta.get().trim();
    }

    public static void main(String[] args) {
        launch(args);
    }
}