package client.controllers;

import client.Client;
import client.Launcher;
import client.Model;
import client.view.animations.ShakeAnim;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import server.User;

import java.net.URL;
import java.util.ResourceBundle;

public class SignInController extends BaseController implements Initializable {
    private static Client client;
    private Model model;
    public static final String URL_FXML = "/client/view/signIn.fxml";

    @FXML
    private TextField userNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button authButton;

    @FXML
    private Text errorTextUserName;

    @FXML
    private Text errorTextPassword;

    @FXML
    private Button signUpButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        signUpButton.setOnAction(event -> {
            Launcher.getNavigation().load(SignUpController.URL_FXML).show();
        });
        authButton.setOnAction(event -> {
            String userName = userNameField.getText().trim();
            String password = passwordField.getText().trim();

            if (isCorrectInput(userName, password)) {
                User user = new User(userName, password);
                errorTextPassword.setOpacity(0);
                errorTextUserName.setOpacity(0);
                model = new Model();
                client = new Client(model);
                client.setDaemon(true);
                client.setUser(user);
                client.start();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (client.isClientConnected()) {
                    Launcher.getNavigation().load(ChatController.URL_FXML).show();
                } else if(!client.isServerConnect()){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка соединения с сервером");
                    alert.setHeaderText("Нет подлкючения к серверу. Повторите попытку позже");
                    alert.showAndWait();
                    client = null;
                    model = null;
                } else if(client.isUserNotFound()){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибка авторизации");
                    alert.setHeaderText("Пользователь с таким логином или паролем не найден");
                    alert.showAndWait();
                }
            }
        });
    }

    public static Client getClient() {
        return client;
    }


    private boolean isCorrectInput(String userName, String password){
        if(userName.isEmpty() && password.isEmpty()){
            errorTextUserName.setText("Некорректный логин");
            errorTextPassword.setText("Некорректный пароль");
            errorTextPassword.setOpacity(100);
            errorTextUserName.setOpacity(100);
            new ShakeAnim(userNameField).play();
            new ShakeAnim(passwordField).play();
        } else if(userName.isEmpty()){
            errorTextUserName.setText("Некорректный логин");
            errorTextUserName.setOpacity(100);
            new ShakeAnim(userNameField).play();
        } else if(password.isEmpty()){
            errorTextPassword.setText("Некорректный пароль");
            errorTextPassword.setOpacity(100);
            new ShakeAnim(passwordField).play();
        } else return true;

        return false;
    }
}