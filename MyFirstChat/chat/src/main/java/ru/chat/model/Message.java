package ru.chat.model;

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

    public User getUser() {
        return user;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public enum MessageType {
        USER_REQUEST_DB,
        USER,
        USER_ACCEPTED,
        USER_NOT_ACCEPTED,
        TEXT,
        USER_ADDED,
        USER_REMOVED,
        CLOSE_CONNECTION,
        USER_REGISTRATION,
        REGISTRATION_ALLOWED,
        REGISTRATION_ACCEPTED,
        REGISTRATION_NOT_ACCEPTED,
        REG_USER,
        BOT,
        BOT_MESSAGE
    }

}
