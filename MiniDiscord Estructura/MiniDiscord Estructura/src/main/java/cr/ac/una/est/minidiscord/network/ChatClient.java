package cr.ac.una.est.minidiscord.network;

import cr.ac.una.est.minidiscord.business.model.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public final class ChatClient implements AutoCloseable {
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private volatile boolean conectado;

    public void conectar(String host, int puerto, String username,
                         Consumer<Message> alRecibirMensaje,
                         Consumer<String> alCambiarEstado,
                         Consumer<String> alRecibirError) throws IOException {
        socket = new Socket();


        //nos ayuda a que el conector tiene u limite de tiempo, para que un host no dure mucho, tiene un tiempo de 5000 segundos
        socket.connect(new InetSocketAddress(host, puerto), 5000);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

        enviarLinea(ChatProtocol.saludo(username));


        //siempre está escuchando qué está pasando
        Thread oyente = new Thread(
                () -> escuchar(alRecibirMensaje, alCambiarEstado, alRecibirError),
                "minidiscord-cliente-oyente");
        oyente.setDaemon(true);
        oyente.start();
    }


    //enviar lo que se escribió
    public void enviarMensaje(String contenido) throws IOException {
        if (!conectado) {
            throw new IOException("Todavía no hay conexión con el servidor.");
        }
        enviarLinea(ChatProtocol.solicitudMensaje(contenido));
        if (salida.checkError()) {
            throw new IOException("No se pudo enviar el mensaje al servidor.");
        }
    }


    private synchronized void enviarLinea(String linea) throws IOException {
        if (salida == null) throw new IOException("La conexión no está disponible.");
        salida.println(linea);
        if (salida.checkError()) throw new IOException("No se pudo escribir en el socket.");
    }

    private void escuchar(Consumer<Message> alRecibirMensaje,
                          Consumer<String> alCambiarEstado,
                          Consumer<String> alRecibirError) {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                if (linea.startsWith("MSG|")) {
                    try {
                        alRecibirMensaje.accept(ChatProtocol.decodificar(linea));
                    } catch (IllegalArgumentException e) {
                        alRecibirError.accept(e.getMessage());
                    }
                } else if ("READY".equals(linea)) {
                    conectado = true;
                    alCambiarEstado.accept("Conectado al servidor");
                } else if (linea.startsWith("ERR|")) {
                    alRecibirError.accept(linea.substring("ERR|".length()));
                }

            }
        } catch (IOException e) {
            if (conectado) alCambiarEstado.accept("Se perdió la conexión con el servidor.");
        } finally {
            conectado = false;
            alCambiarEstado.accept("Desconectado del servidor");
            cerrarSocket();
        }
    }

    public boolean estaConectado() {
        return conectado;
    }

    @Override
    public void close() {
        conectado = false;
        cerrarSocket();
    }

    private void cerrarSocket() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {

        }
    }
}