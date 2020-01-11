package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();


    public static void sendBroadcastMessage(Message message) {
        for (String key : connectionMap.keySet()) {
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

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name = null;
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message messageClient = connection.receive();
                if (messageClient.getType() == MessageType.USER_NAME
                        && !messageClient.getData().isEmpty()
                        && !connectionMap.containsKey(messageClient.getData())) {
                    name = messageClient.getData();
                    connectionMap.put(name, connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                    break;
                }
            }
            return name;
        }

        private void notifyUsers(Connection connection, String userName) {
            connectionMap.forEach((k, v) -> {
                if (!k.equals(userName)) {
                    try {
                        connection.send(new Message(MessageType.USER_ADDED, k));
                    } catch (IOException ignored) {
                    }
                }
            });
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message messageClient = connection.receive();
                if (messageClient.getType() == MessageType.TEXT) {
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
        public void run() {
            ConsoleHelper.writeMessage("Установлено соединение " + socket.getRemoteSocketAddress().toString());
            Connection connection = null;
            String userName = null;
            try {
                connection = new Connection(socket);
                userName = serverHandshake(connection);
                notifyUsers(connection, userName);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                serverMainLoop(connection, userName);
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED,userName));
                socket.close();
                ConsoleHelper.writeMessage("Соединение с сервером закрыто");
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка соединения с сервером");
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