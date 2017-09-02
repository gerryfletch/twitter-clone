package me.gerryfletcher.twitter.resources.tweets.actions;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.gerryfletcher.twitter.controllers.security.HTTPRequestUtil;
import me.gerryfletcher.twitter.controllers.security.JWTSecret;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.services.tweets.LikeService;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("tweet/{hashid}")
public class LikeResource {

    private Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    private JWTSecret jwt = new JWTSecret();

    @Path("like")
    @POST
    @RolesAllowed("user")
    public Response likeTweet(@HeaderParam("authorization") String auth, @PathParam("hashid") String hashid) {
        String token = HTTPRequestUtil.getJWT(auth);
        int uid = jwt.getClaim(token, "uid").asInt();

        LikeService likeService = LikeService.getInstance();
        try {
            likeService.setLike(uid, hashid);
            return Response.ok().build();
        } catch (ApplicationException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Path("unlike")
    @DELETE
    @RolesAllowed("user")
    public Response unlikeTweet(@HeaderParam("authorization") String auth, @PathParam("hashid") String hashid) {
        String token = HTTPRequestUtil.getJWT(auth);
        int uid = jwt.getClaim(token, "uid").asInt();

        LikeService likeService = LikeService.getInstance();
        try {
            likeService.unsetLike(uid, hashid);
            return Response.ok().build();
        } catch (ApplicationException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
