package client;

import server.Connection;
import server.Message;
import server.MessageType;
import server.User;
import java.io.IOException;
import java.net.Socket;

public class Bot extends AbstractClient {
    public Bot(Model model) {
        super(model, new User("bot_77","bot_77","bot_77","bot_77","male"));
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
            sendTextMessage("Привет! Я бот я буду помогать тебе в чате.\n" +
                    "Для того, чтобы обратиться ко мне, просто начни свое сообщение с символов '//bot_77'.");
            System.out.println("Соединение с клиентом установлено");
        } else {
            System.out.println("Произошла ошибка во время соединения с клиентом.");
        }
        while (isClientConnected()) {

        }
    }

    private class SocketThread extends AbstractClient.AbstractSocketThread{
        @Override
        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true){
                Message message = getConnection().receive();
                if(message.getType() == MessageType.USER_REQUEST_DB){
                    getConnection().send(new Message(MessageType.BOT,getUser()));
                } else if(message.getType() == MessageType.USER_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                } else if(message.getType() == MessageType.USER_NOT_ACCEPTED){
                    setUserNotFound(true);
                    closeConnection();
                }
            }
        }

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true){
                Message message = getConnection().receive();
                if(message.getType() == MessageType.BOT_MESSAGE){
                    String answer = requestHandler(message.getData());
                    sendTextMessage(answer);
                }
            }
        }

        private String requestHandler(String message){
            if(message.contains("привет")){
                return "Здорова!";
            }
            return "Не понимаю команду!";
        }

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

    public static void main(String[] args) {
        Bot bot = new Bot(new Model());
        bot.start();
    }
}
