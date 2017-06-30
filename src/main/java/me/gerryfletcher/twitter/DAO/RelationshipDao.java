package me.gerryfletcher.twitter.DAO;

import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.controllers.relationships.RelationshipType;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Gerry on 30/06/2017.
 */
public class RelationshipDao extends UtilDao {

    public RelationshipDao() {}

    private final String GET_RELATIONSHIP_QUERY = "SELECT *" +
            "FROM followers " +
            "WHERE (follower_id = ? AND following_id = ?)";

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

        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(GET_RELATIONSHIP_QUERY)) {

            stmt.setInt(1, follower_id);
            stmt.setInt(2, following_id);

            ResultSet result = stmt.executeQuery();

            if(! result.next()) {
                return status;
            } else {
                status = RelationshipType.FOLLOWING;
            }

            // Check the reversed relationship

            stmt.setInt(1, following_id);
            stmt.setInt(2, follower_id);

            ResultSet resultTwo = stmt.executeQuery();

            if(resultTwo.next()) {
                status = RelationshipType.MUTUALS;
            }

            return status;

        }

    }

}
