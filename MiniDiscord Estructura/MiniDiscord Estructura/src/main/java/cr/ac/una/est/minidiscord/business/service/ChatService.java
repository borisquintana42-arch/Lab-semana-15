package cr.ac.una.est.minidiscord.business.service;

import cr.ac.una.est.minidiscord.business.model.Message;
import cr.ac.una.est.minidiscord.business.model.User;

public class ChatService {

    public static final int MAX_USERNAME = 50;
    public static final int MAX_MESSAGE = 500;

    public User createUser(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario no puede estar vacío.");
        }
        String limpio = name.trim();
        if (limpio.length() > MAX_USERNAME) {
            throw new IllegalArgumentException("El nombre no puede superar los " + MAX_USERNAME + " caracteres.");
        }

        if (limpio.contains("|") || limpio.contains("\n") || limpio.contains("\r")) {
            throw new IllegalArgumentException("El nombre no puede contener | ni saltos de línea.");
        }
        return new User(limpio);
    }

    public Message processMessage(User sender, String text) {
        if (sender == null) throw new IllegalArgumentException("Debe identificarse antes de enviar mensajes.");
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío.");
        }
        String limpio = text.trim();
        if (limpio.length() > MAX_MESSAGE) {
            throw new IllegalArgumentException("El mensaje no puede superar los " + MAX_MESSAGE + " caracteres.");
        }

        if (limpio.contains("\n") || limpio.contains("\r")) {
            throw new IllegalArgumentException("El mensaje debe ocupar una sola línea.");
        }
        return new Message(sender, limpio);
    }
}