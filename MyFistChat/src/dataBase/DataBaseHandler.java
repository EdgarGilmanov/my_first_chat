package dataBase;

import sample.User;

import java.sql.*;

public class DataBaseHandler {
    Connection dbConnection;

    private Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connectionString = "jdbc:mysql://" + Const.DB_HOST + ":" + Const.DB_PORT
                + "/" + Const.DB_NAME;
        Class.forName("com.mysql.cj.jdbc.Driver");
        dbConnection = DriverManager.getConnection(connectionString, Const.DB_USER, Const.DB_PASS);
        return dbConnection;
    }

    public void signUpUser(User user) {
        String insert = String.format("INSERT INTO %s(%s,%s,%s,%s,%s)VALUES(?,?,?,?,?)",
                Const.USER_TABLE, Const.USERS_FIRSTNAME, Const.USERS_LASTNAME, Const.USERS_USERNAME,
                Const.USERS_PASSWORD, Const.USERS_GENDER);
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insert);
            prSt.setString(1, user.getFirstName());
            prSt.setString(2, user.getLastName());
            prSt.setString(3, user.getUserName());
            prSt.setString(4, user.getPassword());
            prSt.setString(5, user.getGender());
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ResultSet getUser(User user) {
        ResultSet resultSet = null;

        //create a request to the database
        String select = String.format("SELECT * FROM %s WHERE %s=? AND %s=?",
                Const.USER_TABLE, Const.USERS_USERNAME, Const.USERS_PASSWORD);

        try {
            PreparedStatement psSt = getDbConnection().prepareStatement(select);
            psSt.setString(1, user.getUserName());
            psSt.setString(2, user.getPassword());

            resultSet = psSt.executeQuery();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public ResultSet getUserForSignUp(User user) {
        ResultSet resultSet = null;

        String select = String.format("SELECT * FROM %s WHERE %s=?",
                Const.USER_TABLE, Const.USERS_USERNAME);

        try {
            PreparedStatement psSt = getDbConnection().prepareStatement(select);
            psSt.setString(1, user.getUserName());
            resultSet = psSt.executeQuery();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return resultSet;
    }
}
