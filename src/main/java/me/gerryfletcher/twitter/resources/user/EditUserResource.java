package me.gerryfletcher.twitter.resources.user;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.services.UserService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("user")
public class EditUserResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * This initial GET checks if the user who sent the request
     * is the same as the path handle.
     *
     * @param auth   The JWT Bearer authentication sent in the HTTP request.
     * @param handle The handle to be edited.
     * @return Response 200 OK if it is fine, or unauthorized.
     */
    @Path("{handle}/edit")
    @RolesAllowed("User")
    @GET
    public Response verifyUser(@HeaderParam("authorization") String auth, @PathParam("handle") String handle) {

        try {
            UserService userService = UserService.getInstance();

            String token = HTTPRequestUtil.getJWT(auth); // JWT
            JWTSecret jwtSecret = new JWTSecret();

            int headerUID = jwtSecret.getClaim(token, "uid").asInt(); // User ID
            String headerHandle = userService.getUserHandle(headerUID);

            if (!headerHandle.equals(handle)) {
                JsonObject response = new JsonObject();
                response.addProperty("handle", handle);
                return Response
                        .status(Response.Status.UNAUTHORIZED)
                        .entity(gson.toJson(response))
                        .build();
            } else {
                return Response.ok().build();
            }

        } catch (UserNotExistsException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (ApplicationException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
