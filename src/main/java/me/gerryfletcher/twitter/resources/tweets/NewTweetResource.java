package me.gerryfletcher.twitter.resources.tweets;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/tweet")
public class NewTweetResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * Each tweet request has:
     *  - created_at Date Time
     *  - entities: hashtags, urls, user_metions
     *  - body
     *
     *  The response includes:
     *  - Permalink: HashId library
     *  - the full tweet
     *      - created at
     *      - favorited
     *      - enties
     *          - user mentions
     *      - body
     *      - display name
     *      - handle
     * @param json  The request
     * @return  The created tweet
     */
    @Path("/new")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newTweet(String json) {

        JsonObject request = gson.fromJson(json, JsonObject.class);



        return Response.ok().build();
    }
}
