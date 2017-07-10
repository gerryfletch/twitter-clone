package me.gerryfletcher.twitter.resources.tweets;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.Tweet;
import me.gerryfletcher.twitter.services.tweets.TweetService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
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
     * @return  The created tweet relative to hash ID.
     */
    @Path("/new")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    public Response newTweet(@HeaderParam("authorization") String auth, String json) {
        JsonObject request = gson.fromJson(json, JsonObject.class);
        String body = request.get("body").getAsString();

        if (!Tweet.isTweetValid(body)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String token = HTTPRequestUtil.getJWT(auth);
        JWTSecret jwtSecret = new JWTSecret();

        int uid = jwtSecret.getClaim(token, "uid").asInt();

        TweetService tweetService = TweetService.getInstance();
        try {
            String tweetHash = tweetService.postTweet(body, uid);
            JsonObject newTweet = tweetService.getTweet(tweetHash);
            System.out.println(newTweet);
            return Response.ok().entity(gson.toJson(newTweet)).build();

        } catch (ApplicationException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (UserNotExistsException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
