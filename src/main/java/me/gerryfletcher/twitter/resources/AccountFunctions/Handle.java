package me.gerryfletcher.twitter.resources.AccountFunctions;

import me.gerryfletcher.twitter.sqlite.SQLUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Gerry on 10/06/2017.
 */
public class Handle {

    /* Usernames must:
        1) Match characters and symbols in the list: a-z, 0-9, underscore, hyphen
        2) Be at least 3 characters long, and a maximum of 15
    */
    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$";

    private static Pattern pattern;
    private static Matcher matcher;

    public static boolean isHandleValid(String username) {
        pattern = Pattern.compile(USERNAME_PATTERN);
        matcher = pattern.matcher(username);

        if(! matcher.matches()) {
            return false;
        }

        return true;
    }

    /*
        Query the database for just the handle.
     */
    public static boolean doesHandleExist(String handle) {
        String query = "SELECT count(*) AS count FROM users WHERE handle= ?";
        try (Connection conn = SQLUtils.connect();
             PreparedStatement st = conn.prepareStatement(query)){
            st.setString(1, handle);
            st.execute();
            ResultSet resultSet = st.getResultSet();
            if(resultSet.getInt("count") == 1) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return false;
    }

    /*
        Get the corresponding integer ID of a String handle
        Returns -1 if failed.
     */
    public static int getUserId(String handle) {
        String query = "SELECT id FROM users WHERE handle= ?";
        try (Connection conn = SQLUtils.connect();
             PreparedStatement st = conn.prepareStatement(query)){
            st.setString(1, handle);
            st.execute();
            ResultSet resultSet = st.getResultSet();
            return resultSet.getInt("id");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return -1;
    }

    /*
        Returns the role of a user.
     */
    public static String getUserRole(String handle) {
        String query = "SELECT role FROM users WHERE handle= ?";
        try (Connection conn = SQLUtils.connect();
             PreparedStatement st = conn.prepareStatement(query)){
            st.setString(1, handle);
            st.execute();
            ResultSet resultSet = st.getResultSet();
            return resultSet.getString("role");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
