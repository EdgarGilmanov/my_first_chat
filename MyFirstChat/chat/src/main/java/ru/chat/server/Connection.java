package ru.chat.server;

import ru.chat.model.Message;

import java.io.*;

public class Connection {
    private final ObjectOutputStream out;
    private final ObjectInputStream in;

    public Connection(OutputStream out, InputStream in) throws IOException {
        this.out = new ObjectOutputStream(out);
        this.in = new ObjectInputStream(in);
    }

    public Message receive() throws IOException, ClassNotFoundException {
        return (Message) in.readObject();
    }

    public void send(Message message) throws IOException {
        out.writeObject(message);
    }
}
