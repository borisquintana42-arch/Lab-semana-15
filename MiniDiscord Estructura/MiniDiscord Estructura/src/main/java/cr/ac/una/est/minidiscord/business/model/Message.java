package cr.ac.una.est.minidiscord.business.model;

import java.time.LocalDateTime;
import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private final User author;
    private final String content;
    private final LocalDateTime sentAt;

    public Message(User author, String content) {
        this(author, content, LocalDateTime.now());
    }
    public Message(User author, String content, LocalDateTime sentAt) {
        this.author = author;
        this.content = content;
        this.sentAt = sentAt;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
    @Override
    public String toString() {
        return author.getUsername() + ": " + content;
    }
}
