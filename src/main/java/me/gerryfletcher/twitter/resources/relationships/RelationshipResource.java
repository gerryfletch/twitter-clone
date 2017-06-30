package me.gerryfletcher.twitter.resources.relationships;

import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.services.RelationshipService;
import me.gerryfletcher.twitter.services.UserService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;


@Path("/relationship")
public class RelationshipResource {

    @GET
    @Path("{handle}/{handleTwo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRelationship(@PathParam("handle") String handle, @PathParam("handleTwo") String handleTwo) {
        RelationshipService relationshipService = RelationshipService.getInstance();
        return Response.ok().build();
    }

}
