package me.gerryfletcher.twitter.resources.tweets;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.services.tweets.GetTagService;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/tweet")
public class GetTagResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    @Path("/get/tags")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getTags(@HeaderParam("authorization") String auth, String json) {

        String token = HTTPRequestUtil.getJWT(auth);
        int uid = new JWTSecret().getClaim(token, "uid").asInt();

        JsonObject request = gson.fromJson(json, JsonObject.class);
        String search = request.get("tag").getAsString();

        GetTagService tagService = GetTagService.getInstance();

        try {
            JsonObject response = tagService.getTags(search, uid);
            return Response.ok().entity(gson.toJson(response)).build();
        } catch (ApplicationException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
