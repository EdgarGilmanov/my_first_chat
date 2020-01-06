package client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Model {
    private final Set<String> allUserNames = new HashSet<>();
    private StringBuffer newMessage = new StringBuffer();

    public Set<String> getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);
    }

    public String getNewMessage() {
        return newMessage.toString();
    }

    public void setNewMessage(String newMessage) {
        this.newMessage.append(newMessage).append("\n");
    }
    public void addUser(String newUserName){
        allUserNames.add(newUserName);
    }
    public void deleteUser(String userName){
        allUserNames.remove(userName);
    }

}
