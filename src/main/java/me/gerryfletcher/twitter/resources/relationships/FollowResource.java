package me.gerryfletcher.twitter.resources.relationships;

import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.models.RelationshipType;
import me.gerryfletcher.twitter.services.RelationshipService;
import me.gerryfletcher.twitter.utilities.ResourceUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;


@Path("/user/{handle}/follow")
public class FollowResource {

    /**
     * Attempts to follow a user. It is from the point of view of the client that sends the request.
     * @param auth  The Authrorization header.
     * @param handleToFollow    The handle to try to follow.
     * @return  200 OK, 400 Bad Request, 500 Server Error.
     */
    @POST
    public Response followUser(@HeaderParam("authorization") String auth, @PathParam("handle") String handleToFollow) {

        if(! Handle.isHandleValid(handleToFollow)) {
            return ResourceUtils.unauthorized("Invalid handle.", Response.Status.BAD_REQUEST); // Invalid Handle
        }

        String token = HTTPRequestUtil.getJWT(auth);
        JWTSecret jwt = new JWTSecret();
        String userHandle = jwt.getClaim(token, "handle").asString();

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

        System.out.println("User followed.");
        return Response.ok().build();
    }

}
