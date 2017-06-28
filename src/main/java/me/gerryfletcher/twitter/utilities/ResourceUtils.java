package me.gerryfletcher.twitter.utilities;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.ws.rs.core.Response;

public class ResourceUtils {

    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * Returns a Response of status 401 Unauthorized with a String error.
     *
     * @param error A user-friendly error.
     * @return 401 Unauthorized response with JSON error in body.
     **/
    public static Response failed(String error) {
        System.out.println("ERROR: " + error);
        JsonObject returnFail = new JsonObject();
        returnFail.addProperty("error", error);
        return Response.status(401).entity(gson.toJson(returnFail)).build();
    }

    /**
     * Returns a Response with a chosen status and a String error.
     *
     * @param error  A user-friendly error.
     * @param status A HTTP status code.
     * @return  The Response object with a JSON error in the body.
     */
    public static Response failed(String error, int status) {
        System.out.println("ERROR: " + error);
        JsonObject returnFail = new JsonObject();
        returnFail.addProperty("error", error);
        return Response.status(status).entity(gson.toJson(returnFail)).build();
    }
}
