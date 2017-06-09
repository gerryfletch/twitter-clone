package me.gerryfletcher.twitter.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Created by Gerry on 09/06/2017.
 */
@Path("user")
public class User {

    @Path("{handle}")
    @RolesAllowed("User")
    @GET
    public String getUser(@PathParam("handle") String username) {
        if(username.equals("gerry")) {
            return "Gerry is your friend!";
        } else {
            return "User not found.";
        }
    }
}
