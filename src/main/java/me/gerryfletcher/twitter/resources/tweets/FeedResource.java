package me.gerryfletcher.twitter.resources.tweets;

import com.google.gson.*;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.services.tweets.TweetService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("feed")
public class FeedResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    /**
     * Gets a Tweet feed from a users point of view, with a specified number of tweets.
     * If no params are supplied, it defaults to 10 posts at offset 0.
     * <p>
     * Example: feed/?limit=100&page=1
     *
     * @param auth  The authorization header.
     * @param page  The offset of results - page 1, page 2 etc.
     * @param limit The number of tweets to return.
     * @return JSON representation of tweets.
     */
    @GET
    @RolesAllowed("user")
    public Response getUserFeed(@HeaderParam("authorization") String auth, @QueryParam("limit") Integer limit, @QueryParam("page") Integer page, @QueryParam("handle") String handle) {
        String token = HTTPRequestUtil.getJWT(auth);
        JWTSecret jwt = new JWTSecret();
        int uid = jwt.getClaim(token, "uid").asInt();

        System.out.println("Getting " + limit + " posts on page " + page);

        if (page == null) {
            page = 0;
        }

        page = page * 10;

        if (limit == null) {
            limit = 10;
        }

        TweetService tweetService = TweetService.getInstance();

        try {

            JsonArray tweets;

            if (handle == null) {
                tweets = tweetService.getUserFeed(uid, limit, page);
            } else {
                tweets = tweetService.getUserFeed(handle, limit, page);
            }

            JsonObject res = new JsonObject();
            res.add("tweets", tweets);

            return Response.ok().entity(gson.toJson(tweets)).build();

        } catch (ApplicationException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
