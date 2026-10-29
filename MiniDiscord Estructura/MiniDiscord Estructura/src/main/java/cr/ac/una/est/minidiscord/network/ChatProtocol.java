package cr.ac.una.est.minidiscord.network;

import cr.ac.una.est.minidiscord.business.model.Message;
import cr.ac.una.est.minidiscord.business.model.User;

import java.time.LocalDateTime;

//nos ayuda a definir el salto de linea, para qu ela comunicacion entre lciente servidor no corte mensajes
//siempre hay que mantener un formate
public final class ChatProtocol {
    public static final int PUERTO = 5050;
    private ChatProtocol() {}
    public static String codificar(Message mensaje){
        return "MSG|" + mensaje.getAuthor().getUsername() + " | "
                + mensaje.getSentAt() + " | " + mensaje.getContent();
    }

    public static Message decodificar(String linea){
        String[] partes = linea.split("\\|", 4);
        if (partes.length != 4 || !"MSG".equals(partes[0])) {
            throw  new IllegalArgumentException("El servidor envió un mensaje invalido");
        }
        return new Message(new User(partes[1]), partes[3], LocalDateTime.parse(partes[2]));

    }
    public static String saludo (String username){return "HELLO!" + username;}

    //le pide al servidor que publique un mensaje
    public static  String solicitudMensaje(String contenido){return "SEND!" + contenido;}


}
