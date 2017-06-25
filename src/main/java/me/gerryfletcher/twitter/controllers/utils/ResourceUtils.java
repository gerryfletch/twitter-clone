package me.gerryfletcher.twitter.controllers.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.ws.rs.core.Response;

/**
 * Created by Gerry on 17/06/2017.
 */
public class ResourceUtils {

    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
        Returns a Response of status 401 Unauthorized with a String error.
        @param error A user-friendly error.
        @return 401 Unauthorized response with JSON error in body.
     **/
    public static Response failed(String error) {
        System.out.println("ERROR: " + error);
        JsonObject returnFail = new JsonObject();
        returnFail.addProperty("error", error);
        return Response.status(401).entity(gson.toJson(returnFail)).build();
    }

    /*

     */
    public static Response failed(String error, int status) {
        System.out.println("ERROR: " + error);
        JsonObject returnFail = new JsonObject();
        returnFail.addProperty("error", error);
        return Response.status(status).entity(gson.toJson(returnFail)).build();
    }
}
