package cr.ac.una.est.minidiscord.network;

import cr.ac.una.est.minidiscord.business.model.Message;
import cr.ac.una.est.minidiscord.business.model.User;
import cr.ac.una.est.minidiscord.business.service.ChatService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


//cliente handler se asegura la conexion, si un usuario se salió no arruine la sesion de los demás usuarios
final class ClientHandler implements Runnable {
    private final Socket conexion;
    private final ChatServer servidor;
    private final ChatService servicio;
    private PrintWriter salida;
    private User usuario;

    ClientHandler(Socket conexion, ChatServer servidor, ChatService servicio) {
        this.conexion = conexion;
        this.servidor = servidor;
        this.servicio = servicio;
    }

    User getUsuario() {
        return usuario;
    }

    @Override
    public void run() {

        try (Socket socket = conexion;
             BufferedReader entrada = new BufferedReader(new InputStreamReader(
                     socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

            salida = out;


            //siempre hay que saber quién envio el mensaje
            String saludo = entrada.readLine();
            if (saludo == null || !saludo.startsWith("HELLO|")) {
                enviar("ERR|Primero debe identificarse con un nombre de usuario.");
                return;
            }

            try {
                usuario = servicio.createUser(saludo.substring("HELLO|".length()));
            } catch (IllegalArgumentException e) {
                enviar("ERR|" + e.getMessage());
                return;
            }

            try {
                servidor.registrar(this);
            } catch (RuntimeException e) {
                enviar("ERR|No se pudo cargar el historial de mensajes.");
                System.err.println("No se pudo registrar a " + usuario.getUsername() + ": " + e.getMessage());
                return;
            }

            String linea;

            while ((linea = entrada.readLine()) != null) {
                if (linea.startsWith("SEND|")) {
                    procesarSolicitud(linea.substring("SEND|".length()));
                } else {
                    enviar("ERR|Instrucción desconocida.");
                }
            }
        } catch (IOException e) {
            System.out.println("Conexión terminada: "
                    + (usuario == null ? "cliente sin identificar" : usuario.getUsername()));
        } finally {

            servidor.quitar(this);
        }
    }

    private void procesarSolicitud(String texto) {
        try {
            Message mensaje = servicio.processMessage(usuario, texto);
            servidor.publicar(this, mensaje);
        } catch (IllegalArgumentException e) {
            enviar("ERR|" + e.getMessage());
        } catch (RuntimeException e) {
            enviar("ERR|No se pudo guardar el mensaje. Revise la conexión con MySQL.");
            System.err.println("Error al publicar mensaje: " + e.getMessage());
        }
    }


    //sincroniza de que siempre estén los mensajes separados
    synchronized boolean enviar(String linea) {
        if (salida == null) return false;
        salida.println(linea);
        return !salida.checkError();
    }
}
