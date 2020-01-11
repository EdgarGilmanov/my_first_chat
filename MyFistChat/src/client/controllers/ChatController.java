package client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import client.Client;
import client.Model;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import sample.Main;


public class ChatController extends BaseController implements Initializable {
    public static final String URL_FXML = "/client/view/chatWindow.fxml";
    private Client client;
    private Model model;

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
        client = SignInController.getClient();
        model = client.getModel();
        textChatArea.textProperty().bind(model.newMessageProperty());
        usersListArea.textProperty().bind(model.usersProperty());
        signOutButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("Вы уверены, что хотите покинуть чат?");
            alert.setTitle("Предупреждение");
            alert.showAndWait();
            if (alert.getResult().getText().equals("OK")) {
                Main.getNavigation().goBack();
                try {
                    client.closeConnection();
                    client.interrupt();
                    client = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        messageTextField.setOnAction(event -> {
            client.sendTextMessage(messageTextField.getText());
            messageTextField.setText("");
        });
    }
}