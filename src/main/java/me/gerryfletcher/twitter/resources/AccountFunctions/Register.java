package me.gerryfletcher.twitter.resources.AccountFunctions;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.config.JWTSecret;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserSqlException;
import me.gerryfletcher.twitter.resources.ResourceUtils;
import me.gerryfletcher.twitter.sqlite.SQLUtils;

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

/**
 * Created by Gerry on 10/06/2017.
 */

@Path("register")
public class Register {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();


    private Connection conn = SQLUtils.connect();

    /*
        SELECT user id
        Generate JWT
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response registerUser(String json) {
        JsonObject request = gson.fromJson(json, JsonObject.class);

        String handle;
        String displayName;
        String email;
        String password;
        int uid;
        // Check that form data is valid
        try {
            handle = processHandle(request.getAsJsonPrimitive("handle").getAsString());
            displayName = processDisplayName(request.getAsJsonPrimitive("displayName").getAsString());
            email = processEmail(request.getAsJsonPrimitive("email").getAsString());
            password = processPassword(request.getAsJsonPrimitive("password").getAsString());
        } catch (BadDataException e) {
            return ResourceUtils.failed(e.getMessage());
        }

        // Check that the handle and email are not in use
        try {
            veriyUnique(handle, email);
        } catch (UserSqlException e) {
            return ResourceUtils.failed(e.getMessage());
        }

        try {
            uid = createUser(handle, displayName, email, password);
        } catch (UserSqlException e) {
            return ResourceUtils.failed(e.getMessage());
        }

        return success(handle, displayName, uid);
    }

    private Response success(String handle, String displayName, int uid) {
        JsonObject returnSuccess = new JsonObject();

        JWTSecret jwtSecret = new JWTSecret();
        String token = jwtSecret.generateToken(uid, "User");

        returnSuccess.addProperty("uid", uid);
        returnSuccess.addProperty("handle", handle);
        returnSuccess.addProperty("displayName", displayName);
        returnSuccess.addProperty("token", token);

        return Response.ok().entity(gson.toJson(returnSuccess)).build();
    }

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

                if (resultEmail.equals(email) && resultHandle.equals(handle)) {
                    throw new UserSqlException("Account already exists with this username or email.");
                } else if (resultEmail.equals(email)) {
                    throw new UserSqlException("Account already exists with this email.");
                } else if (resultHandle.equals(handle)) {
                    throw new UserSqlException("Account already exists with this handle.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new UserSqlException("There was an issue registering your account. Error #2", e);
        }
    }

    private int createUser(String handle, String displayName, String email, String password) throws UserSqlException {

        String registerSql =
                "INSERT INTO users ("
                        + "handle,"
                        + "displayName,"
                        + "email,"
                        + "password,"
                        + "role"
                        + ") VALUES ("
                        + "?,?,?,?, 'user');";

        try (PreparedStatement registerUser = this.conn.prepareStatement(registerSql)) {

            registerUser.setString(1, handle);
            registerUser.setString(2, displayName);
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

    private String processHandle(String handle) throws BadDataException {
        handle = handle.toLowerCase();
        if (!Handle.isHandleValid(handle)) {
            throw new BadDataException("Username is not valid.");
        }
        return handle;
    }

    private String processDisplayName(String displayName) throws BadDataException {
        if (displayName.length() < 3 || displayName.length() > 15) {
            throw new BadDataException("Display name is bad length.");
        }

        return displayName;
    }

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

    private String processPassword(String password) throws BadDataException {
        if (!Password.isPasswordValid(password)) {
            throw new BadDataException("Badly formatted password.");
        }

        return Password.hashPassword(password);
    }

}
