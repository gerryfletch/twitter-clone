package me.gerryfletcher.twitter.resources;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.user.Handle;
import me.gerryfletcher.twitter.controllers.user.User;
import me.gerryfletcher.twitter.controllers.utils.ResourceUtils;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserSqlException;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

/**
 * Created by Gerry on 09/06/2017.
 */
@Path("user")
public class UserResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /*
        Returns a users public profile data:
            - Display name
            - Uid
            - Number of tweets
            - Number of followers
            - Number of following
     */
    @Path("{handle}")
    @PermitAll
    @GET
    public Response getUserProfile(@PathParam("handle") String handle) {

        if (!Handle.doesHandleExist(handle.toLowerCase())) {
            return Response.status(404).build();
        }

        try(User user = new User(handle)) {
            JsonObject profile = user.getProfile();
            return Response.ok().entity(gson.toJson(profile)).build();
        } catch (BadDataException | SQLException e) {
            return ResourceUtils.failed(e.getMessage(), 403); // Return forbidden
        }
    }
}
