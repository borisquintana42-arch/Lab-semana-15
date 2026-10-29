package cr.ac.una.est.minidiscord.data.dao;
import cr.ac.una.est.minidiscord.business.model.Message;

import javax.imageio.IIOException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {
    private static final String ARCHIVO = "mensaje.dat";

    public void guardarTodos(List<Message> mensajes) {
        try (ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(ARCHIVO))) {
            salida.writeObject(new ArrayList<>(mensajes));
        }catch (Exception e) {
            System.out.println("No se pudo leer el historial local: "+ e.getMessage());
        }
    }
    public List<Message> cargarTodos(List<Message> mensajes) {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) return new ArrayList<>();
        try (ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(ARCHIVO))) {
            return (List<Message>) entrada.readObject();
        }catch (IOException | ClassNotFoundException e) {
            System.out.println("No se pudo leer el historial local: "+ e.getMessage());
            return new ArrayList<>();
        }
    }
}



