package me.gerryfletcher.twitter.resources.account;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserExistsException;
import me.gerryfletcher.twitter.models.DisplayName;
import me.gerryfletcher.twitter.models.Email;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.models.Password;
import me.gerryfletcher.twitter.services.RegisterService;
import me.gerryfletcher.twitter.utilities.ResourceUtils;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("register")
public class RegisterResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * Attempts to create a user.
     *
     * @param json JSON containing handle, display name, email and password.
     * @return 200 OK with JWT, or 403 Forbidden if unauthorized.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response registerUser(String json) {
        System.out.println(json);
        JsonObject request = gson.fromJson(json, JsonObject.class);

        String handle = request.get("handle").getAsString();
        String displayName = request.get("display_name").getAsString();
        String email = request.get("email").getAsString();
        String password = request.get("password").getAsString();

        if (!Handle.isHandleValid(handle)) {
            return ResourceUtils.unauthorized("Handle is not valid.", Response.Status.BAD_REQUEST);
        }

        if (!DisplayName.isDisplayNameValid(displayName)) {
            return ResourceUtils.unauthorized("Display Name is not valid.", Response.Status.BAD_REQUEST);
        }

        if (!Email.isEmailValid(email)) {
            return ResourceUtils.unauthorized("Email is not valid.", Response.Status.BAD_REQUEST);
        }

        if (!Password.isPasswordValid(password)) {
            return ResourceUtils.unauthorized("Password is not valid.", Response.Status.BAD_REQUEST);
        }

        RegisterService registerService = RegisterService.getInstance();

        try {
            String token = registerService.registerUser(handle, displayName, email, password);

            JsonObject returnSuccess = new JsonObject();
            returnSuccess.addProperty("authenticated", true);
            returnSuccess.addProperty("token", token);

            return Response.ok().entity(gson.toJson(returnSuccess)).build();

        } catch (BadDataException | UserExistsException e) {
            return ResourceUtils.unauthorized(e.getMessage(), Response.Status.BAD_REQUEST);
        } catch (ApplicationException e) {
            return ResourceUtils.unauthorized("Something went wrong on our end.", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
