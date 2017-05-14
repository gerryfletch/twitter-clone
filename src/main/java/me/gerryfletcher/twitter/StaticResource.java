package me.gerryfletcher.twitter;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 * Created by Gerry on 14-May-17.
 */
@Resource
@Path("/")
public class StaticResource {
    @GET
    @Path("{path:.*}")
    public Response get(@PathParam("path") String inPath) {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}