package me.gerryfletcher.twitter.resources.user;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.services.user.RelationshipService;
import me.gerryfletcher.twitter.services.user.UserService;
import me.gerryfletcher.twitter.utilities.ResourceUtils;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("profile/{handle}")
public class UserResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * TODO: Create a PermitAll roles version for public profiles.
     *
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
    @GET
    @RolesAllowed("User")
    public Response getUserProfile(@HeaderParam("authorization") String auth, @PathParam("handle") String handle) {
        try {
            if (!UserService.getInstance().doesHandleExist(handle)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (ApplicationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        JWTSecret jwt = new JWTSecret();
        int requestId = jwt.getClaim(HTTPRequestUtil.getJWT(auth), "uid").asInt();
        String requestHandle = jwt.getClaim(HTTPRequestUtil.getJWT(auth), "handle").asString();

        try {
            UserService us = UserService.getInstance();
            int userId = us.getUserId(handle);
            JsonObject profile = us.getUserJson(userId);

            RelationshipService rs = RelationshipService.getInstance();
            JsonObject relationship = rs.getRelationshipJson(requestId, userId);

            profile.add("relationship", relationship);
            profile.addProperty("self", requestHandle.equalsIgnoreCase(handle));

            return Response.ok().entity(gson.toJson(profile)).build();
        } catch (UserNotExistsException e) {
            e.printStackTrace();
            return ResourceUtils.unauthorized(e.getMessage(), Response.Status.FORBIDDEN);
        } catch (ApplicationException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
