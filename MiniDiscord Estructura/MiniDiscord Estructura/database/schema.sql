CREATE DATABASE IF NOT EXISTS minidiscord
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE minidiscord;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_usuarios_username (username)
);

CREATE TABLE IF NOT EXISTS mensajes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id INT NOT NULL,
    contenido VARCHAR(500) NOT NULL,
    fecha_envio DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mensajes_usuarios
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);
