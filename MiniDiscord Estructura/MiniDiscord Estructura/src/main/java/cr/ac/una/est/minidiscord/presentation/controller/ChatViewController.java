package cr.ac.una.est.minidiscord.presentation.controller;

import cr.ac.una.est.minidiscord.business.model.Message;
import cr.ac.una.est.minidiscord.business.model.User;
import cr.ac.una.est.minidiscord.business.service.ChatService;
import cr.ac.una.est.minidiscord.network.ChatClient;
import cr.ac.una.est.minidiscord.presentation.component.Avatars;
import cr.ac.una.est.minidiscord.presentation.component.MessageCell;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class ChatViewController {
    @FXML private ListView<Message> messageListView;
    @FXML private TextField messageInputField;
    @FXML private Label statusLabel;
    @FXML private Label userNameLabel;
    @FXML private StackPane userAvatarHolder;

    private final ChatService chatService = new ChatService();
    private final ObservableList<Message> messages = FXCollections.observableArrayList();
    private final ChatClient chatClient = new ChatClient();
    private User currentUser;

    @FXML
    public void initialize() {
        messageListView.setItems(messages);
        messageListView.setCellFactory(lista -> new MessageCell());
        statusLabel.visibleProperty().bind(statusLabel.textProperty().isNotEmpty());
        statusLabel.managedProperty().bind(statusLabel.visibleProperty());
        messageInputField.setDisable(true);
        statusLabel.setText("Esperando conexión con el servidor...");
    }

    public void setCurrentUser(User user) {
        currentUser = user;
        userNameLabel.setText(user.getUsername());
        userAvatarHolder.getChildren().setAll(Avatars.create(user.getUsername(), 16));
    }

    public void connectToServer(String host, int port) {
        Task<Void> tareaConexion = new Task<>() {
            @Override
            protected Void call() throws Exception {
                chatClient.conectar(host, port, currentUser.getUsername(),
                        mensaje -> Platform.runLater(() -> agregarMensaje(mensaje)),
                        estado -> Platform.runLater(() -> actualizarEstado(estado)),
                        error -> Platform.runLater(() -> statusLabel.setText(error)));
                return null;
            }
        };

        tareaConexion.setOnFailed(event -> {
            Throwable error = tareaConexion.getException();
            statusLabel.setText("No se pudo conectar con " + host + ":" + port
                    + ". Compruebe que el servidor esté iniciado. "
                    + (error == null ? "" : error.getMessage()));
        });
        Thread hilo = new Thread(tareaConexion, "minidiscord-conexion");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void agregarMensaje(Message mensaje) {
        messages.add(mensaje);
        scrollToEnd();
    }

    private void actualizarEstado(String estado) {
        statusLabel.setText(estado);

        if ("Conectado al servidor".equals(estado)) {
            messageInputField.setDisable(false);
            messageInputField.requestFocus();
        } else if (estado.startsWith("Desconectado") || estado.startsWith("Se perdió")) {
            messageInputField.setDisable(true);
        }
    }

    @FXML
    private void handleSendMessage() {
        try {

            Message mensajeValidado = chatService.processMessage(currentUser, messageInputField.getText());
            chatClient.enviarMensaje(mensajeValidado.getContent());
            messageInputField.clear();
            statusLabel.setText("");
        } catch (IllegalArgumentException | IOException e) {
            statusLabel.setText(e.getMessage());
        }
    }

    private void scrollToEnd() {
        if (!messages.isEmpty()) messageListView.scrollTo(messages.size() - 1);
    }

    public void closeConnection() {
        chatClient.close();
    }
}