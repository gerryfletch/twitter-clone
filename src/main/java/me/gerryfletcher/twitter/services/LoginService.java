package me.gerryfletcher.twitter.services;

import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.models.Password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {

    private static LoginService instance = null;

    private static Connection conn = SQLUtils.connect();

    protected LoginService() throws SQLException {}

    public static LoginService getInstance() {
        if(instance == null) {
            try {
                instance = new LoginService();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    private final String check_user_SQL = "SELECT id, password, role FROM users WHERE lower(handle)=?";
    private final PreparedStatement check_user = conn.prepareStatement(check_user_SQL);

    /**
     * Checks if the handle and password are valid, then attempts login.
     * @param handle    Handle to be checked
     * @param password  Plain-text password
     * @return  The JSON Web token
     * @throws UserNotExistsException If the user does not exist
     * @throws BadDataException If a detail is wrong
     */
    public String loginUser(String handle, String password) throws UserNotExistsException, BadDataException, SQLException {

        handle = handle.toLowerCase();

        if(! Handle.isHandleValid(handle))
            throw new BadDataException("Username is not valid.");

        if(! Password.isPasswordValid(password))
            throw new BadDataException("Password is not valid.");


        try {
            check_user.setString(1, handle);
            ResultSet result = check_user.executeQuery();

            if(! result.next()) {
                throw new UserNotExistsException("User " + handle + " does not exist.");
            }

            int uid = result.getInt("id");
            String role = result.getString("role");
            String passwordResult = result.getString("password");

            if(! Password.checkPassword(password, passwordResult)) {
                throw new BadDataException("Incorrect password.");
            }

            return new JWTSecret().generateToken(uid, handle, role);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Something went wrong logging in.");
        }

    }

}
