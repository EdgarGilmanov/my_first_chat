package ru.chat.service;

import ru.chat.model.Message;

import java.util.concurrent.Callable;

public class Registration implements Callable<Registration.Status> {
    private final Client client;

    public Registration(Client client) {
        this.client = client;
    }

    @Override
    public Status call() throws Exception {
        Connection cnn = client.getConnection();
        while (true) {
            cnn.send(new Message(Message.Type.USER_REGISTRATION, client.getUser()));
            Message response = cnn.receive();
            Message.Type type = response.getType();
            if (type == Message.Type.REGISTRATION_ACCEPTED) {
                return Status.OK;
            }
            if (type == Message.Type.REGISTRATION_NOT_ACCEPTED) {
                return Status.FILED;
            }
        }
    }

    public enum Status {
        OK,
        FILED
    }
}


