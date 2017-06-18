package me.gerryfletcher.twitter.resources;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.resources.AccountFunctions.Handle;
import me.gerryfletcher.twitter.sqlite.SQLUtils;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Gerry on 09/06/2017.
 */
@Path("user")
public class User {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    @Path("all")
    @RolesAllowed("Admin")
    @GET
    public String getAllUsers() {
        SQLUtils.selectAllFromUsers();
        return "Got all users.";
    }

    @Path("{handle}")
    @PermitAll
    @GET
    public Response getUser(@PathParam("handle") String handle) {

        if(! Handle.doesHandleExist(handle)) {
            return Response.status(404).build();
        }

        return Response.ok().build();
    }
}
