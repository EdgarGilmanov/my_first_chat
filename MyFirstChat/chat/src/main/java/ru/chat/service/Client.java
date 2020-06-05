package ru.chat.service;

import server.Connection;
import server.User;

import java.io.IOException;
import java.net.Socket;

public class Client extends AbstractClient {
    public Client(Model model, User user) {
        super(model,user);
    }

    @Override
    public void run() {
        SocketThread thread = new SocketThread();
        thread.setDaemon(true);
        thread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (isClientConnected()) {
            System.out.println("Соединение с клиентом установлено");
        } else {
            System.out.println("Произошла ошибка во время соединения с клиентом.");
        }
        while (isClientConnected()) {}
    }

    private class SocketThread extends AbstractSocketThread {
        @Override
        public void run() {
            try {
                Socket socket = new Socket("localhost", 8000);
                setConnection(new Connection(socket));
                setServerConnect(true);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
