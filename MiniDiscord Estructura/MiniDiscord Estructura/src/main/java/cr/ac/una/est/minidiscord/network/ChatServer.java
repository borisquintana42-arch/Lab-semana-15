package cr.ac.una.est.minidiscord.network;

import cr.ac.una.est.minidiscord.business.model.Message;
import cr.ac.una.est.minidiscord.business.service.ChatService;
import cr.ac.una.est.minidiscord.data.dao.JdbcMessageRepository;
import cr.ac.una.est.minidiscord.data.dao.MessageRepository;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//servidor central del minidiscord
public final class ChatServer {

    private final List<ClientHandler> clientes = new CopyOnWriteArrayList<>();
    private final ChatService servicio = new ChatService();
    private final MessageRepository repositorio = new JdbcMessageRepository();


    //en el servidor indica el puerto que vamos a usar
    public void iniciar() throws IOException {
        try (ServerSocket servidor = new ServerSocket(ChatProtocol.PUERTO)) {
            System.out.println("Mini-Discord servidor iniciado en el puerto " + ChatProtocol.PUERTO);
            System.out.println("Cierre el servidor con Ctrl+C cuando termine la práctica.");

            while (true) {


                //el socket acepta al servidor
                Socket socket = servidor.accept();

                //socketadress ayuda a que todos nos conectemos
                System.out.println("Nueva conexión desde " + socket.getRemoteSocketAddress());

                ClientHandler manejador = new ClientHandler(socket, this, servicio);
                Thread hilo = new Thread(manejador, "cliente-minidiscord-" + socket.getPort());
                hilo.start();
            }
        }
    }


    //envía el historial y registrar al cliente
    synchronized void registrar(ClientHandler cliente) {
        List<Message> historial = repositorio.obtenerTodos();
        cliente.enviar("HISTORY_BEGIN");
        for (Message mensaje : historial) {
            cliente.enviar(ChatProtocol.codificar(mensaje));
        }
        clientes.add(cliente);
        cliente.enviar("READY");
        System.out.println(cliente.getUsuario().getUsername() + " está conectado. Clientes: " + clientes.size());
    }

    synchronized void publicar(ClientHandler remitente, Message mensaje) {

        repositorio.guardar(mensaje);

        //a como se lo muestra al usuario, lo guarde de diferente para que sea aceptable para el servidor
        String linea = ChatProtocol.codificar(mensaje);

        for (ClientHandler cliente : clientes) {
            if (!cliente.enviar(linea)) {

                clientes.remove(cliente);
            }
        }
        System.out.println("Mensaje guardado de " + remitente.getUsuario().getUsername());
    }


    //alguien se salió del chat, avisa y que no cierre la conexion de los demas usuarios
    void quitar(ClientHandler cliente) {
        clientes.remove(cliente);
        if (cliente.getUsuario() != null) {
            System.out.println(cliente.getUsuario().getUsername() + " salió. Clientes: " + clientes.size());
        }
    }


    //indica que se inició el servidor
    public static void main(String[] args) {
        try {
            new ChatServer().iniciar();
        } catch (IOException e) {
            System.err.println("No se pudo iniciar el servidor. ¿El puerto 5050 está ocupado? " + e.getMessage());
        }
    }
}