package ru.chat.server;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chat.model.Message;
import ru.chat.model.User;
import ru.chat.service.Connection;
import ru.chat.store.Store;
import ru.chat.store.UserStore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class Server implements Runnable {
    private final long start = System.currentTimeMillis();
    private final Logger log = LoggerFactory.getLogger(Server.class);
    private final Store store;
    private final int poolSize;
    private final int port;
    private static final Map<User, Connection> connectionMap = new ConcurrentHashMap<>();

    public Server(BasicDataSource dataSource, int poolSize, int port) {
        this.store = new UserStore(dataSource);
        this.poolSize = poolSize;
        this.port = port;
    }

    @Override
    public void run() {
        log.info("Server start : pool - {}, port - {}, time - {} ms.",
                poolSize, port, (System.currentTimeMillis() - start)
        );
        ForkJoinPool fork = new ForkJoinPool(poolSize);
        try (ServerSocket server = new ServerSocket(port)) {
            while (!server.isClosed()) {
                final Socket so = server.accept();
                fork.execute(() -> {
                    try (OutputStream out = so.getOutputStream();
                         InputStream in = so.getInputStream()) {
                        Connection cnn = new Connection(out, in);
                        Optional<User> curUs = handshake(cnn);
                        if (curUs.isPresent()) {
                            mainLoop(cnn, curUs.get());
                            connectionMap.remove(curUs.get());
                            sendBroadcastMessage(new Message(Message.Type.USER_REMOVED, curUs.get().getUserName()));
                        }
                    } catch (Exception e) {
                        log.error("Socket accept", e);
                    }
                });
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<User> handshake(Connection cnn) throws IOException, ClassNotFoundException {
        cnn.send(new Message(Message.Type.USER_REQUEST));
        Message response = cnn.receive();
        Message.Type type = response.getType();
        if (type == Message.Type.USER_LOGIN) {
            return signIn(cnn, response.getUser());
        }
        if (type == Message.Type.USER_REGISTRATION) {
            registration(cnn, response.getUser());
        }
        return Optional.empty();
    }

    private Optional<User> signIn (Connection cnn, User user) throws IOException {
        if (user == null) {
            return Optional.empty();
        }
        Optional<User> us = store.findBy(user.getUserName());
        if (!us.isPresent()) {
            cnn.send(new Message(Message.Type.USER_NOT_ACCEPTED));
        }
        connectionMap.put(us.get(), cnn);
        cnn.send(new Message(Message.Type.USER_ACCEPTED));
        sendBroadcastMessage(new Message(Message.Type.USER_ADDED, user.getUserName()));
        notifyUsers(cnn, us.get().getUserName());
        log.info("user logged in {}", us.get().getUserName());
        return us;
    }

    private void notifyUsers(Connection connection, String userName) {
        connectionMap.forEach((k, v) -> {
            if (!k.getUserName().equals(userName)) {
                try {
                    connection.send(new Message(Message.Type.USER_ADDED, k.getUserName()));
                } catch (IOException ignored) {
                }
            }
        });
    }

    private void registration(Connection cnn, User user) throws IOException {
        Optional<User> us = Optional.of((User)store.save(user));
        if (!us.isPresent() || us.get().getId() < 1) {
            cnn.send(new Message(Message.Type.REGISTRATION_NOT_ACCEPTED));
        }
        cnn.send(new Message(Message.Type.REGISTRATION_ACCEPTED));
    }

    private void mainLoop(Connection connection, User user) throws IOException, ClassNotFoundException {
        while (true) {
            Message messageClient = connection.receive();
            if (messageClient.getType() == Message.Type.TEXT) {
                Message messageServer = new Message(Message.Type.TEXT, user.getUserName() + ": " + messageClient.getData());
                sendBroadcastMessage(messageServer);
            } else if (messageClient.getType() == Message.Type.CLOSE_CONNECTION) {
                break;
            }
        }
    }

    private void sendBroadcastMessage(Message message) {
        for (User key : connectionMap.keySet()) {
            try {
                connectionMap.get(key).send(message);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}