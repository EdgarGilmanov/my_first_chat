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
        String messageResult = message;
        if(messageResult.length()>50){
            messageResult = messageTransfer(messageResult);
        }
        this.newMessage.set(newMessage.get()+messageResult +"\n");
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


    //здесь пытаемся разделить длинное сообщение
    private String messageTransfer(String message){
        int count = message.length()/50;
        StringBuilder stringBuilder = new StringBuilder(message);
        int count2 = 50;
        while (count>0){
            if(stringBuilder.charAt(count2) == ' '){
                stringBuilder.insert(count2+1,"\n");
                count2 = count2 + 50;
                count--;
            } else {
                count2--;
            }
        }
        return stringBuilder.toString();
    }
}
