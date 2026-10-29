package cr.ac.una.est.minidiscord.presentation.component;

import cr.ac.una.est.minidiscord.business.model.Message;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.time.format.DateTimeFormatter;

public class MessageCell extends ListCell<Message> {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final double ANCHO_RESERVADO = 110;

    private final StackPane contenedorAvatar = new StackPane();
    private final Label autor = new Label();
    private final Label fecha = new Label();
    private final Label contenido = new Label();
    private final HBox raiz;

    public MessageCell() {
        autor.getStyleClass().add("message-author");
        fecha.getStyleClass().add("message-time");
        contenido.getStyleClass().add("message-content");

        contenido.setWrapText(true);
        contenido.setMinHeight(Region.USE_PREF_SIZE);

        HBox encabezado = new HBox(8, autor, fecha);
        encabezado.setAlignment(Pos.BASELINE_LEFT);

        VBox texto = new VBox(2, encabezado, contenido);
        HBox.setHgrow(texto, Priority.ALWAYS);

        raiz = new HBox(16, contenedorAvatar, texto);
        raiz.setAlignment(Pos.TOP_LEFT);

        setPrefWidth(0);
    }

    @Override
    protected void updateItem(Message mensaje, boolean vacia) {

        super.updateItem(mensaje, vacia);

        if (vacia || mensaje == null) {
            setGraphic(null);
            return;
        }

        String username = mensaje.getAuthor().getUsername();
        autor.setText(username);
        fecha.setText(mensaje.getSentAt().format(FORMATO_FECHA));
        contenido.setText(mensaje.getContent());
        contenedorAvatar.getChildren().setAll(Avatars.create(username, 20));

        contenido.maxWidthProperty().bind(getListView().widthProperty().subtract(ANCHO_RESERVADO));

        setGraphic(raiz);
    }
}