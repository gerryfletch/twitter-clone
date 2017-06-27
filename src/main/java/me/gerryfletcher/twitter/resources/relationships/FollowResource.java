package me.gerryfletcher.twitter.resources.relationships;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.controllers.user.Handle;
import me.gerryfletcher.twitter.exceptions.BadDataException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/relationship")
public class FollowResource {

    private Gson gson = new Gson();

    @POST
    @Path("/follow")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response followUser(@HeaderParam("authorization") String auth, String requestJson) {

        JsonObject request = gson.fromJson(requestJson, JsonObject.class);
        String followHandle = request.get("handle").getAsString();
        String handle;

        try {
            handle = Handle.getHandleFromAuth(auth);

            if(handle.equalsIgnoreCase(followHandle)) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (BadDataException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }
}
