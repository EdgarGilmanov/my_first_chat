package ru.chat.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.chat.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class UserStore implements Store<User> {
    private final Logger log = LoggerFactory.getLogger(UserStore.class);
    private final BasicDataSource pool;

    public UserStore(BasicDataSource pool) {
        this.pool = pool;
    }

    @Override
    public User save(User user) {
        try (Connection cnn = pool.getConnection();
             PreparedStatement st = cnn.prepareStatement(
                     "INSERT INTO users (first_name, last_name, user_name, password, gender) " +
                             "VALUES (?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getUserName());
            st.setString(4, user.getPassword());
            st.setBoolean(5, user.getGender());
            st.execute();
            try (ResultSet rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            log.error("SQL", e);
        }
        return user;
    }

    @Override
    public boolean delete(User user) {
        try (Connection cnn = pool.getConnection();
             PreparedStatement st = cnn.prepareStatement(
                    "DELETE FROM users WHERE id = ?")) {
            st.setInt(1, user.getId());
            st.execute();
            return true;
        } catch (Exception e) {
            log.error("SQL", e);
        }
        return false;
    }

    @Override
    public Collection<User> getAll() {
        Collection<User> rsl = new ArrayList<>();
        try (Connection cnn = pool.getConnection();
             PreparedStatement st = cnn.prepareStatement(
                    "SELECT * FROM users")) {
            try (ResultSet rs = st.executeQuery()){
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setUserName(rs.getString("user_name"));
                    user.setPassword(rs.getString("password"));
                    user.setGender(rs.getBoolean("gender"));
                }
            }
        } catch (Exception e) {
            log.error("SQL", e);
        }
        return rsl;
    }

    @Override
    public User update(User user) {
        try (Connection cnn = pool.getConnection();
             PreparedStatement st = cnn.prepareStatement(
                    "UPDATE users SET first_name = ?, last_name = ?, user_name = ?, password = ?, gender = ?")) {
            st.setString(1, user.getFirstName());
            st.setString(2, user.getLastName());
            st.setString(3, user.getUserName());
            st.setString(4, user.getPassword());
            st.setBoolean(5, user.getGender());
            st.executeUpdate();
        } catch (Exception e) {
            log.error("SQL", e);
        }
        return user;
    }

    @Override
    public Optional<User> findBy(String userName) {
        Optional<User> rsl = Optional.empty();
        try (Connection cnn = pool.getConnection();
             PreparedStatement st = cnn.prepareStatement(
                     "SELECT * FROM users WHERE user_name = ?")) {
            st.setString(1, userName);
            try (ResultSet rs = st.executeQuery()){
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setUserName(rs.getString("user_name"));
                    user.setPassword(rs.getString("password"));
                    user.setGender(rs.getBoolean("gender"));
                }
            }
        } catch (Exception e) {
            log.error("SQL", e);
        }
        return rsl;
    }

    @Override
    public Optional<User> findById(String id) {
        Optional<User> rsl = Optional.empty();
        try (Connection cnn = pool.getConnection();
             PreparedStatement st = cnn.prepareStatement(
                     "SELECT * FROM users WHERE id = ?")) {
            st.setInt(1, Integer.parseInt(id));
            try (ResultSet rs = st.executeQuery()){
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setFirstName(rs.getString("first_name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setUserName(rs.getString("user_name"));
                    user.setPassword(rs.getString("password"));
                    user.setGender(rs.getBoolean("gender"));
                }
            }
        } catch (Exception e) {
            log.error("SQL", e);
        }
        return rsl;
    }
}
