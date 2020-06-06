package ru.chat.model;

import java.io.Serializable;

public class Message implements Serializable {
    private final Type type;
    private final String data;
    private final User user;

    public Message(Type type, String data){
        this.type = type;
        this.data = data;
        this.user = null;
    }

    public Message(Type type, User user) {
        this.type = type;
        this.user = user;
        this.data = null;
    }

    public Message(Type type){
        this.type = type;
        this.data = null;
        this.user = null;
    }

    public User getUser() {
        return user;
    }

    public Type getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public enum Type {
        USER_REQUEST,
        USER_LOGIN,
        USER_REGISTRATION,
        REGISTRATION_ACCEPTED,
        REGISTRATION_NOT_ACCEPTED,
        USER_ADDED,
        USER_REMOVED,
        USER_ACCEPTED,
        USER_NOT_ACCEPTED,
        TEXT,
        CLOSE_CONNECTION,
        BOT,
        BOT_MESSAGE
    }

}
