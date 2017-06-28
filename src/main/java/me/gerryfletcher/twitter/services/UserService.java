package me.gerryfletcher.twitter.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;
import me.gerryfletcher.twitter.controllers.user.Handle;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Gerry on 27/06/2017.
 */
public class UserService {

    private static UserService instance = null;

    protected UserService() throws SQLException {
    }

    private Connection conn = SQLUtils.connect();

    /* Profile */
    private final String get_user_profile_SQL = "SELECT handle, display_name, profile_picture, bio\n" +
            "FROM users\n" +
            "LEFT JOIN account_details\n" +
            "ON users.id = account_details.id\n" +
            "WHERE users.id = ?";

    private final PreparedStatement get_user_profile = conn.prepareStatement(get_user_profile_SQL);

    /* User */
    private final String get_id_SQL = "SELECT id FROM users WHERE lower(handle) = ?";
    private final String get_handle_SQL = "SELECT handle FROM users WHERE id = ?";
    private final String get_display_name_SQL = "SELECT display_name FROM users WHERE id = ?";

    private final PreparedStatement get_id = conn.prepareStatement(get_id_SQL);
    private final PreparedStatement get_handle = conn.prepareStatement(get_handle_SQL);
    private final PreparedStatement get_display_name = conn.prepareStatement(get_display_name_SQL);

    /* Account Details */
    private final String get_profile_picture_SQL = "SELECT profile_picture FROM account_details WHERE id = ?";
    private final String get_bio_SQL = "SELECT bio FROM account_details WHERE id = ?";

    private final PreparedStatement get_profile_picture = conn.prepareStatement(get_profile_picture_SQL);
    private final PreparedStatement get_bio = conn.prepareStatement(get_bio_SQL);

    /* Statistics */
    private final String get_number_of_tweets_SQL = "SELECT COUNT(*) FROM tweets WHERE id = ?";
    private final String get_number_of_followers_SQL = "SELECT COUNT(*) FROM followers WHERE following_id = ?";
    private final String get_number_of_following_SQL = "SELECT COUNT(*) FROM followers WHERE follower_id = ?";

    private final PreparedStatement get_number_of_tweets = conn.prepareStatement(get_number_of_tweets_SQL);
    private final PreparedStatement get_number_of_followers = conn.prepareStatement(get_number_of_followers_SQL);
    private final PreparedStatement get_number_of_following = conn.prepareStatement(get_number_of_following_SQL);

    public static UserService getInstance() throws SQLException {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public int getUserId(String handle) throws SQLException {
        handle = handle.toLowerCase();
        get_id.setString(1, handle);
        ResultSet result = get_id.executeQuery();
        if (result.next()) {
            return result.getInt("id");
        }
        throw new UserNotExistsException("User " + handle + " does not exist.");
    }

    public String getHandle(int uid) throws SQLException {
        ResultSet result = get_handle.executeQuery();
        if (result.next()) {
            return result.getString("handle");
        }
        throw new UserNotExistsException("User " + uid + " does not exist.");
    }

    private final String does_id_exist_SQL = "SELECT EXISTS(SELECT 1 FROM users WHERE id=? LIMIT 1)";
    private final PreparedStatement does_id_exist = conn.prepareStatement(does_id_exist_SQL);
    public boolean doesIdExist(int uid) throws SQLException {
        does_id_exist.setInt(1, uid);
        ResultSet result = does_id_exist.executeQuery();
        result.next();

        return result.getInt(1) == 1;
    }

    private final String does_handle_exist_SQL = "SELECT EXISTS(SELECT 1 FROM users WHERE lower(handle)=? LIMIT 1)";
    private final PreparedStatement does_handle_exist = conn.prepareStatement(does_handle_exist_SQL);
    public boolean doesHandleExist(String handle) throws SQLException {
        handle = handle.toLowerCase();
        if(! Handle.isHandleValid(handle))
            return false;

        does_handle_exist.setString(1, handle);
        ResultSet result = does_handle_exist.executeQuery();
        result.next();

        return result.getInt(1) == 1;
    }

    public int getNumberOfFollowing(int uid) throws SQLException {
        get_number_of_following.setInt(1, uid);
        ResultSet num_following = get_number_of_following.executeQuery();
        if (num_following.next()) {
            return num_following.getInt(1);
        }
        throw new UserNotExistsException("User " + uid + " does not exist");
    }

    public int getNumberOfFollowers(int uid) throws SQLException {
        get_number_of_followers.setInt(1, uid);
        ResultSet num_followers = get_number_of_followers.executeQuery();
        if (num_followers.next()) {
            return num_followers.getInt(1);
        }
        throw new UserNotExistsException("User " + uid + " does not exist.");
    }

    public int getNumberOfTweets(int uid) throws SQLException {
        get_number_of_tweets.setInt(1, uid);
        ResultSet num_tweets = get_number_of_tweets.executeQuery();
        if (num_tweets.next()) {
            return num_tweets.getInt(1);
        }
        throw new UserNotExistsException("User " + uid + " does not exist.");
    }

    public User getUser(int uid) throws SQLException {
        Gson gson = new Gson();
        return gson.fromJson(getJsonProfile(uid), User.class);
    }

    /**
     * Returns a JSON representation of a users profile.
     * @param uid   The users ID
     * @return  JsonObject
     * @throws SQLException extends UserNotExistsException, will report back if the user does not exist.
     */
    public JsonObject getJsonProfile(int uid) throws SQLException {

        JsonObject profile = new JsonObject();

        get_user_profile.setInt(1, uid);
        ResultSet user = get_user_profile.executeQuery();

        if (user.next()) {
            profile.addProperty("uid", uid);
            profile.addProperty("handle", user.getString("handle"));
            profile.addProperty("display_name", user.getString("display_name"));
            profile.addProperty("profile_picture", user.getString("profile_picture"));
            profile.addProperty("bio", user.getString("bio"));
        } else {
            throw new UserNotExistsException("User " + uid + " does not exist.");
        }

        profile.add("statistics", getProfileStatistics(uid));

        return profile;

    }

    private JsonObject getProfileStatistics(int uid) throws SQLException {
        JsonObject statistics = new JsonObject();
        statistics.addProperty("number_of_tweets", getNumberOfTweets(uid));
        statistics.addProperty("number_of_followers", getNumberOfFollowers(uid));
        statistics.addProperty("number_of_following", getNumberOfFollowing(uid));

        return statistics;
    }


}
