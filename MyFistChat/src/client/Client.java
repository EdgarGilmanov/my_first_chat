package client;


import sample.User;
import server.Connection;
import server.ConsoleHelper;
import server.Message;
import server.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
    private Model model;
    private Connection connection;
    private volatile boolean clientConnected = true;
    private User user;

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

    public SocketThread getSocketThread() {
        return new SocketThread();
    }

    public void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка соединения с сервером");
            clientConnected = false;
        }
    }

    public void closeConnection() throws IOException {
         clientConnected = false;
         connection.send(new Message(MessageType.CLOSE_CONNECTION));
         connection.close();
    }

    public void close(){
        clientConnected = false;
    }

    public void run() {
        System.out.println("Поток client начал свою работу ");
        SocketThread thread = getSocketThread();
        thread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage(e.getMessage());
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение с клиентом установлено");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время соединения с клиентом.");
        }
        while (clientConnected) {}
        System.out.println("Поток client завершил свою работу");
    }

    public class SocketThread extends Thread {
        public void processIncomingMessage(String message) throws IOException, ClassNotFoundException {
            model.setNewMessage(message);
        }

        public void informAboutAddingNewUser(String userName) {
            model.setNewMessage(userName + " подлючился(-ась) к чату");
            model.addUser(userName);
            model.setAllUserNames();
        }

        public void informAboutDeletingNewUser(String userName) {
            model.setNewMessage(userName + " покинул(-а) чат");
            model.deleteUser(userName);
            model.setAllUserNames();
        }

        public void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        public void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, user.getUserName()));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                } else throw new IOException("Unsupported message");
            }
        }

        public void clientMainLoop() throws IOException, ClassNotFoundException {
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
                Socket socket = new Socket("192.168.43.82", 8000);
                connection = new Connection(socket);
                clientHandshake();
                System.out.println("перешел на маинлуп");
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}