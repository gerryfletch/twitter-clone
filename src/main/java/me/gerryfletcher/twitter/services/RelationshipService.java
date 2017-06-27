package me.gerryfletcher.twitter.services;

import me.gerryfletcher.twitter.controllers.relationships.RelationshipType;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;

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
            "FROM followers" +
            "WHERE (follower_id = ? AND following_id = ?)";
    private final PreparedStatement get_relationship = conn.prepareStatement(get_relationship_SQL);


    public static RelationshipService getInstance() throws SQLException {
        if (instance == null) {
            instance = new RelationshipService();
        }
        return instance;
    }

    /**
     * GetRelationship tells you the type of relationship between users, from the <b>first users point of view.</b>
     * @param handleOne The user whos persective it is from
     * @param handleTwo The user we are comparing the relationship with
     * @return  The relationshipType enum
     * @throws SQLException
     */
    public RelationshipType getRelationship(String handleOne, String handleTwo) throws SQLException {

        RelationshipType status = RelationshipType.NO_RELATIONSHIP;

        get_relationship.setString(1, handleOne);
        get_relationship.setString(2, handleTwo);

        ResultSet result = get_relationship.executeQuery();

        if(! result.next()) {
            return status;
        } else {
            status = RelationshipType.FOLLOWING;
        }

        // Check if the relationship is reversed
        get_relationship.setString(1, handleTwo);
        get_relationship.setString(2, handleOne);

        ResultSet resultTwo = get_relationship.executeQuery();

        if(resultTwo.next()) {
            status = RelationshipType.MUTUALS;
        }

        return status;

    }
}
