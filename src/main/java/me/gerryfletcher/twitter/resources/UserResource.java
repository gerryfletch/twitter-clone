package me.gerryfletcher.twitter.resources;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.utilities.ResourceUtils;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.services.RelationshipService;
import me.gerryfletcher.twitter.services.UserService;

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
     * - Display name
     * - Profile Picture
     * - Bio
     * - Uid
     * - Number of tweets
     * - Number of followers
     * - Number of following
     * - If this is the users profile
     * - If you are following this user
     *
     * @param handle The users handle.
     * @return Response 200 OK with the profile in JSON, 404 not found, or 403 forbidden if there is another error.
     */
    @Path("{handle}")
    @RolesAllowed("User")
    @GET
    public Response getUserProfile(@HeaderParam("authorization") String auth, @PathParam("handle") String handle) {

        try {
            if (!UserService.getInstance().doesHandleExist(handle)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        JWTSecret jwt = new JWTSecret();
        int requestId;
        try {
            requestId = jwt.getClaim(HTTPRequestUtil.getJWT(auth), "uid").asInt();
        } catch (BadDataException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        try {
            UserService us = UserService.getInstance();
            int userId = us.getUserId(handle);
            JsonObject profile = us.getJsonProfile(userId);

            RelationshipService rs = RelationshipService.getInstance();
            JsonObject relationship = rs.getRelationshipJson(requestId, userId);
            profile.add("relationship", relationship);

            return Response.ok().entity(gson.toJson(profile)).build();
        } catch (UserNotExistsException e) {
            e.printStackTrace();
            return ResourceUtils.unauthorized(e.getMessage(), Response.Status.FORBIDDEN);
        } catch (SQLException ev) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * This initial GET checks if the user who sent the request
     * is the same as the path handle.
     * @param auth  The JWT Bearer authentication sent in the HTTP request.
     * @param handle    The handle to be edited.
     * @return  Response 200 OK if it is fine, or unauthorized.
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
            String headerHandle = userService.getHandle(headerUID);

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

        } catch (BadDataException | UserNotExistsException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
