package me.gerryfletcher.twitter.DAO.tweets;

import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.DAO.UtilDao;
import me.gerryfletcher.twitter.DAO.profile.UserDao;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TweetDao extends UtilDao {

    private UserDao userDao;

    public TweetDao() {
        userDao = new UserDao();
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

    private final String GET_TWEET_QUERY = "SELECT * FROM tweets WHERE id=?";

    /**
     * Gets a tweets DB info.
     * @param tweetId   The numerical ID of the tweet.
     * @return  The tweets information.
     * @throws SQLException In DB failiure.
     */
    public JsonObject getTweet(long tweetId) throws SQLException, UserNotExistsException {
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(GET_TWEET_QUERY)) {

            stmt.setLong(1, tweetId);

            ResultSet rs = stmt.executeQuery();

            if (! rs.next()) {
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
