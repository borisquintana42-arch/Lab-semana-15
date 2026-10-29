package cr.ac.una.est.minidiscord.data.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Lee la URL y las credenciales de MySQL desde config.properties, fuera del código fuente. */
public final class DatabaseConfig {
    private static final String ARCHIVO = "config.properties";
    private final String url;
    private final String usuario;
    private final String clave;

    private DatabaseConfig(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    /** Carga el archivo de propiedades y verifica que los datos esenciales estén presentes. */
    public static DatabaseConfig cargar() {
        Properties props = new Properties();
        try (InputStream entrada = new FileInputStream(ARCHIVO)) {
            props.load(entrada);
        } catch (IOException e) {
            throw new DataAccessException("No se encontró " + ARCHIVO
                    + ". Copie config.example.properties como " + ARCHIVO + " y complete sus datos.", e);
        }
        return new DatabaseConfig(
                requerida(props, "db.url"),
                requerida(props, "db.user"),
                props.getProperty("db.password", ""));
    }

    /** Obtiene una propiedad obligatoria y muestra un error claro si está vacía. */
    private static String requerida(Properties props, String nombre) {
        String valor = props.getProperty(nombre);
        if (valor == null || valor.isBlank()) {
            throw new DataAccessException("Falta la propiedad " + nombre + " en " + ARCHIVO + ".", null);
        }
        return valor.trim();
    }

    /** Abre una conexión JDBC; solo la capa de datos debe solicitarla. */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }
}