package me.gerryfletcher.twitter.resources.relationships;

import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.models.RelationshipType;
import me.gerryfletcher.twitter.services.RelationshipService;
import me.gerryfletcher.twitter.utilities.ResourceUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/user/{handle}")
public class FollowResource {

    @POST
    @Path("/follow")
    public Response followUser(@HeaderParam("authorization") String auth, @PathParam("handle") String handleToFollow) {

        if(! Handle.isHandleValid(handleToFollow)) {
            return ResourceUtils.unauthorized("Invalid handle.", Response.Status.BAD_REQUEST); // Invalid Handle
        }

        String userHandle;
        try {
            String token = HTTPRequestUtil.getJWT(auth);
            JWTSecret jwt = new JWTSecret();
            userHandle = jwt.getClaim(token, "handle").asString();
        } catch (BadDataException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Invalid token
        }

        if (userHandle.equalsIgnoreCase(handleToFollow)) {
            return Response.status(Response.Status.BAD_REQUEST).build(); // Is themself
        }

        try {
            RelationshipService rs = RelationshipService.getInstance();
            RelationshipType status = rs.getRelationship(userHandle, handleToFollow);

            if(status == RelationshipType.FOLLOWING || status == RelationshipType.MUTUALS) {
                return ResourceUtils.unauthorized("You're already following this user.", Response.Status.BAD_REQUEST); // Already following
            }

            rs.setFollowing(userHandle, handleToFollow);

        } catch (ApplicationException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserNotExistsException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }

    @POST
    @Path("/unfollow")
    public Response unfollowUser(@HeaderParam("authorization") String auth, @PathParam("handle") String handleToUnfollow) {

        if(! Handle.isHandleValid(handleToUnfollow)) {
            return ResourceUtils.unauthorized("Invalid handle.", Response.Status.BAD_REQUEST);
        }

        String userHandle;
        try {
            String token = HTTPRequestUtil.getJWT(auth);
            JWTSecret jwt = new JWTSecret();
            userHandle = jwt.getClaim(token, "handle").asString();
        } catch (BadDataException e) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Invalid token
        }

        if(userHandle.equalsIgnoreCase(handleToUnfollow)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        RelationshipService relationshipService = RelationshipService.getInstance();
        try {
            RelationshipType status = relationshipService.getRelationship(userHandle, handleToUnfollow);

            if(status == RelationshipType.NO_RELATIONSHIP) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            relationshipService.unsetFollowing(userHandle, handleToUnfollow);

        } catch (ApplicationException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserNotExistsException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.ok().build();
    }
}
