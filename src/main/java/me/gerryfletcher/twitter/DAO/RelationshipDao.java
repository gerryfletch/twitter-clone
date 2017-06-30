package me.gerryfletcher.twitter.DAO;

import me.gerryfletcher.twitter.models.RelationshipType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RelationshipDao extends UtilDao {

    private final String GET_RELATIONSHIP_QUERY = "SELECT *" +
            "FROM followers " +
            "WHERE (follower_id = ? AND following_id = ?)";

    public RelationshipDao() {}

    /**
     * GetRelationship tells you the type of relationship between users, from the <b>first users point of view.</b>
     *
     * @param follower_id  The user whos persective it is from.
     * @param following_id The user we are comparing the relationship with.
     * @return The relationshipType enum.
     * @throws SQLException In DB failiure.
     */
    public RelationshipType getRelationship(int follower_id, int following_id) throws SQLException {

        RelationshipType status = RelationshipType.NO_RELATIONSHIP;

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_RELATIONSHIP_QUERY)) {

            stmt.setInt(1, follower_id);
            stmt.setInt(2, following_id);

            ResultSet result = stmt.executeQuery();

            if (!result.next()) {
                return status;
            } else {
                status = RelationshipType.FOLLOWING;
            }

            // Check the reversed relationship

            stmt.setInt(1, following_id);
            stmt.setInt(2, follower_id);

            ResultSet resultTwo = stmt.executeQuery();

            if (resultTwo.next()) {
                status = RelationshipType.MUTUALS;
            }

            return status;

        }

    }

    private final String SET_FOLLOWING_QUERY = "INSERT INTO followers(follower_id, following_id) VALUES(?,?)";

    public void setFollowing(int userId, int idToFollow) throws SQLException {
        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(SET_FOLLOWING_QUERY)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, idToFollow);

            stmt.executeUpdate();
        }
    }

    private final String UNSET_FOLLOWING_QUERY = "DELETE FROM followers WHERE (follower_id=? AND following_id=?)";

    public void unsetFollowing(int userId, int idToUnfollow) throws SQLException {
        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(UNSET_FOLLOWING_QUERY)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, idToUnfollow);

            stmt.executeUpdate();

        }
    }
}
