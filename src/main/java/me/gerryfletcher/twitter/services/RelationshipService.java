package me.gerryfletcher.twitter.services;

import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.relationships.RelationshipType;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;
import me.gerryfletcher.twitter.exceptions.ApplicationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RelationshipService {

    private static RelationshipService instance = null;

    protected RelationshipService() throws SQLException {
    }

    private Connection conn = SQLUtils.connect();

    private final String get_relationship_SQL = "SELECT *" +
            "FROM followers " +
            "WHERE (follower_id = ? AND following_id = ?)";
    private final PreparedStatement get_relationship = conn.prepareStatement(get_relationship_SQL);

    private final String create_following_SQL = "";
    private final PreparedStatement create_following = conn.prepareStatement(create_following_SQL);


    public static RelationshipService getInstance() throws SQLException {
        if (instance == null) {
            instance = new RelationshipService();
        }
        return instance;
    }

    /**
     * GetRelationship tells you the type of relationship between users, from the <b>first users point of view.</b>
     *
     * @param follower_id  The user whos persective it is from
     * @param following_id The user we are comparing the relationship with
     * @return The relationshipType enum
     * @throws SQLException
     */
    public RelationshipType getRelationship(int follower_id, int following_id) throws SQLException {

        RelationshipType status = RelationshipType.NO_RELATIONSHIP;

        get_relationship.setInt(1, follower_id);
        get_relationship.setInt(2, following_id);

        ResultSet result = get_relationship.executeQuery();

        if (!result.next()) {
            return status;
        } else {
            status = RelationshipType.FOLLOWING;
        }

        // Check if the relationship is reversed
        get_relationship.setInt(1, following_id);
        get_relationship.setInt(2, follower_id);

        ResultSet resultTwo = get_relationship.executeQuery();

        if (resultTwo.next()) {
            status = RelationshipType.MUTUALS;
        }

        return status;

    }

    public JsonObject getRelationshipJson(int follower_id, int following_id) throws SQLException {
        RelationshipType relationship = getRelationship(follower_id, following_id);

        boolean following = (relationship == RelationshipType.FOLLOWING || relationship == RelationshipType.MUTUALS);
        boolean mutuals = (relationship == RelationshipType.MUTUALS);

        JsonObject response = new JsonObject();
        response.addProperty("following", following);
        response.addProperty("mutuals", mutuals);

        return response;
    }

    /**
     * Makes user 1 follow user 2.
     *
     * @param uid      User 1 ID.
     * @param followId Receiving user 2 ID.
     * @return True if successful, false if they are already following.
     */
    public boolean setFollowing(int uid, int followId) {
        UserService userService = UserService.getInstance();
        return false;
    }
}
