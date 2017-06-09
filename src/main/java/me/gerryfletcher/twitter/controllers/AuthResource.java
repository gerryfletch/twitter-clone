package me.gerryfletcher.twitter.controllers;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Gerry on 07/06/2017.
 */
@Path("/login")
public class AuthResource {
    @RolesAllowed("ADMIN")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getData() {
        return "Welcome to the lair";
    }
}
