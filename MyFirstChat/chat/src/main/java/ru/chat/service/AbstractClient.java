package ru.chat.service;

import server.Connection;
import server.Message;
import server.MessageType;
import server.User;

import java.io.IOException;

/**
 * Этот класс создавался абстрактным для того, чтобы обозначить основное поведение клиента.
 * Данная абстракция позволит создать на ее основе как обычного клиента-пользователя, так и бота-помощника для
 * резонирования действий клиентов-пользователей, подключенных к чату.
 * Методы, получвшие модификатор abstract,должны быть переопредленые с целью уточнения поведения объекта исходя из его предназначения. */

public abstract class AbstractClient extends Thread {
    private Model model;
    private Connection connection;
    private volatile boolean clientConnected = true;
    /**
     * здесь важно понимать, что класс-контейнер User подаразумевает не только пользователя, а в целом единицу хранения
     * информации о субъекте, подлключющийся к чату. К примеру, при создании бота-помощника в этот контейнер мы можем
     * поместить информацию о боте. */
    private User user;
    private volatile boolean serverConnect = false;
    private volatile boolean userNotFound = false;

    public AbstractClient(Model model,User user) {
        this.model = model;
        this.user = user;
    }

    /**
     * Метод sendTextMessage является публичным для между GUI-контроллером и клиентской части. В этот метод мы непосредственно
     * передаем текст из поля textField GUI-части.  */
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
    /**
     * Метод run() в основном предназначен для запуска и поддержание работы объекта класса SocketThread.
     * Под поддержением работы имеется ввиду, что работа объекта SocketThread будет напрямую зависить от переменной clientConnected
     * Так же в этом методе можно реализовать новые действия объекта, при расширении дополнительными методами.*/
    public abstract void run();

    public void setUserNotFound(boolean userNotFound) {
        this.userNotFound = userNotFound;
    }
    public User getUser() {
        return user;
    }
    public Connection getConnection() {
        return connection;
    }
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
    public void setServerConnect(boolean serverConnect) {
        this.serverConnect = serverConnect;
    }
    public void setConnection(Connection connection) {
        this.connection = connection;
    }


    abstract class AbstractSocketThread extends Thread {
        private void processIncomingMessage(String message) {
            model.setNewMessage(message);
        }

        private void informAboutAddingNewUser(String userName) {
            model.setNewMessage(userName + " подлючился(-ась) к чату");
            model.addUser(userName);
        }

        private void informAboutDeletingNewUser(String userName) {
            model.setNewMessage(userName + " покинул(-а) чат");
            model.deleteUser(userName);
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            AbstractClient.this.clientConnected = clientConnected;
            synchronized (AbstractClient.this) {
                AbstractClient.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
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

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
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
        /**
         * Метод run() создает подключение к socket и вызываем основные методы текущего класса:
         * - clientHandshake,
         * - clientMainLoop;
         * В случае возникновения исключения, мы должны его обработать путем оповещение, что клиент потерял
         * подключение с сервером. */
        public abstract void run();
    }
}
