package cr.ac.una.est.minidiscord.data.dao;

import cr.ac.una.est.minidiscord.business.model.Message;

import java.util.List;

public interface MessageRepository {
    void guardar(Message message);

    List<Message> obtenerTodos();
}
