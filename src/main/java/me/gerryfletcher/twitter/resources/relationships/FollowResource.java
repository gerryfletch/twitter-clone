package me.gerryfletcher.twitter.resources.relationships;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.gerryfletcher.twitter.controllers.relationships.RelationshipType;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.controllers.user.Handle;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.services.RelationshipService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;


@Path("/relationship")
public class FollowResource {

    private Gson gson = new Gson();

    @POST
    @Path("/follow")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response followUser(@HeaderParam("authorization") String auth, String requestJson) {

        JsonObject request = gson.fromJson(requestJson, JsonObject.class);
        String followHandle = request.get("handle").getAsString();
        int followId = Handle.getUserId(followHandle); //todo: change to UserService

        String token;
        try {
            token = HTTPRequestUtil.getJWT(auth);
        } catch (BadDataException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Invalid token
        }

        JWTSecret jwt = new JWTSecret();
        String handle = jwt.getClaim(token, "handle").asString();
        int uid = jwt.getClaim(token, "uid").asInt();

        if(handle.equalsIgnoreCase(followHandle)) {
            return Response.status(Response.Status.BAD_REQUEST).build(); // Is themself
        }

        try {
            RelationshipService rs = RelationshipService.getInstance();
            RelationshipType status = rs.getRelationship(uid, followId);

            if(rs.setFollowing(uid, followId)) {
                return Response.status(Response.Status.BAD_REQUEST).build(); // Already following
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().build();
    }
}
