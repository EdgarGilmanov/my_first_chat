package client;

import controllers.ChatController;
import sample.User;
import server.Connection;
import server.ConsoleHelper;
import server.Message;
import server.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client extends Thread {
    private static User user;
    private Connection connection;
    private volatile boolean clientConnected = true;
    private Model model = new Model();
    private ChatController chatController = new ChatController();



    public static void setUser(User parUser) {
        System.out.println("setuser");
        user = parUser;
    }

    public Model getModel() {
        System.out.println("getModel");
        return model;
    }


    protected SocketThread getSocketThread() {
        System.out.println("getSocketThread");
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        //System.out.println("sendTextMessage");
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Ошибка отправки сообщений");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread thread = getSocketThread();
        thread.setDaemon(true);
        thread.start();
        synchronized (this) {
            System.out.println("run synch(this)");
            try {
                this.wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage(e.getMessage());
            }
        }
        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено.\nДля выхода наберите команду '//exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }
        while (clientConnected) {
            //System.out.println("ждет сообщения");
            String result = null;
            if (result.equals("//exit")) break;
            sendTextMessage(result);
        }
    }

    public class SocketThread extends Thread {
        protected void processIncomingMessage(String message) throws IOException, ClassNotFoundException {
            //System.out.println("processIncomingMessage");
            getModel().setNewMessage(message);
            chatController.getTextChatArea().setText(getModel().getNewMessage());
        }

        protected void informAboutAddingNewUser(String userName) {
            System.out.println("informAboutAddingNewUser");
            getModel().setNewMessage(userName + " подлючился к чату");
            chatController.getTextChatArea().setText(getModel().getNewMessage());
            getModel().addUser(userName);
            StringBuilder sb = new StringBuilder();
            for (String name : getModel().getAllUserNames()) {
                sb.append(name).append("\n");
            }
            chatController.getUsersListArea().setText(sb.toString());
        }

        protected void informAboutDeletingNewUser(String userName) {
            System.out.println("informAboutDeletingNewUser");
            getModel().setNewMessage(userName + " покинул чат");
            chatController.getTextChatArea().setText(getModel().getNewMessage());
            getModel().deleteUser(userName);
            StringBuilder sb = new StringBuilder();
            for (String name : getModel().getAllUserNames()) {
                sb.append(name).append("\n");
            }
            chatController.getUsersListArea().setText(sb.toString());
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            System.out.println("notifyConnectionStatusChanged");
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                System.out.println("synchronized (Client.this)");
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            System.out.println("clientHandshake()");
            while (true) {
                Message message = connection.receive();
                System.out.println("ждем сообщения");
                if (message.getType() == MessageType.NAME_REQUEST) {
                    System.out.println("приняли запрос на имя");
                    connection.send(new Message(MessageType.USER_NAME, user.getUserName()));
                    System.out.println("отправили имя");
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    break;
                } else throw new IOException("Unexpected MessageType");
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            System.out.println("clientMainLoop()");
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
            Socket socket = null;
            try {
                socket = new Socket("localhost", 8000);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}