package me.gerryfletcher.twitter.DAO.tweets;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.DAO.UtilDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GetTagDao extends UtilDao {
    public GetTagDao() {
    }

    private final String GET_TAG_FOLLOWING_QUERY = ""
            + "SELECT "
            + "  users.id, "
            + "  handle, "
            + "  display_name, "
            + "  account.profile_picture "
            + "FROM users users "
            + "  INNER JOIN followers "
            + "    ON users.id = followers.following_id "
            + "  LEFT JOIN account_details account "
            + "    ON users.id = account.id "
            + "WHERE users.handle LIKE ? "
            + "      AND followers.follower_id = ? "
            + "ORDER BY followers.creation_date "
            + "LIMIT 5";

    private final String GET_TAG_FOLLOWERS_QUERY = "SELECT "
            + "  users.id, "
            + "  users.handle, "
            + "  users.display_name, "
            + "  account.profile_picture "
            + "FROM users users "
            + "  INNER JOIN followers "
            + "    ON users.id = followers.follower_id "
            + "  LEFT JOIN account_details account "
            + "    ON users.id = account.id "
            + "WHERE users.handle LIKE ? "
            + "      AND followers.following_id= ? "
            + "ORDER BY creation_date";

    public JsonObject getTags(String search, int uid) throws SQLException {
        int maxCount = 5;
        int counter = 0;

        search = "%" + search + "%";

        JsonObject result = new JsonObject();
        JsonArray userArray = new JsonArray();

        List<Integer> keptIDs = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_TAG_FOLLOWING_QUERY)) {

            stmt.setString(1, search);
            stmt.setInt(2, uid);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int rsID = rs.getInt("id");
                if (keptIDs.contains(rsID)) {
                    continue;
                } else {
                    keptIDs.add(rsID);
                }

                JsonObject user = new JsonObject();
                user.addProperty("uid", rsID);
                user.addProperty("handle", rs.getString("handle"));
                user.addProperty("display_name", rs.getString("display_name"));
                user.addProperty("profile_picture", rs.getString("profile_picture"));
                userArray.add(user);

                counter++;
            }

        }

        System.out.println("Added first set of results.");

        if (counter == maxCount) {
            result.add("users", userArray);
            return result;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(GET_TAG_FOLLOWERS_QUERY)) {

            stmt.setString(1, search);
            stmt.setInt(2, uid);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                if (counter == 5) {
                    break;
                }

                int rsID = rs.getInt("id");
                System.out.println(keptIDs);
                if (keptIDs.contains(rsID)) {
                    System.out.println("Identical user.");
                    continue;
                }

                JsonObject user = new JsonObject();

                user.addProperty("uid", rsID);
                user.addProperty("handle", rs.getString("handle"));
                user.addProperty("display_name", rs.getString("display_name"));
                user.addProperty("profile_picture", rs.getString("profile_picture"));
                userArray.add(user);

                keptIDs.add(rsID);

                counter++;
            }

        }

        result.add("users", userArray);
        return result;
    }

}
