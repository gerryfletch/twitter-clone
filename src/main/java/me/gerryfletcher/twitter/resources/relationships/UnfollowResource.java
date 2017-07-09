package me.gerryfletcher.twitter.resources.relationships;

import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.models.RelationshipType;
import me.gerryfletcher.twitter.services.user.RelationshipService;
import me.gerryfletcher.twitter.utilities.ResourceUtils;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;


@Path("/user/{handle}/unfollow")
public class UnfollowResource {

    @POST
    public Response unfollowUser(@HeaderParam("authorization") String auth, @PathParam("handle") String handleToUnfollow) {

        if(! Handle.isHandleValid(handleToUnfollow)) {
            return ResourceUtils.unauthorized("Invalid handle.", Response.Status.BAD_REQUEST);
        }

        String token = HTTPRequestUtil.getJWT(auth);
        JWTSecret jwt = new JWTSecret();
        String userHandle = jwt.getClaim(token, "handle").asString();

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

        System.out.println("User unfollowed.");
        return Response.ok().build();
    }

}
