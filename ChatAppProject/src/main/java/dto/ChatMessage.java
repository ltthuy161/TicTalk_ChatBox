package dto;

import java.sql.Timestamp;

public class ChatMessage {
    private String sender;
    private String message;
    private String receiver;
    private Timestamp timestamp;

    public ChatMessage() {
        super();
    }

    // Constructor, getters, and setters
    public ChatMessage(String sender, String receiver, String message, Timestamp timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

}