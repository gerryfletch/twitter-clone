package me.gerryfletcher.twitter.resources.AccountFunctions;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.controllers.user.Handle;
import me.gerryfletcher.twitter.controllers.user.Password;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserSqlException;
import me.gerryfletcher.twitter.controllers.utils.ResourceUtils;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Path("register")
public class RegisterResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();


    private Connection conn = SQLUtils.connect();

    /**
     * Attempts to create a user.
     * @param json  JSON containing handle, display name, email and password.
     * @return 200 OK with JWT, or 403 Forbidden if failed.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response registerUser(String json) {
        JsonObject request = gson.fromJson(json, JsonObject.class);

        String handle;
        String display_name;
        String email;
        String password;
        int uid;
        // Check that form data is valid
        try {
            handle = processHandle(request.getAsJsonPrimitive("handle").getAsString());
            display_name = processDisplayName(request.getAsJsonPrimitive("display_name").getAsString());
            email = processEmail(request.getAsJsonPrimitive("email").getAsString());
            password = processPassword(request.getAsJsonPrimitive("password").getAsString());
        } catch (BadDataException e) {
            return ResourceUtils.failed(e.getMessage(), 400);
        }

        // Check that the handle and email are not in use
        try {
            veriyUnique(handle, email);
        } catch (UserSqlException e) {
            return ResourceUtils.failed("Handle or Email in use.", 403);
        }

        try {
            uid = createUser(handle, display_name, email, password);
        } catch (UserSqlException e) {
            return ResourceUtils.failed("Problem creating user.", 403);
        }

        return success(handle, display_name, uid);
    }

    /**
     * Creates a sucessful response.
     * @param handle The users handle.
     * @param display_name The users display name.
     * @param uid The users unique ID.
     * @return 200OK Response with JSON holding the JWT.
     */
    private Response success(String handle, String display_name, int uid) {
        JsonObject returnSuccess = new JsonObject();

        JWTSecret jwtSecret = new JWTSecret();
        String token = jwtSecret.generateToken(uid, "UserResource");

        returnSuccess.addProperty("uid", uid);
        returnSuccess.addProperty("handle", handle);
        returnSuccess.addProperty("display_name", display_name);
        returnSuccess.addProperty("token", token);

        return Response.ok().entity(gson.toJson(returnSuccess)).build();
    }

    /**
     * Checks that a registering user is unique, by looking up the username and email
     * in the DB.
     * @param handle    The users handle.
     * @param email     The users email.
     * @throws UserSqlException if an account already exists.
     */
    private void veriyUnique(String handle, String email) throws UserSqlException {
        String checkSql =
                "SELECT id, handle, email "
                        + "FROM users "
                        + "WHERE "
                        + "(handle=? OR email=?);";

        try (PreparedStatement checkAccount = this.conn.prepareStatement(checkSql)) {
            checkAccount.setString(1, handle);
            checkAccount.setString(2, email);

            checkAccount.execute();

            ResultSet resultSet = checkAccount.getResultSet();

            while (resultSet.next()) {
                String resultEmail = resultSet.getString("email").toLowerCase();
                String resultHandle = resultSet.getString("handle");

                if (resultEmail.equals(email) && resultHandle.equalsIgnoreCase(handle)) {
                    throw new UserSqlException("Account already exists with this username or email.");
                } else if (resultEmail.equals(email)) {
                    throw new UserSqlException("Account already exists with this email.");
                } else if (resultHandle.equalsIgnoreCase(handle)) {
                    throw new UserSqlException("Account already exists with this handle.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new UserSqlException("There was an issue registering your account. Error #2", e);
        }
    }

    /**
     * Creates the new user in the database.
     * @param handle    The users handle.
     * @param display_name  The users display name.
     * @param email The users email.
     * @param password  The users Hashed password.
     * @return  The newly created users ID.
     * @throws UserSqlException If there is a problem creating the user.
     */
    private int createUser(String handle, String display_name, String email, String password) throws UserSqlException {

        String registerSql =
                "INSERT INTO users ("
                        + "handle,"
                        + "display_name,"
                        + "email,"
                        + "password,"
                        + "role"
                        + ") VALUES ("
                        + "?,?,?,?, 'user');";

        try (PreparedStatement registerUser = this.conn.prepareStatement(registerSql)) {

            registerUser.setString(1, handle);
            registerUser.setString(2, display_name);
            registerUser.setString(3, email);
            registerUser.setString(4, password);

            registerUser.executeUpdate();

            try (ResultSet generatedKeys = registerUser.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new UserSqlException("Unable to process user ID.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new UserSqlException("There was an error creating the user.", e);
        }
    }

    /**
     * Uses the Handle class to check that the handle is valid.
     * @param handle    The users handle.
     * @return  The handle (if it is valid.)
     * @throws BadDataException If the handle is not valid.
     */
    private String processHandle(String handle) throws BadDataException {
        if (!Handle.isHandleValid(handle)) {
            throw new BadDataException("Handle is not valid.");
        }
        return handle;
    }

    /**
     * The Display Name can be any characters, it just has a length limit of 3-15.
     * @param display_name  The users display name.
     * @return  The display name (if it is valid).
     * @throws BadDataException If the display name is not valid.
     */
    private String processDisplayName(String display_name) throws BadDataException {
        if (display_name.length() < 3 || display_name.length() > 15) {
            throw new BadDataException("Display name is bad length.");
        }

        return display_name;
    }

    /**
     * Uses a 99.99% accurate email regex to validate it.
     * @param email The users email.
     * @return  The email (if it is valid).
     * @throws BadDataException If the email is not valid.
     */
    private String processEmail(String email) throws BadDataException {
        /*
            Email regex:
            http://emailregex.com/
         */

        email = email.toLowerCase();

        String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            throw new BadDataException("Badly formatted email.");
        }

        return email;
    }

    /**
     * Uses the Password class to validate the password.
     * @param password  The users plaintext password.
     * @return  The users hashed password (if it is valid).
     * @throws BadDataException If the password is not valid.
     */
    private String processPassword(String password) throws BadDataException {
        if (!Password.isPasswordValid(password)) {
            throw new BadDataException("Badly formatted password.");
        }

        return Password.hashPassword(password);
    }

}
