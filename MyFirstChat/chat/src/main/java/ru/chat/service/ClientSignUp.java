package ru.chat.service;

import server.Connection;
import server.Message;
import server.MessageType;
import server.User;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;

public class ClientSignUp implements Callable<Integer> {
    private Connection connection;
    private User user;

    public ClientSignUp(User user) {
        this.user = user;
    }

    private int clientHandshake() throws IOException, ClassNotFoundException {
        int result = 0;
        while (true){
            connection.send(new Message(MessageType.USER_REGISTRATION));
            Message message = connection.receive();
            if(message.getType() == MessageType.REGISTRATION_ALLOWED){
                if(user != null) connection.send(new Message(MessageType.REG_USER,user));
            } else if(message.getType() == MessageType.REGISTRATION_ACCEPTED){
                result = 1;
                break;
            } else if(message.getType() == MessageType.REGISTRATION_NOT_ACCEPTED){
                break;
            }
        }
        return result;
    }

    @Override
    public Integer call() throws Exception {
        int result = 0;
        try {
            Socket socket = new Socket("localhost", 8000);
            connection = new Connection(socket);
            result = clientHandshake();
        } catch (IOException | ClassNotFoundException e) {
            result = 2;
        }
        return result;
    }
}
