package sample;

import client.Client;
import client.Model;
import controllers.SignInController;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
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
        Main.getNavigation().load(SignInController.URL_FXML).show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}