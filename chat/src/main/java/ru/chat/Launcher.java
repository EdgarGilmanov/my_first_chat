package ru.chat;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.dbcp2.BasicDataSource;
import ru.chat.controller.Navigation;
import ru.chat.controller.SignInController;
import ru.chat.server.Server;
import ru.chat.service.Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Launcher extends Application {
    private static Navigation navigation;
    private String path;
    private final Properties cfg = new Properties();
    private final BasicDataSource pool = new BasicDataSource();
    private static Client client;

    public Launcher() { }

    public Launcher(String path) {
        this.path = path;
    }

    private void cfg() {
        try (BufferedReader in = new BufferedReader(new FileReader(new File(path)))) {
            cfg.load(in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void pool() {
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
    }

    private void server() {
        if (cfg.getProperty("server").equals("true")) {
            Thread server = new Thread(new Server(
                    pool,
                    Integer.parseInt(cfg.getProperty("fork.pool.size")),
                    Integer.parseInt(cfg.getProperty("server.port"))));
            server.start();
        }
    }

    private void client() {
        client = new Client(cfg.getProperty("server.host"),
                Integer.parseInt(cfg.getProperty("server.port")));
        Thread t = new Thread(client);
        t.start();
    }

    @Override
    public void start(Stage primaryStage) {
        navigation = new Navigation(primaryStage);
        primaryStage.setTitle("chat");
        primaryStage.setResizable(false);
        primaryStage.show();
        navigation.load(SignInController.URL_FXML).show();
    }

    public static Navigation getNavigation() {
        return navigation;
    }

    public static Client getClient() {
        return client;
    }

    public static void main(String[] args) {
        Launcher l = new Launcher("app.properties");
        l.cfg();
        l.pool();
        l.server();
        l.client();
        launch(args);
    }
}

