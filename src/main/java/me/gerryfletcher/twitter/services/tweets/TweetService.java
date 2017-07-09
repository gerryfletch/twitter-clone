package me.gerryfletcher.twitter.services.tweets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.DAO.tweets.TweetDao;
import me.gerryfletcher.twitter.controllers.HashId;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.services.user.UserService;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweetService {

    private static TweetService instance = null;
    private final UserService userService;
    private TweetDao tweetDao;
    private HashId hashId;

    public static TweetService getInstance() {
        if (instance == null) {
            instance = new TweetService();
        }
        return instance;
    }

    private TweetService() {
        tweetDao = new TweetDao();
        userService = UserService.getInstance();
        hashId = new HashId();
    }

    /**
     * Posts the tweet to the DB, returning the permalink hashid.
     * @param tweet The tweet body
     * @param uid   The user posting
     * @return  The <b>String</b> permalink (hashid)
     * @throws ApplicationException In DB failiure.
     */
    public String postTweet(String tweet, int uid) throws ApplicationException {
        try {
            int tweetId = tweetDao.postTweet(tweet, uid);
            return hashId.encode(tweetId);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem creating New Tweet in TweetDao", e);
        }
    }

    public JsonObject getTweet(String tweetHash) throws UserNotExistsException, ApplicationException {
        try {
            long tweetId = hashId.decode(tweetHash)[0];
            JsonObject tweet = tweetDao.getTweet(tweetId);
            JsonArray entities = getEntities(tweet.get("body").getAsString());
            tweet.add("entities", entities);
            return tweet;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem getting Tweet from TweetDao.", e);
        }
    }

    /**
     * Gets the entities. Hash tags and user mentions.
     * @param tweet The tweet body.
     * @return  A JsonObject containing the entities.
     * @throws ApplicationException In DB failiure.
     */
    private JsonArray getEntities(String tweet) throws ApplicationException {
        Pattern pattern = Pattern.compile("@([A-Za-z1-9_\\-]+)");
        Matcher matcher = pattern.matcher(tweet);

        JsonArray entities = new JsonArray();

        while (matcher.find()) {
            String handle = matcher.group(1);
            int start = matcher.start(1);
            int end = matcher.end(1);

            if (! this.userService.doesHandleExist(handle)) {
                continue;
            }

            JsonObject user = new JsonObject();
            user.addProperty("handle", handle);

            JsonArray indices = new JsonArray();
            indices.add(start);
            indices.add(end);

            user.add("indices", indices);
            entities.add(user);
        }

        System.out.println(entities);

        return entities;
    }

}
