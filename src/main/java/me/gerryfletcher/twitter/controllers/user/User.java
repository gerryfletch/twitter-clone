package me.gerryfletcher.twitter.controllers.user;

import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserSqlException;

import javax.xml.transform.Result;
import java.sql.*;

/**
 * Created by Gerry on 24/06/2017.
 */
public class User implements AutoCloseable {

    private Connection conn = SQLUtils.connect();

    private int uid;
    private String handle;


    /*
        If a User is created with a handle, the UID is looked up and stored.
        The UID is used for all other methods.
     */
    public User(String handle) throws BadDataException {
        if (!Handle.isHandleValid(handle)) {
            throw new BadDataException("Invalid Username.");
        }

        this.handle = handle;
        try {
            this.uid = getUserId(handle);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BadDataException("Cannot find ID of user.");
        }
    }

    /*
        If a User is created with a UID, it is checked against the DB and
        stored.
        The UID is used for all other methods.
     */
    public User(int uid) throws BadDataException {
        if (uid < 1)
            throw new BadDataException("Invalid User ID.");
        else if (!checkIfIdExists(uid))
            throw new BadDataException("User ID does not exist.");

        this.uid = uid;
    }

    /*
        The class extends AutoCloseable so that the class can be used
        inside a try catch block, and the DB connection will be closed
        when the user is done with.
     */
    @Override
    public void close() throws SQLException {
        this.conn.close();
    }

    private int getUserId(String handle) throws SQLException {
        String sql = "SELECT id FROM users WHERE handle = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, handle);
            ResultSet rs = st.executeQuery();

            rs.next();
            return rs.getInt("id");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Problem getting the user ID of this handle.");
        }
    }

    private boolean checkIfIdExists(int id) {

        String sql = "SELECT TOP 1 id FROM users WHERE id = ?";

        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public int getId() {
        return this.uid;
    }

    public String getHandle() throws SQLException {

        if (this.handle != null) {
            return this.handle;
        }

        String sql = "SELECT handle FROM users WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, this.uid);
            ResultSet rs = st.executeQuery();
            rs.next();

            this.handle = rs.getString("handle");
            return this.handle;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Problem getting user handle.");
        }
    }

    public String getDisplayName() throws SQLException {
        String sql = "SELECT display_name FROM users WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, this.uid);
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getString("display_name");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Problem getting Display Name for user " + this.uid);
        }
    }

    /*
        Returns the PATH to the users profile picture.
        Uses rs.next() to check whether or not the user has a profile picture.
        @return path the path to the image.
     */
    public String getProfilePicture() {
        String sql = "SELECT profile_picture FROM account_details WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, this.uid);
            ResultSet rs = st.executeQuery();

            // The user has a profile picture
            if (rs.next()) {
                return rs.getString(1);
            } else {
                return null;
            }

        } catch (SQLException e) {
            return null;
        }
    }

    /*
        User Statistics
     */

    public int getNumberOfTweets() throws SQLException {
        String sql = "SELECT COUNT(*) FROM tweets WHERE id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, this.uid);
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Problem getting # of tweets from user " + this.uid);
        }
    }

    public int getNumberOfFollowers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM followers WHERE following_id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, this.uid);
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new SQLException("Problem getting number of followers from user " + this.uid);
        }
    }

    public int getNumberOfFollowing() throws SQLException {
        String sql = "SELECT COUNT(*) FROM followers WHERE follower_id = ?";
        try (PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, this.uid);
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new SQLException("Problem getting number of following from user " + this.uid);
        }
    }

    /*
        Returns the users public profile in JSON.
        Included:
            - UID
            - Handle
            - Display Name
            - Number of Tweets
            - Number of Followers
            - Number of Following
     */
    public JsonObject getProfile() throws SQLException {
        JsonObject profile = new JsonObject();
        try {
            profile.addProperty("uid", this.uid);
            profile.addProperty("handle", getHandle());
            profile.addProperty("display_name", getDisplayName());
            profile.addProperty("profile_picture", getProfilePicture());
            profile.addProperty("number_of_tweets", getNumberOfTweets());
            profile.addProperty("number_of_followers", getNumberOfFollowers());
            profile.addProperty("number_of_following", getNumberOfFollowing());
        } catch (SQLException e) {
            throw new SQLException("Problem getting user data. " + e.getMessage());
        }

        return profile;
    }
}
