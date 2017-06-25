package me.gerryfletcher.twitter.resources;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.controllers.user.Handle;
import me.gerryfletcher.twitter.controllers.user.User;
import me.gerryfletcher.twitter.controllers.utils.ResourceUtils;
import me.gerryfletcher.twitter.exceptions.BadDataException;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.sql.SQLException;

@Path("user")
public class UserResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * Returns a users public profile data:
     *  - Display name
     *  - Uid
     *  - Number of tweets
     *  - Number of followers
     *  - Number of following
     * @param handle    The users handle.
     * @return  Response 200 OK with the profile in JSON, 404 not found, or 403 forbidden if there is another error.
     */
    @Path("{handle}")
    @PermitAll
    @GET
    public Response getUserProfile(@PathParam("handle") String handle) {

        if (!Handle.doesHandleExist(handle.toLowerCase())) {
            return Response.status(404).build();
        }

        try(User user = new User(111)) {
            JsonObject profile = user.getProfile();
            return Response.ok().entity(gson.toJson(profile)).build();
        } catch (BadDataException | SQLException e) {
            return ResourceUtils.failed(e.getMessage(), 403); // Return forbidden
        }
    }

    @Path("{handle}/edit")
    @RolesAllowed("User")
    @GET
    public Response verifyUser(@HeaderParam("authorization") String auth, @PathParam("handle") String handle) {
        try {
            String token = HTTPRequestUtil.getJWT(auth); // JWT
            JWTSecret jwtSecret = new JWTSecret();

            int headerUID = jwtSecret.getClaim(token, "uid").asInt(); // User ID
            String headerHandle = Handle.getUserHandle(headerUID);

            if(! headerHandle.equals(handle)) {
                return ResourceUtils.failed("Bad credential. Redirect user to /handle.", 401);
            } else {
                System.out.println("returning true");
                return Response.ok().build();
            }

        } catch (BadDataException e) {
            e.printStackTrace();
            return Response.status(400).build();
        }
    }
}
