package server;

import java.io.Serializable;

public class User implements Serializable {
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String gender;

    public User(String firstName, String lastName, String userName, String password, String gender) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.gender = gender;
    }

    public User(String userName, String password){
        this.userName = userName;
        this.password = password;
    }


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getGender() {
        return gender;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + userName.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof User)) return false;
        User user = (User) obj;
        return user.getUserName().equals(userName);
    }

}
