package me.gerryfletcher.twitter.resources.AccountFunctions;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.config.JWTSecret;
import me.gerryfletcher.twitter.resources.ResourceUtils;
import me.gerryfletcher.twitter.sqlite.SQLUtils;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;

/**
 * Created by Gerry on 09/06/2017.
 */
@Path("/login")
public class Login {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response attemptLogin(String json) {
        System.out.println("Login attempted: " + json);
        JsonObject userDetails = gson.fromJson(json, JsonObject.class);
        String handle = userDetails.get("handle").getAsString();
        String password = userDetails.get("password").getAsString();

        if (handle.isEmpty() || password.isEmpty()) {
            return ResourceUtils.failed("Handle or Password is empty.");
        }

        if (!Password.isPasswordValid(password)) {
            return ResourceUtils.failed("Password is not valid.");
        }

        if (!Handle.isHandleValid(handle)) {
            return ResourceUtils.failed("Handle is not valid.");
        }

        if (!Handle.doesHandleExist(handle)) {
            return ResourceUtils.failed("Handle does not exist.");
        }

        if (!isLoginValid(handle, password)) {
            return ResourceUtils.failed("Incorrect password.");
        }

        int userId = Handle.getUserId(handle);
        String role = Handle.getUserRole(handle);

        String token = new JWTSecret().generateToken(userId, role);

        if (token == null) {
            return ResourceUtils.failed("Unkown error.");
        }

        JsonObject returnSuccess = new JsonObject();
        returnSuccess.addProperty("authenticated", true);
        returnSuccess.addProperty("token", token);

        System.out.println("[" + role + "] " + handle + " just logged in.");

        return Response.status(200).entity(gson.toJson(returnSuccess)).build();
    }

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
