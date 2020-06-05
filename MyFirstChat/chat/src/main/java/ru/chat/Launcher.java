package ru.chat;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.commons.dbcp2.BasicDataSource;
import ru.chat.controller.Navigation;
import ru.chat.controller.SignInController;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class Launcher extends Application {
    private Navigation navigation;
    private final String path;
    private final Properties cfg = new Properties();
    private final BasicDataSource pool = new BasicDataSource();

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
            //server.start();
        }
    }



    @Override
    public void start(Stage primaryStage) {
        navigation = new Navigation(primaryStage);
        primaryStage.setTitle("chat");
        primaryStage.setResizable(false);
        primaryStage.show();
        navigation.load(SignInController.URL_FXML).show();
    }

    public static void main(String[] args) {
        Launcher l = new Launcher("app.properties");
        l.cfg();
        l.pool();
        l.server();
        launch(args);
    }
}

