package me.gerryfletcher.twitter.DAO;

import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.Password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginDao extends UtilDao {

    public LoginDao() {}

    private final String CHECK_USER_QUERY = "SELECT id, password, role FROM users WHERE lower(handle)=?";

    public String loginUser(String handle, String password) throws SQLException, UserNotExistsException, BadDataException {

        handle = handle.toLowerCase();

        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(CHECK_USER_QUERY)) {

            stmt.setString(1, handle);
            ResultSet result = stmt.executeQuery();

            if(! result.next()) {
                throw new UserNotExistsException("This handle does not exist.");
            }

            int uid = result.getInt("id");
            String role = result.getString("role");
            String passwordResult = result.getString("password");

            if(! Password.checkPassword(password, passwordResult)) {
                throw new BadDataException("Incorrect password.");
            }

            return new JWTSecret().generateToken(uid, handle, role);
        }

    }

}
