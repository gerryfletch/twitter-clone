package me.gerryfletcher.twitter.resources.user;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.models.DisplayName;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.services.EditUserService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URL;

@Path("user/{handle}/edit")
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
    @GET
    @RolesAllowed("user")
    public Response verifyUser(@HeaderParam("authorization") String auth, @PathParam("handle") String handle) {
        if (doesAuthMatchUser(auth, handle)) {
            return Response.ok().build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
    @RolesAllowed("user")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("authorization") String auth, @PathParam("handle") String handle, String requestJson) {

        System.out.println("EDIT called with request: " + requestJson);

        if (!doesAuthMatchUser(auth, handle)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String token = HTTPRequestUtil.getJWT(auth);
        JWTSecret jwt = new JWTSecret();

        int uid = jwt.getClaim(token, "uid").asInt();

        JsonObject request = gson.fromJson(requestJson, JsonObject.class);
        EditUserService editService = EditUserService.getInstance();

        if (request.has("display_name")) {
            String displayName = request.get("display_name").getAsString();
            if (!DisplayName.isDisplayNameValid(displayName)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            try {
                editService.updateDisplayName(uid, displayName);
            } catch (ApplicationException e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        if (request.has("profile_picture")) {
            String profilePicture = request.get("profile_picture").getAsString();
            URL pictureHref;
            try {
                pictureHref = new URL(profilePicture);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            try {
                editService.updateProfilePicture(uid, pictureHref);
            } catch (ApplicationException e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        if (request.has("bio")) {
            String bio = request.get("bio").getAsString();
            try {
                editService.updateBio(uid, bio);
            } catch (ApplicationException e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        return Response.ok().build();
    }

    private boolean doesAuthMatchUser(String auth, String handle) {
        if (!Handle.isHandleValid(handle)) {
            return false;
        }

        String token = HTTPRequestUtil.getJWT(auth);
        String authHandle = new JWTSecret().getClaim(token, "handle").asString();

        return authHandle.equalsIgnoreCase(handle);
    }


}
