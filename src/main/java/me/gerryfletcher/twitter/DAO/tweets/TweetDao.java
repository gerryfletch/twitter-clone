package me.gerryfletcher.twitter.DAO.tweets;

import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.DAO.UtilDao;
import me.gerryfletcher.twitter.DAO.profile.UserDao;
import me.gerryfletcher.twitter.controllers.HashId;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TweetDao extends UtilDao {

    private UserDao userDao;
    private LikeDao likeDao;

    public TweetDao() {
        userDao = new UserDao();
        likeDao = new LikeDao();
    }

    private final String POST_TWEET_QUERY = "INSERT INTO tweets(author_id, body, creation_date) VALUES (?,?,DATETIME('now', 'localtime'))";

    public int postTweet(String tweet, int uid) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(POST_TWEET_QUERY)) {

            stmt.setInt(1, uid);
            stmt.setString(2, tweet);

            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();

            if (!generatedKeys.next()) {
                throw new SQLException();
            }

            return generatedKeys.getInt(1);
        }
    }

    private final String GET_TWEETS_FOR_USER_QUERY = "SELECT tweets.id, tweets.author_id, tweets.body, tweets.creation_date, users.display_name, users.handle, account_details.profile_picture\n" +
            "FROM tweets\n" +
            "INNER JOIN followers\n" +
            "ON tweets.author_id = followers.following_id\n" +
            "INNER JOIN users\n" +
            "ON users.id = tweets.author_id\n" +
            "LEFT JOIN account_details\n" +
            "ON account_details.id = tweets.author_id\n" +
            "WHERE followers.follower_id = ? \n" +
            "OR tweets.author_id = ? \n" +
            "ORDER BY datetime(tweets.creation_date) DESC \n" +
            "LIMIT ? OFFSET ?";

    /**
     * Gets tweets from users someone follows, including themself.
     *
     * @param uid         The users ID.
     * @param numOfTweets The number of tweets to select. Typically 10.
     * @param fromRow     How far into the table to skip rows.
     * @return A list of tweets in JSON format.
     */
    public List<JsonObject> getUserFeed(int uid, int numOfTweets, int fromRow) throws SQLException {

        List<JsonObject> tweets = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_TWEETS_FOR_USER_QUERY)) {

            stmt.setInt(1, uid);
            stmt.setInt(2, uid);
            stmt.setInt(3, numOfTweets);
            stmt.setInt(4, fromRow);

            ResultSet rs = stmt.executeQuery();
            HashId hashId = new HashId();

            return getTweetsFromResultSet(rs, hashId, uid);
        }
    }

    private final String GET_TWEETS_FOR_PROFILE_QUERY = "SELECT tweets.id, tweets.author_id, tweets.body, tweets.creation_date, users.display_name, users.handle, account_details.profile_picture\n" +
            "FROM tweets\n" +
            "INNER JOIN users\n" +
            "ON users.id = tweets.author_id\n" +
            "LEFT JOIN account_details\n" +
            "ON account_details.id = tweets.author_id\n" +
            "WHERE users.handle = ?\n" +
            "ORDER BY datetime(tweets.creation_date) DESC \n" +
            "LIMIT ? OFFSET ?";

    public List<JsonObject> getUserProfile(int callerId, String handle, int numOfTweets, int fromRow) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_TWEETS_FOR_PROFILE_QUERY)) {

            stmt.setString(1, handle);
            stmt.setInt(2, numOfTweets);
            stmt.setInt(3, fromRow);

            ResultSet rs = stmt.executeQuery();
            HashId hashId = new HashId();

            return getTweetsFromResultSet(rs, hashId, callerId);
        }
    }

    private List<JsonObject> getTweetsFromResultSet(ResultSet rs, HashId hashId, int callerId) throws SQLException {
        List<JsonObject> tweets = new ArrayList<>();

        while (rs.next()) {
            JsonObject tweet = new JsonObject();

            tweet.addProperty("temp_caller_id", callerId);
            tweet.addProperty("tweet_id", rs.getInt("id"));

            int authorID = rs.getInt("author_id");
            int tweetID = rs.getInt("id");

            tweet.addProperty("author_id", authorID);
            tweet.addProperty("body", rs.getString("body"));
            tweet.addProperty("timestamp", rs.getString("creation_date"));

            boolean isLiked = this.likeDao.isTweetLiked(callerId, tweetID);
            tweet.addProperty("liked", isLiked);

            int numOfLikes = this.likeDao.getNumberOfLikes(tweetID);
            tweet.addProperty("like_count", numOfLikes);


            String hashid = hashId.encode(tweetID);
            tweet.addProperty("hash_id", hashid);

            JsonObject profile = new JsonObject();
            profile.addProperty("display_name", rs.getString("display_name"));
            profile.addProperty("handle", rs.getString("handle"));
            profile.addProperty("profile_picture", rs.getString("profile_picture"));

            tweet.add("profile", profile);

            tweets.add(tweet);
        }

        return tweets;
    }

    private final String GET_TWEET_QUERY = "SELECT * FROM tweets WHERE id=?";

    /**
     * Gets a tweets DB info.
     *
     * @param tweetId The numerical ID of the tweet.
     * @return The tweets information.
     * @throws SQLException In DB failiure.
     */
    public JsonObject getTweet(long tweetId) throws SQLException, UserNotExistsException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_TWEET_QUERY)) {

            stmt.setLong(1, tweetId);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return null;
            }

            int authorId = rs.getInt("author_id");

            JsonObject tweet = new JsonObject();
            tweet.addProperty("author_id", authorId);
            tweet.addProperty("timestamp", rs.getString("creation_date"));

            JsonObject userProfile = userDao.getProfile(authorId);
            userProfile.remove("bio");
            userProfile.remove("uid");
            userProfile.remove("statistics");

            tweet.add("profile", userProfile);
            tweet.addProperty("body", rs.getString("body"));

            return tweet;
        }
    }

}
