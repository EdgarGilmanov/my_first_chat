package server;

import org.apache.commons.dbcp2.BasicDataSource;
import ru.chat.model.Message;
import ru.chat.model.User;
import ru.chat.server.Connection;
import ru.chat.store.Store;
import ru.chat.store.UserStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final Store store;

    public Server(BasicDataSource dataSource) {
        this.store = new UserStore(dataSource);
    }

    private static Map<User, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (User key : connectionMap.keySet()) {
            try {
                connectionMap.get(key).send(message);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private User serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            //TODO неообходимо оптимизировать код для читабельности
            User user = null;
            while (true){
                connection.send(new Message(Message.MessageType.USER_REQUEST_DB));
                Message messageClient = connection.receive();
                if(messageClient.getType() == MessageType.USER && messageClient.getUser()!=null){
                    if(searchUserInDB(Server.dbHandler,messageClient.getUser())) {
                        user = messageClient.getUser();
                        connectionMap.put(user,connection);
                        connection.send(new Message(MessageType.USER_ACCEPTED));
                        break;
                    }else {
                        connection.send(new Message(MessageType.USER_NOT_ACCEPTED));
                    }
                }else if(messageClient.getType() == MessageType.USER_REGISTRATION){
                    connection.send(new Message(MessageType.REGISTRATION_ALLOWED));
                } else if(messageClient.getType() == MessageType.REG_USER && messageClient.getUser()!= null){
                    if(!searchUserInDB(Server.dbHandler,messageClient.getUser())){
                        dbHandler.signUpUser(messageClient.getUser());
                        connection.send(new Message(MessageType.REGISTRATION_ACCEPTED));
                    }else{
                        connection.send(new Message(MessageType.REGISTRATION_NOT_ACCEPTED));
                    }
                } else if(messageClient.getType() == MessageType.BOT && messageClient.getUser()!=null){
                    if(!connectionMap.containsKey(messageClient.getUser())){
                        user = messageClient.getUser();
                        connectionMap.put(user,connection);
                        connection.send(new Message(MessageType.USER_ACCEPTED));
                        break;
                    }
                }
            }
            return user;
        }

        private boolean searchUserInDB(DataBaseHandler dbHandler, User user){
            ResultSet resultSet = dbHandler.getUser(user);
            int count = 0;
            try {
                while (resultSet.next()) count++;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return !(count == 0);
        }

        private void notifyUsers(Connection connection, String userName) {
            connectionMap.forEach((k, v) -> {
                if (!k.getUserName().equals(userName)) {
                    try {
                        connection.send(new Message(MessageType.USER_ADDED, k.getUserName()));
                    } catch (IOException ignored) {
                    }
                }
            });
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message messageClient = connection.receive();
                if (messageClient.getType() == MessageType.TEXT) {
                    isItForBots(messageClient.getData());
                    Message messageServer = new Message(MessageType.TEXT, userName + ": " + messageClient.getData());
                    sendBroadcastMessage(messageServer);
                }else if(messageClient.getType() == MessageType.CLOSE_CONNECTION){
                    break;
                } else {
                    ConsoleHelper.writeMessage("Ошибка получения сообщения от пользователя");
                    break;
                }
            }
        }
        private void isItForBots(String message) throws IOException {
            if(message.startsWith("//bot")){
                for(User key : connectionMap.keySet()){
                    if(message.contains(key.getUserName())){
                        connectionMap.get(key).send(new Message(MessageType.BOT_MESSAGE,message));
                    }
                }
            }
        }
        public void run() {
            ConsoleHelper.writeMessage("Установлено соединение " + socket.getRemoteSocketAddress().toString());
            Connection connection = null;
            User user = null;
            try {
                connection = new Connection(socket);
                user = serverHandshake(connection);
                notifyUsers(connection, user.getUserName());
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, user.getUserName()));
                serverMainLoop(connection, user.getUserName());
                connectionMap.remove(user);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, user.getUserName()));
                socket.close();
                ConsoleHelper.writeMessage("Соединение с клиентом закрыто");
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка соединения с клиентом");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Сервер запущен!");
        ServerSocket serverSocket = new ServerSocket(8000);
        try {
            while (true){
                new Handler(serverSocket.accept()).start();
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
            serverSocket.close();
        }
    }
}