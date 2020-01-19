package client;

import client.controllers.Navigation;
import client.controllers.SignInController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Launcher extends Application {
    private static Navigation navigation;

    public static Navigation getNavigation() {
        return navigation;
    }

    @Override
    public void start(Stage primaryStage){
        navigation = new Navigation(primaryStage);
        primaryStage.setTitle("chat");
        primaryStage.setResizable(false);
        primaryStage.show();
        Launcher.getNavigation().load(SignInController.URL_FXML).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}