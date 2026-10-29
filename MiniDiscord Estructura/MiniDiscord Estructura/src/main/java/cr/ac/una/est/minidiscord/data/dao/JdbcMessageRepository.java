package cr.ac.una.est.minidiscord.data.dao;

import cr.ac.una.est.minidiscord.business.model.Message;
import cr.ac.una.est.minidiscord.business.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcMessageRepository implements MessageRepository {
    private volatile DatabaseConfig config;

    private Connection abrirConexion() throws SQLException {
        if (config == null) {
            synchronized (this) {
                if (config == null) config = DatabaseConfig.cargar();
            }
        }
        return config.getConnection();
    }

    private int obtenerOcrearUsuario(Connection con, String username) throws SQLException {

        try (PreparedStatement buscar = con.prepareStatement(
                "SELECT id FROM usuarios WHERE username = ?")) {
            buscar.setString(1, username);
            try (ResultSet rs = buscar.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }

        try (PreparedStatement crear = con.prepareStatement(
                "INSERT INTO usuarios (username) VALUES (?)",
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            crear.setString(1, username);
            crear.executeUpdate();
            try (ResultSet claves = crear.getGeneratedKeys()) {
                if (claves.next()) return claves.getInt(1);
            }
        }
        throw new SQLException("MySQL no devolvió el id del usuario.");
    }

    @Override
    public void guardar(Message mensaje) {
        String sql = "INSERT INTO mensajes (usuario_id, contenido, fecha_envio) VALUES (?, ?, ?)";
        try (Connection con = abrirConexion()) {
            int usuarioId = obtenerOcrearUsuario(con, mensaje.getAuthor().getUsername());
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setInt(1, usuarioId);
                stmt.setString(2, mensaje.getContent());
                stmt.setTimestamp(3, Timestamp.valueOf(mensaje.getSentAt()));
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo guardar en la base de datos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Message> obtenerTodos() {
        List<Message> resultado = new ArrayList<>();
        String sql = "SELECT u.username, m.contenido, m.fecha_envio "
                + "FROM mensajes m JOIN usuarios u ON m.usuario_id = u.id "
                + "ORDER BY m.fecha_envio, m.id";

        try (Connection con = abrirConexion();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User autor = new User(rs.getString("username"));
                Timestamp fecha = rs.getTimestamp("fecha_envio");
                LocalDateTime enviado = fecha == null ? LocalDateTime.now() : fecha.toLocalDateTime();
                resultado.add(new Message(autor, rs.getString("contenido"), enviado));
            }
        } catch (SQLException e) {
            throw new DataAccessException("No se pudo leer el historial: " + e.getMessage(), e);
        }
        return resultado;
    }
}