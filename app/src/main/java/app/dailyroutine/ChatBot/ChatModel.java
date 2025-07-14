package app.dailyroutine.ChatBot;

public class ChatModel {
    private String message;
    private boolean isUser;
    private String timestamp;
    private MessageStatus status;
    private MessageType type;

    public enum MessageStatus {
        SENDING, SENT, DELIVERED, READ
    }

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM
    }

    public ChatModel(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = "";
        this.status = MessageStatus.DELIVERED;
        this.type = MessageType.TEXT;
    }

    public ChatModel(String message, boolean isUser, String timestamp) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.status = MessageStatus.DELIVERED;
        this.type = MessageType.TEXT;
    }

    public ChatModel(String message, boolean isUser, String timestamp, MessageStatus status, MessageType type) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.status = status;
        this.type = type;
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public MessageType getType() {
        return type;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ChatModel{" +
                "message='" + message + '\'' +
                ", isUser=" + isUser +
                ", timestamp='" + timestamp + '\'' +
                ", status=" + status +
                ", type=" + type +
                '}';
    }
}
