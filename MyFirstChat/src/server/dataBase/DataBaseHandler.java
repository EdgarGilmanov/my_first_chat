package server.dataBase;

import server.User;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.Properties;

public class DataBaseHandler {
    Connection dbConnection;

    Properties properties;
    Reader reader;
    private final String USER_TABLE;
    private final String USERS_ID;
    private final String USERS_FIRSTNAME;
    private final String USERS_LASTNAME;
    private final String USERS_USERNAME;
    private final String USERS_PASSWORD;
    private final String USERS_GENDER;

    //connection options for MySQL
    private final String DB_HOST;
    private final String DB_PORT;
    private final String DB_USER;
    private final String DB_PASS;
    private final String DB_NAME;

    public DataBaseHandler() throws IOException {
        this.properties = new Properties();
        this.reader = new FileReader("src/ConnectionParam.properties");
        this.properties.load(reader);
        this.USER_TABLE = properties.getProperty("USER_TABLE");
        this.USERS_ID = properties.getProperty("USERS_ID");
        this.USERS_FIRSTNAME = properties.getProperty("USERS_FIRSTNAME");
        this.USERS_LASTNAME = properties.getProperty("USERS_LASTNAME");
        this.USERS_USERNAME = properties.getProperty("USERS_USERNAME");
        this.USERS_PASSWORD = properties.getProperty("USERS_PASSWORD");
        this.USERS_GENDER = properties.getProperty("USERS_GENDER");
        this.DB_HOST = properties.getProperty("DB_HOST");
        this.DB_PORT = properties.getProperty("DB_PORT");
        this.DB_USER = properties.getProperty("DB_USER");
        this.DB_PASS = properties.getProperty("DB_PASS");
        this.DB_NAME = properties.getProperty("DB_NAME");
        reader.close();
        properties = null;
    }

    private Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connectionString = "jdbc:mysql://" + DB_HOST+ ":" + DB_PORT
                + "/" + DB_NAME; //jdbc:mysql://localhost/db?useUnicode=true&serverTimezone=UTC&useSSL=true&verifyServerCertificate=false
        Class.forName("com.mysql.cj.jdbc.Driver");
        dbConnection = DriverManager.getConnection(connectionString, DB_USER, DB_PASS);
        return dbConnection;
    }

    public void signUpUser(User user) {
        String insert = String.format("INSERT INTO %s(%s,%s,%s,%s,%s)VALUES(?,?,?,?,?)",
                USER_TABLE, USERS_FIRSTNAME, USERS_LASTNAME, USERS_USERNAME,
                USERS_PASSWORD, USERS_GENDER);
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

    public ResultSet getUser(server.User user) {
        ResultSet resultSet = null;

        //create a request to the database
        String select = String.format("SELECT * FROM %s WHERE %s=? AND %s=?",
                USER_TABLE, USERS_USERNAME, USERS_PASSWORD);

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
                USER_TABLE, USERS_USERNAME);

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
