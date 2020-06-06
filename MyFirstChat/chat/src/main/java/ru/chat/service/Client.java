package ru.chat.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chat.model.Chat;
import ru.chat.model.Message;
import ru.chat.model.User;
import ru.chat.server.Connection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client implements Runnable {
    private final Logger log = LoggerFactory.getLogger(Client.class);
    private final String host;
    private final int port;
    private final Chat model;
    private Connection connection;
    private User user;
    private boolean serverConnect;
    private boolean userExist;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.model = new Chat();
    }

    @Override
    public void run() {
        try {
            try (Socket socket = new Socket(host, port);
                 OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {
                this.connection = new Connection(out, in);
                this.serverConnect = true;
                wait();
                handShake();
                mainLoop();
            }
        } catch (Exception e) {
            log.error("Client connection", e);
        }
    }

    private void handShake() throws IOException, ClassNotFoundException, InterruptedException {
        while (true) {
            Message response = connection.receive();
            Message.Type type = response.getType();
            if (type == Message.Type.USER_REQUEST) {
                connection.send(new Message(Message.Type.USER_LOGIN, user));
            }
            if (type == Message.Type.USER_ACCEPTED) {
                this.userExist = true;
                break;
            }
            if (type == Message.Type.USER_NOT_ACCEPTED) {
                wait();
            }
        }
    }

    private void mainLoop() throws IOException, ClassNotFoundException {
        while (true) {
            Message response = connection.receive();
            Message.Type type = response.getType();
            if (type == Message.Type.TEXT) {
                model.setNewMessage(response.getData());
            }
            if (type == Message.Type.USER_ADDED) {
                model.setNewMessage(user.getUserName() + " подлючился(-ась) к чату");
                model.addUser(user.getUserName());
            }
            if (type == Message.Type.USER_REMOVED) {
                model.setNewMessage(user.getUserName() + " покинул(-а) чат");
                model.deleteUser(user.getUserName());
            }
        }
    }

    public void sendTextMessage(String text) {
        try {
            connection.send(new Message(Message.Type.TEXT, text));
        } catch (IOException e) {
            log.error("Client send message", e);
        }
    }

    public boolean isServerConnect() {
        return serverConnect;
    }

    public boolean isUserExist() {
        return userExist;
    }

    public void setUser(User user) {
        this.user = user;
    }
}