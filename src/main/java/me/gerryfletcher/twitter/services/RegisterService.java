package me.gerryfletcher.twitter.services;

import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserExistsException;
import me.gerryfletcher.twitter.models.DisplayName;
import me.gerryfletcher.twitter.models.Email;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.models.Password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegisterService {

    private static RegisterService instance = null;

    private static Connection conn = SQLUtils.connect();

    RegisterService() throws SQLException {}

    public static RegisterService getInstance() {
        if(instance == null) {
            try {
                instance = new RegisterService();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    /**
     * Tries to create a new user by validating the credentials and creating a record.
     * @param handle    The users handle.
     * @param displayName   The users Display Name.
     * @param email The users email.
     * @param password  The users password.
     * @throws BadDataException If an item is not valid.
     * @throws UserExistsException  If a user with this email/handle exists.
     * @throws SQLException If the DB fails to create a record.
     */
    public String registerUser(String handle, String displayName, String email, String password) throws BadDataException, SQLException, UserExistsException {
        assertValid(handle, displayName, email, password);
        assertUnique(handle, email);
        password = Password.hashPassword(password);
        int uid = createUser(handle, displayName, email, password);
        return new JWTSecret().generateToken(uid, handle, "user");
    }

    private final String register_user_SQL =
            "INSERT INTO users ("
                    + "handle,"
                    + "display_name,"
                    + "email,"
                    + "password,"
                    + "role"
                    + ") VALUES ("
                    + "?,?,?,?, 'user');";
    private final PreparedStatement register_user = conn.prepareStatement(register_user_SQL);

    /**
     * Creates the user, then selects the generated user ID.
     * @param handle    The users handle.
     * @param displayName   The users display name.
     * @param email The users email.
     * @param password  The users password.
     * @return  The newly created users ID.
     * @throws SQLException If the DB fails to create the record.
     */
    private int createUser(String handle, String displayName, String email, String password) throws SQLException {
        try {
            register_user.setString(1, handle);
            register_user.setString(2, displayName);
            register_user.setString(3, email);
            register_user.setString(4, password);

            register_user.executeUpdate();

            ResultSet generatedKeys = register_user.getGeneratedKeys();
            if(generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Something went wrong. Unable to process user ID.");
            }

        } catch (SQLException e) {
            throw new SQLException("Something went wrong.");
        }
    }

    /**
     * Checks that a user is unique using the User service.
     * @param handle    The users handle.
     * @param email The users email.
     * @throws SQLException If the DB breaks.
     * @throws UserExistsException  If the user does exist
     */
    private void assertUnique(String handle, String email) throws SQLException, UserExistsException {
        try {
            UserService userService = UserService.getInstance();
            if(userService.doesHandleExist(handle)) {
                throw new UserExistsException("This username already exists.");
            }
            if(userService.doesEmailExist(email)) {
                throw new UserExistsException("This email already exists.");
            }
        } catch (SQLException e) {
            throw new SQLException("Something went wrong.");
        }
    }

    private void assertValid(String handle, String displayName, String email, String password) throws BadDataException {
        if (!Handle.isHandleValid(handle))
            throw new BadDataException("Handle is not valid.");

        if (!DisplayName.isDisplayNameValid(displayName))
            throw new BadDataException("Display Name is not valid.");

        if (!Email.isEmailValid(email))
            throw new BadDataException("Email is not valid.");

        if (!Password.isPasswordValid(password))
            throw new BadDataException("Password is not valid.");
    }
}
