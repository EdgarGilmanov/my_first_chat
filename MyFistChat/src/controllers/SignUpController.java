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

public class SignUpController extends BaseController implements Initializable {

    public static final String URL_FXML = "/gui/view/signUp.fxml";

    @FXML
    private TextField userNameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button signUpDoneButton;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private RadioButton maleButton;

    @FXML
    private RadioButton femaleButton;

    @FXML
    private Text errorTextFirstName;

    @FXML
    private Text errorTextLastName;

    @FXML
    private Text errorTextPassword;

    @FXML
    private Text errorTextUserName;

    @FXML
    private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ToggleGroup group = new ToggleGroup();
        maleButton.setToggleGroup(group);
        maleButton.setSelected(true);
        femaleButton.setToggleGroup(group);
        backButton.setOnAction(event -> {
            Main.getNavigation().goBack();
        });
        signUpDoneButton.setOnAction(event -> {
            signUp();
        });
    }

    private void signUp() {
        DataBaseHandler dbHandler = new DataBaseHandler();
        User user = null;
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String userName = userNameField.getText().trim();
        String password = passwordField.getText().trim();
        String gender = "male";
        if (femaleButton.isSelected()) gender = "female";

        if(isCorrectInput(firstName,lastName,userName,password)) {
            user = new User(firstName, lastName, userName, password, gender);
            if(tryRegistration(dbHandler,user)) {
                dbHandler.signUpUser(user);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Завершение регистрации");
                alert.setHeaderText("Регистрация заверешена. В окне авторизации войдите в свою учетную запись");
                alert.showAndWait();
                Main.getNavigation().goBack();
            }
        }
    }

    private boolean tryRegistration(DataBaseHandler dbHandler,User user) {
        ResultSet resultSet = dbHandler.getUserForSignUp(user);
        int count = 0;
        try {
            while (resultSet.next()) count++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (count > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка при регистрации");
            alert.setHeaderText("Имя пользователя уже зарегистрировано");
            alert.showAndWait();
        } else return true;

        return false;
    }

    private boolean isCorrectInput(String firstName, String lastName, String userName, String password){
        boolean fn = true;
        boolean ln = true;
        boolean un = true;
        boolean pw = true;
        if(firstName.isEmpty()){
            errorTextFirstName.setText("Имя не может быть пустым");
            errorTextFirstName.setOpacity(100);
            new ShakeAnim(firstNameField).play();
            fn = false;
        }
        if(lastName.isEmpty()){
            errorTextLastName.setText("Фамилия не может быть пустой");
            errorTextLastName.setOpacity(100);
            new ShakeAnim(lastNameField).play();
            ln = false;
        }
        if(userName.isEmpty()){
            errorTextUserName.setText("Логин не может быть пустым");
            errorTextUserName.setOpacity(100);
            new ShakeAnim(userNameField).play();
            un = false;
        }

        if(password.length()<5) {
            errorTextPassword.setText("Слишком короткий пароль. Попробуйте другой");
            errorTextPassword.setOpacity(100);
            new ShakeAnim(passwordField).play();
            pw = false;
        }

        return (fn && ln && un && pw);
    }
}