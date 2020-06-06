package ru.chat.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import ru.chat.Launcher;
import ru.chat.controller.animations.ShakeAnim;
import ru.chat.model.User;
import ru.chat.service.Client;

import java.net.URL;
import java.util.ResourceBundle;

public class SignInController extends BaseController implements Initializable {
    public static final String URL_FXML = "ru/chat/JFX-INF/views/signIn.fxml";

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
                skipErrorText();
                User user = new User();
                user.setUserName(userName);
                user.setPassword(password);
                Client client = Launcher.getClient();
                client.setUser(user);
                client.notifyAll();
                if (checkClient(client)) {
                    Launcher.getNavigation().load(ChatController.URL_FXML).show();
                }
            }
        });
    }

    private boolean checkClient(Client client) {
        boolean sr = client.isServerConnect();
        boolean us = client.isUserExist();
        if (sr) {
            alertErrorWithServerConnect();
        }
        if (us) {
            alertUserNotFount();
        }
        return sr && us;
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

    private void alertErrorWithServerConnect() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка соединения с сервером");
        alert.setHeaderText("Нет подлкючения к серверу. Повторите попытку позже");
        alert.showAndWait();
    }

    private void alertUserNotFount() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка авторизации");
        alert.setHeaderText("Пользователь с таким логином или паролем не найден");
        alert.showAndWait();
    }

    private void skipErrorText() {
        errorTextPassword.setOpacity(0);
        errorTextUserName.setOpacity(0);
    }
}