package server;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType type;
    private final String data;
    private final User user;

    public Message(MessageType type, String data){
        this.type = type;
        this.data = data;
        this.user = null;
    }

    public Message(MessageType type, User user) {
        this.type = type;
        this.user = user;
        this.data = null;
    }

    public Message(MessageType type){
        this.type = type;
        this.data = null;
        this.user = null;
    }

    public server.User getUser() {
        return user;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
