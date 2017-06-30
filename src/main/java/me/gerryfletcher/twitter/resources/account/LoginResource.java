package me.gerryfletcher.twitter.resources.account;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.models.Password;
import me.gerryfletcher.twitter.services.LoginService;
import me.gerryfletcher.twitter.utilities.ResourceUtils;

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
     * @param json  JSON made up of handle + password
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
         *  and 400 Bad Request / 401 Unauthorized HTTP response code
         */

        if (handle.isEmpty() || password.isEmpty()) {
            return ResourceUtils.unauthorized("Handle or Password is empty.");
        }

        if(!Handle.isHandleValid(handle)) {
            return ResourceUtils.unauthorized("Handle is invalid.", Response.Status.BAD_REQUEST);
        }

        if(!Password.isPasswordValid(password)) {
            return ResourceUtils.unauthorized("Password is invalid", Response.Status.BAD_REQUEST);
        }

        String token;

        try {
            LoginService loginService = LoginService.getInstance();
            token = loginService.loginUser(handle, password);
        } catch (BadDataException | UserNotExistsException e) {
            return ResourceUtils.unauthorized(e.getMessage());
        } catch (ApplicationException ev) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        JsonObject returnSuccess = new JsonObject();
        returnSuccess.addProperty("authenticated", true);
        returnSuccess.addProperty("token", token);

        System.out.println(handle + " just logged in.");

        return Response.ok().entity(gson.toJson(returnSuccess)).build();
    }

}
