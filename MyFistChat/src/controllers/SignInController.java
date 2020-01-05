package controllers;

import animations.ShakeAnim;
import dataBase.DataBaseHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import sample.Main;
import sample.User;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SignInController extends BaseController implements Initializable {

    public static final String URL_FXML = "/view/signIn.fxml";

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

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
            Main.getNavigation().load(SignUpController.URL_FXML).show();
        });
        authButton.setOnAction(event -> {
            DataBaseHandler dbHandler = new DataBaseHandler();
            String userName = userNameField.getText().trim();
            String password = passwordField.getText().trim();

            if(isCorrectInput(userName,password)){
                User user = new User(userName,password);
                if(trySignIn(dbHandler,user)){
                    errorTextPassword.setOpacity(0);
                    errorTextUserName.setOpacity(0);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.showAndWait();
                };
            }
        });
    }

    private boolean trySignIn(DataBaseHandler dbHandler, User user){
        ResultSet resultSet = dbHandler.getUser(user);
        int count = 0;
        try {
            while (resultSet.next()) count++;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (count == 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка при входе");
            alert.setHeaderText("Пользователь с таким логином или паролем не найден");
            alert.showAndWait();
        } else return true;

        return false;
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