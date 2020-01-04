package sample;

import javafx.application.Application;
import javafx.stage.Stage;
import controllers.SignInController;

public class Main extends Application {

    private static Navigation navigation;

    public static Navigation getNavigation() {
        return navigation;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        navigation = new Navigation(primaryStage);

        primaryStage.setTitle("chat");
        primaryStage.show();
        Main.getNavigation().load(SignInController.URL_FXML).show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}