package me.gerryfletcher.twitter.DAO.tweets;

import me.gerryfletcher.twitter.DAO.UtilDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LikeDao extends UtilDao{

    private final String SET_LIKE_QUERY = "INSERT OR IGNORE INTO likes(user_id, tweet_id, liked_time) VALUES(?, ?, DATETIME('now', 'localtime'));";
    public void setLike(int uid, long tweetid) throws SQLException {
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(SET_LIKE_QUERY)) {

            stmt.setInt(1, uid);
            stmt.setLong(2, tweetid);

            stmt.executeUpdate();
        }
    }

    private final String UNSET_LIKE_QUERY = "DELETE FROM likes WHERE user_id = ? AND tweet_id = ?";
    public void unsetLike(int uid, long tweetid) throws SQLException {
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(UNSET_LIKE_QUERY)) {

            stmt.setInt(1, uid);
            stmt.setLong(2, tweetid);

            stmt.executeUpdate();
        }
    }

    private final String NUMBER_OF_LIKES = "SELECT COUNT(*) FROM likes WHERE likes.tweet_id = ?";
    public int getNumberOfLikes(int tweetId) throws SQLException {
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(NUMBER_OF_LIKES)) {

            stmt.setInt(1, tweetId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    private final String IS_TWEET_LIKED = "SELECT EXISTS(SELECT 1 FROM likes WHERE user_id = ? AND tweet_id = ? LIMIT 1);";
    public boolean isTweetLiked(int userId, int tweetId) throws SQLException {
        try(Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(IS_TWEET_LIKED)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, tweetId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int i = rs.getInt(1);
                return (i == 1);
            }

            return false;
        }
    }
}
