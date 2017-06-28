package me.gerryfletcher.twitter.resources.account;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.controllers.user.Handle;
import me.gerryfletcher.twitter.controllers.user.Password;
import me.gerryfletcher.twitter.services.UserService;
import me.gerryfletcher.twitter.utilities.ResourceUtils;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;

@Path("/login")
public class LoginResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();


    /**
     * Checks if a login is valid, verifies it against the DB and
     * creates a JSON web token to be included in the response for
     * local storage in the browser.
     * @param json  JSON made up of username + password
     * @return      200 OK and JWT if successful, or a 401 Unauthorized
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response attemptLogin(String json) throws SQLException {
        System.out.println("Login attempted: " + json);

        JsonObject userDetails = gson.fromJson(json, JsonObject.class);

        String handle = userDetails.get("handle").getAsString();
        String password = userDetails.get("password").getAsString();

        /*
         *  Checks for each data type, returning a user-friendly error
         *  and 401 Unauthorized HTTP response code
         */

        if (handle.isEmpty() || password.isEmpty()) {
            return ResourceUtils.failed("Handle or Password is empty.");
        }

        if (!Password.isPasswordValid(password)) {
            return ResourceUtils.failed("Password is not valid.");
        }

        if (!Handle.isHandleValid(handle)) {
            return ResourceUtils.failed("Handle is not valid.");
        }

        if (! UserService.getInstance().doesHandleExist(handle)) {
            return ResourceUtils.failed("Handle does not exist.");
        }

        if (!isLoginValid(handle, password)) {
            return ResourceUtils.failed("Incorrect password.");
        }

        int userId = Handle.getUserId(handle);
        String role = Handle.getUserRole(handle);

        /*
            Generate the JSON Web Token with ID and Role in the payload
         */
        String token = new JWTSecret().generateToken(userId, role);

        if (token == null) {
            return ResourceUtils.failed("Unkown error.");
        }

        JsonObject returnSuccess = new JsonObject();
        returnSuccess.addProperty("authenticated", true);
        returnSuccess.addProperty("handle", handle);
        returnSuccess.addProperty("uid", userId);
        returnSuccess.addProperty("token", token);

        System.out.println("[" + role + "] " + handle + " just logged in.");

        return Response.status(200).entity(gson.toJson(returnSuccess)).build();
    }

    /**
     * Checks the plaintext password against the hashed password in the DB.
     * @param handle    The users handle.
     * @param password  The users plaintext password.
     * @return          True/False on matching passwords.
     */
    private boolean isLoginValid(String handle, String password) {
        String query = "SELECT password FROM users WHERE handle= ?";
        try (Connection conn = SQLUtils.connect();
             PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, handle);
            st.execute();
            ResultSet resultSet = st.getResultSet();

            String resultPassword = resultSet.getString("password");

            if (!Password.checkPassword(password, resultPassword)) {
                return false;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

}
