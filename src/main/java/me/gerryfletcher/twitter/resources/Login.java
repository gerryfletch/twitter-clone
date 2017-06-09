package me.gerryfletcher.twitter.resources;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.xml.internal.ws.client.sei.ResponseBuilder;
import me.gerryfletcher.twitter.config.JWTSecret;
import me.gerryfletcher.twitter.sqlite.SQLUtils;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.Date;

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
        JsonObject userDetails = gson.fromJson(json, JsonObject.class);
        String username = userDetails.get("username").getAsString();
        //TODO: Encrypt password
        String password = userDetails.get("password").getAsString();

        if(username.isEmpty() || password.isEmpty() || username.split(" ").length > 0 || password.split(" ").length > 0) {
            return failed("Bad username or password.");
        }

        int rowCount;
        int userId;
        int userRole;
        String query = "SELECT id, count(*) AS count FROM users WHERE username= ? AND password= ?";
        try (Connection conn = SQLUtils.connect();
             PreparedStatement st = conn.prepareStatement(query)){

            st.setString(1, username);
            st.setString(2, password);
            st.execute();
            ResultSet resultSet = st.getResultSet();

            rowCount = resultSet.getInt("count");

            if(rowCount != 1)
                return failed("No user found.");

            userId = resultSet.getInt("id");
        } catch (SQLException e) {
            System.out.println("SQL failiure.");
            System.out.println(e.getMessage());
            return failed("Database error.");
        }


        try {
            //TODO: Add lifespan
            long now = System.currentTimeMillis();
            Algorithm algorithm = Algorithm.HMAC256(JWTSecret.getKey());
            String token = JWT.create()
                    .withIssuer("auth0")
                    .withClaim("uid", userId) //ID from DB
                    .withClaim("role", "User") //role from DB  (alt: permission int? )
                    //.withExpiresAt(new Date( now + 7200 ))
                    .sign(algorithm);

            JsonObject returnJson = new JsonObject();
            returnJson.addProperty("authenticated", true);
            returnJson.addProperty("token", token);

            System.out.println("Returned json: " + returnJson);

            return Response.status(200).entity(gson.toJson(returnJson)).build();

            //return Response.ok(returnJson.getAsString(),MediaType.APPLICATION_JSON).status(200).build();
        } catch (UnsupportedEncodingException exception) {
            //UTF-8 encoding not supported
            System.out.println("UTF 8 encoding not supported.");
        } catch (JWTCreationException exception) {
            //Invalid Signing configuration / Couldn't convert Claims.
            System.out.println("Invalid signature configuration.");
        }

        return failed("Something went wrong.");
    }

    private Response failed(String error) {
        JsonObject returnFail = new JsonObject();
        returnFail.addProperty("error", error);
        return Response.status(401).entity(gson.toJson(returnFail)).build();
    }
}
