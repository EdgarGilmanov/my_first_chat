package client;

import javafx.beans.property.SimpleStringProperty;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Model {
    private final Set<String> allUserNames = new HashSet<>();
    private SimpleStringProperty newMessage = new SimpleStringProperty("");
    private SimpleStringProperty users = new SimpleStringProperty("");

    public void setAllUserNames() {
        StringBuilder sb = new StringBuilder();
        for (String userName : Collections.unmodifiableSet(allUserNames)) {
            sb.append(userName).append("\n");
        }
        users.set(sb.toString());
    }

    public void setNewMessage(String message) {
        this.newMessage.set(newMessage.get()+message +"\n");
        System.out.println(newMessage.toString());
    }


    public SimpleStringProperty newMessageProperty() {
        return newMessage;
    }


    public SimpleStringProperty usersProperty() {
        return users;
    }

    public void addUser(String newUserName){
        allUserNames.add(newUserName);
    }
    public void deleteUser(String userName){
        allUserNames.remove(userName);
    }

}
