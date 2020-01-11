package client;

import server.Connection;
import server.Message;
import server.MessageType;
import server.User;

import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
    private Model model;
    private Connection connection;
    private volatile boolean clientConnected = true;
    private User user;
    private boolean serverConnect = false;
    private boolean userNotFound = false;

    public boolean isServerConnect() {
        return serverConnect;
    }

    public boolean isUserNotFound() {
        return userNotFound;
    }

    public boolean isClientConnected() {
        return clientConnected;
    }

    public Model getModel() {
        return model;
    }

    public Client(Model model) {
        this.model = model;
    }

    public void setUser(User us){
        user = us;
    }

    private SocketThread getSocketThread() {
        return new SocketThread();
    }

    public void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            System.out.println("Ошибка соединения с сервером");
            clientConnected = false;
        }
    }

    public void closeConnection() throws IOException {
         clientConnected = false;
         connection.send(new Message(MessageType.CLOSE_CONNECTION));
         connection.close();
    }

    public void run() {
        SocketThread thread = getSocketThread();
        thread.setDaemon(true);
        thread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (clientConnected) {
            System.out.println("Соединение с клиентом установлено");
        } else {
            System.out.println("Произошла ошибка во время соединения с клиентом.");
        }
        while (clientConnected) {}
    }

    private class SocketThread extends Thread {
        private void processIncomingMessage(String message) {
            model.setNewMessage(message);
        }

        private void informAboutAddingNewUser(String userName) {
            model.setNewMessage(userName + " подлючился(-ась) к чату");
            model.addUser(userName);
            model.setAllUserNames();
        }

        private void informAboutDeletingNewUser(String userName) {
            model.setNewMessage(userName + " покинул(-а) чат");
            model.deleteUser(userName);
            model.setAllUserNames();
        }

        private void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        private void clientHandshake() throws IOException, ClassNotFoundException {
            while (true){
                Message message = connection.receive();
                if(message.getType() == MessageType.USER_REQUEST_DB){
                    connection.send(new Message(MessageType.USER,user));
                } else if(message.getType() == MessageType.USER_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                } else if(message.getType() == MessageType.USER_NOT_ACCEPTED){
                    userNotFound = true;
                    closeConnection();
                }
            }
        }

        private void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else throw new IOException("Unsupported message");
            }
        }

        public void run() {
            try {
                Socket socket = new Socket("localhost", 8000);
                connection = new Connection(socket);
                serverConnect = true;
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}