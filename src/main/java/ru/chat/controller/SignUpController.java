package ru.chat.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import ru.chat.Launcher;
import ru.chat.controller.animations.ShakeAnim;
import ru.chat.model.User;
import ru.chat.service.Client;
import ru.chat.service.Registration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SignUpController extends BaseController implements Initializable {

    public static final String URL_FXML = "ru/chat/JFX-INF/views/signUp.fxml";

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
            Launcher.getNavigation().goBack();
        });
        signUpDoneButton.setOnAction(event -> {
            try {
                signUp();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void signUp() throws ExecutionException, InterruptedException {
        User user = new User();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String userName = userNameField.getText().trim();
        String password = passwordField.getText().trim();
        boolean gender = true;
        if (femaleButton.isSelected()) {
            gender = false;
        }
        if (isCorrectInput(firstName, lastName, userName, password)) {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUserName(userName);
            user.setPassword(password);
            user.setGender(gender);
            Client client = Launcher.getClient();
            client.setUser(user);
            Registration reg = new Registration(client);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Registration.Status> future = executorService.submit(reg);
            executorService.shutdown();
            if (future.get() == Registration.Status.FILED) {
                alertErrorReg();
            }
            if (future.get() == Registration.Status.OK) {
                alertOkReg();
                Launcher.getNavigation().goBack();
            }
        }
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

    private void alertErrorReg() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ошибка регистрации");
        alert.setHeaderText("Пользователь с таким логином уже найден");
        alert.showAndWait();
    }

    private void alertOkReg() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Завершение регистрации");
        alert.setHeaderText("Регистрация заверешена. В окне авторизации войдите в свою учетную запись");
        alert.showAndWait();
    }
}