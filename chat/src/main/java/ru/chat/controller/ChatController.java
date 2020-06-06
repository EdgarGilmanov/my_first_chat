package ru.chat.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import ru.chat.Launcher;
import ru.chat.model.Chat;
import ru.chat.service.Client;

import java.net.URL;
import java.util.ResourceBundle;


public class ChatController extends BaseController implements Initializable {
    public static final String URL_FXML = "ru/chat/JFX-INF/views/chatWindow.fxml";

    @FXML
    private TextField messageTextField;

    @FXML
    private TextArea textChatArea;

    @FXML
    private TextArea usersListArea;

    @FXML
    private Button signOutButton;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Client client = Launcher.getClient();
        Chat model = client.getModel();
        textChatArea.textProperty().bind(model.newMessageProperty());
        usersListArea.textProperty().bind(model.usersProperty());
        signOutButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Вы уверены, что хотите покинуть чат?");
            alert.setTitle("Предупреждение");
            alert.showAndWait();
            if (alert.getResult().getText().equals("OK")) {
                Launcher.getNavigation().goBack();
            }
        });
        messageTextField.setOnAction(event -> {
            client.sendTextMessage(messageTextField.getText());
            messageTextField.setText("");
        });
    }
}