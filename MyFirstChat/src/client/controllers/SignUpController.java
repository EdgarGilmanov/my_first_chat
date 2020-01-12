package client.controllers;

import client.ClientSignUp;
import client.Launcher;
import client.view.animations.ShakeAnim;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import server.User;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SignUpController extends BaseController implements Initializable {

    public static final String URL_FXML = "/client/view/signUp.fxml";

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
        User user = null;
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String userName = userNameField.getText().trim();
        String password = passwordField.getText().trim();
        String gender = "male";
        if (femaleButton.isSelected()) gender = "female";

        if (isCorrectInput(firstName, lastName, userName, password)) {
            user = new User(firstName, lastName, userName, password, gender);
            ClientSignUp clientSignUp = new ClientSignUp(user);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Integer> future = executorService.submit(clientSignUp);
            executorService.shutdown();
            if(future.get() == 0) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка регистрации");
                alert.setHeaderText("Пользователь с таким логином уже найден");
                alert.showAndWait();
            }else if(future.get() == 1 ) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Завершение регистрации");
                alert.setHeaderText("Регистрация заверешена. В окне авторизации войдите в свою учетную запись");
                alert.showAndWait();
                Launcher.getNavigation().goBack();
            } else if(future.get() == 2) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Ошибка соединения с сервером");
                alert.setHeaderText("Отсутствует содинение с сервером. Повторите попытку позже");
                alert.showAndWait();
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
}