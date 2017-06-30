package me.gerryfletcher.twitter.DAO;

import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariDataSource;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao extends UtilDao {

    public UserDao() {}

    private final String GET_HANDLE_QUERY = "SELECT handle FROM users WHERE id=?";

    /**
     * Gets a users handle from ID.
     *
     * @param uid The users ID.
     * @return The users String handle.
     * @throws UserNotExistsException If the user does not exist.
     * @throws SQLException           In DB failiure.
     */
    public String getHandle(int uid) throws UserNotExistsException, SQLException {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_HANDLE_QUERY)) {

            stmt.setInt(1, uid);
            ResultSet result = stmt.executeQuery();

            if (!result.next()) {
                throw new UserNotExistsException();
            }

            return result.getString(1);
        }
    }

    private final String GET_UID_QUERY = "SELECT id FROM users WHERE handle=?";

    public int getUID(String handle) throws UserNotExistsException, SQLException {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_UID_QUERY)) {

            stmt.setString(1, handle);
            ResultSet result = stmt.executeQuery();

            if(! result.next()) {
                throw new UserNotExistsException();
            }

            return result.getInt(1);
        }
    }

    private final String GET_PROFILE_QUERY = "SELECT handle, display_name, profile_picture, bio\n" +
            "FROM users\n" +
            "LEFT JOIN account_details\n" +
            "ON users.id = account_details.id\n" +
            "WHERE users.id = ?";

    /**
     * Returns a JSON representation of a users profile.
     * - UID
     * - Handle
     * - Display Name
     * - Profile Picture
     * - Bio
     * - Statistics
     *  - Number of Tweets
     *  - Number of Followers
     *  - Number of Following
     *
     * @param uid The users ID
     * @return JsonObject containing profile.
     * @throws UserNotExistsException If the user does not exist.
     * @throws SQLException           In DB failiure.
     */
    public JsonObject getProfile(int uid) throws UserNotExistsException, SQLException {
        JsonObject profile = new JsonObject();

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_PROFILE_QUERY)) {

            stmt.setInt(1, uid);
            ResultSet user = stmt.executeQuery();

            if (!user.next()) {
                throw new UserNotExistsException();
            }

            profile.addProperty("uid", uid);
            profile.addProperty("handle", user.getString("handle"));
            profile.addProperty("display_name", user.getString("display_name"));
            profile.addProperty("profile_picture", user.getString("profile_picture"));
            profile.addProperty("bio", user.getString("bio"));

            profile.add("statistics", getStatistics(uid));

            return profile;
        }
    }

    /**
     * Forms a JSON object of a users statistics.
     * - Number of Tweets
     * - Number of Followers
     * - Number of Following
     *
     * @param uid A users ID.
     * @return A JsonObject containing a users statistics.
     * @throws SQLException In DB failiure.
     */
    public JsonObject getStatistics(int uid) throws SQLException {
        JsonObject statistics = new JsonObject();

        statistics.addProperty("number_of_tweets", selectCountByIdentifier("tweets", "id", uid));
        statistics.addProperty("number_of_followers", selectCountByIdentifier("followers", "following_id", uid));
        statistics.addProperty("number_of_following", selectCountByIdentifier("followers", "follower_id", uid));

        return statistics;
    }

    /**
     * Checks if an email exists in the users table.
     *
     * @param email The users email.
     * @return True or False.
     * @throws SQLException In DB failiure.
     */
    public boolean doesEmailExist(String email) throws SQLException {
        return doesRecordExist("users", "email", email);
    }

    public boolean doesHandleExist(String handle) throws SQLException {
        return doesRecordExist("users", "handle", handle);
    }

}
