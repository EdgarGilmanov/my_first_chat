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

    public Client(Model model) {
        this.model = model;
    }

    protected Connection connection;
    private volatile boolean clientConnected = true;
    private static User user;

    public static void setUser(User user1){
        user = user1;
    }
    public boolean shouldSendTextFromConsole() {
        return true;
    }

    public SocketThread getSocketThread() {
        return new SocketThread();
    }

    public void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread thread = getSocketThread();
        thread.setDaemon(true);
        thread.start();
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage(e.getMessage());
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено.\nДля выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while (clientConnected) {
            String result = ConsoleHelper.readString();
            if (result.equals("exit")) break;
            if (shouldSendTextFromConsole()) {
                sendTextMessage(result);
            }
        }


    }

    public class SocketThread extends Thread {
        public void processIncomingMessage(String message) throws IOException, ClassNotFoundException {
            model.setNewMessage(message);
        }

        public void informAboutAddingNewUser(String userName) {
            model.setNewMessage(userName + " подлючился к чату");
            model.addUser(userName);
            model.setAllUserNames();
        }

        public void informAboutDeletingNewUser(String userName) {
            model.setNewMessage(userName + "покинул чат");
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
            System.out.println("здороваемся с пользователем");
            while (true) {
                Message message = connection.receive();
                System.out.println("Ждем сообщения");
                if (message.getType() == MessageType.NAME_REQUEST) {
                    System.out.println("приняли запрос и отправили имя");
                    connection.send(new Message(MessageType.USER_NAME, user.getUserName()));
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    System.out.println("приняли сообщение о том что пользователь красавчик");
                    notifyConnectionStatusChanged(true);
                    break;
                } else throw new IOException("Unexpected MessageType");
            }
        }

        public void clientMainLoop() throws IOException, ClassNotFoundException {
            System.out.println("общаемся с клиентом");
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }

        public void run() {
            try {
                Socket socket = new Socket("localhost", 8000);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}