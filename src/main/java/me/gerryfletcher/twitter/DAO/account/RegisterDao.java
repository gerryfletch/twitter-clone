package me.gerryfletcher.twitter.DAO.account;


import me.gerryfletcher.twitter.DAO.UtilDao;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserExistsException;
import me.gerryfletcher.twitter.models.Password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterDao extends UtilDao {

    private final String REGISTER_USER_QUERY =
            "INSERT INTO users ("
                    + "handle,"
                    + "display_name,"
                    + "email,"
                    + "password,"
                    + "role"
                    + ") VALUES ("
                    + "?,?,?,?, 'user');";

    public RegisterDao() {
    }

    public String registerUser(String handle, String displayName, String email, String password) throws BadDataException, UserExistsException, SQLException {
        assertUnique(handle, email);
        password = Password.hashPassword(password);
        int uid = createUser(handle, displayName, email, password);

        return new JWTSecret().generateToken(uid, handle, "user");
    }

    private int createUser(String handle, String displayName, String email, String password) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(REGISTER_USER_QUERY)) {

            stmt.setString(1, handle);
            stmt.setString(2, displayName);
            stmt.setString(3, email);
            stmt.setString(4, password);

            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();

            if (!generatedKeys.next()) {
                throw new SQLException();
            }

            return generatedKeys.getInt(1);
        }
    }

    private void assertUnique(String handle, String email) throws UserExistsException, SQLException {
        handle = handle.toLowerCase();
        email = email.toLowerCase();

        if (doesRecordExist("users", "handle", handle)) {
            throw new UserExistsException("This handle is in use.");
        }
        if (doesRecordExist("users", "email", email)) {
            throw new UserExistsException("This email is in use.");
        }
    }

}
