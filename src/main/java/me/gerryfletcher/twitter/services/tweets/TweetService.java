package me.gerryfletcher.twitter.services.tweets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.DAO.tweets.TweetDao;
import me.gerryfletcher.twitter.controllers.HashId;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.services.user.UserService;

import java.sql.SQLException;
import java.util.List;
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

    public JsonArray getUserFeed(int uid, int numOfTweets, int fromRow) throws ApplicationException {
        try {
            List<JsonObject> tweets = tweetDao.getUserFeed(uid, numOfTweets, fromRow);
            return buildResponse(tweets);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem getting Feed in TweetDao", e);
        }
    }

    public JsonArray getUserFeed(String handle, int numOfTweets, int fromRow) throws ApplicationException {
        try {
            List<JsonObject> tweets = tweetDao.getUserFeed(handle, numOfTweets, fromRow);
            return buildResponse(tweets);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem getting Feed in TweetDao", e);
        }

    }

    private JsonArray buildResponse(List<JsonObject> tweets) throws ApplicationException {
        JsonArray response = new JsonArray();
        for (JsonObject tweet: tweets) {
            JsonObject entities = getEntities(tweet.get("body").getAsString());
            tweet.add("entities", entities);
            response.add(tweet);
        }
        return response;
    }

    public JsonObject getTweet(String tweetHash) throws UserNotExistsException, ApplicationException {
        try {
            long tweetId = hashId.decode(tweetHash)[0];
            JsonObject tweet = tweetDao.getTweet(tweetId);
            JsonObject entities = getEntities(tweet.get("body").getAsString());
            tweet.add("entities", entities);
            tweet.addProperty("hash_id", tweetHash);
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
    private JsonObject getEntities(String tweet) throws ApplicationException {
        JsonObject entities = new JsonObject();
        JsonArray userEntities = new JsonArray();

        // The User Mentions
        Pattern pattern = Pattern.compile("@([A-Za-z1-9_\\-]+)");
        Matcher matcher = pattern.matcher(tweet);

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
            userEntities.add(user);
        }

        entities.add("user_mentions", userEntities);

        // And now, the Hash Tags
        Pattern hashTagPattern = Pattern.compile("#([A-Za-z1-9]+)");
        matcher = hashTagPattern.matcher(tweet);

        JsonArray hashTagEntities = new JsonArray();

        while(matcher.find()) {
            String tag = matcher.group(1);
            int start = matcher.start(1);
            int end = matcher.end(1);

            JsonObject hashtag = new JsonObject();
            hashtag.addProperty("tag", tag);

            JsonArray indices = new JsonArray();
            indices.add(start);
            indices.add(end);

            hashtag.add("indices", indices);
            hashTagEntities.add(hashtag);
        }

        entities.add("hashtags", hashTagEntities);
        return entities;
    }

}
