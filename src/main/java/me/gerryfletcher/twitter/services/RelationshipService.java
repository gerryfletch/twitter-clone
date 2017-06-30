package me.gerryfletcher.twitter.services;

import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.DAO.RelationshipDao;
import me.gerryfletcher.twitter.DAO.UserDao;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.RelationshipType;

import java.sql.SQLException;

public class RelationshipService {

    private static RelationshipService instance = null;
    private RelationshipDao relationshipDao;
    private final UserDao userDao;

    private RelationshipService() {
        this.relationshipDao = new RelationshipDao();
        this.userDao = new UserDao();
    }


    public static RelationshipService getInstance() {
        if (instance == null) {
            instance = new RelationshipService();
        }
        return instance;
    }

    /**
     * GetRelationship tells you the type of relationship between users, from the <b>first users point of view.</b>
     *
     * @param follower_id  The user whos persective it is from.
     * @param following_id The user we are comparing the relationship with.
     * @return The relationshipType enum.
     * @throws ApplicationException In DB failiure.
     */
    public RelationshipType getRelationship(int follower_id, int following_id) throws ApplicationException {
        try {
            return relationshipDao.getRelationship(follower_id, following_id);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem getting Relationship from DAO", e);
        }
    }

    public RelationshipType getRelationship(String follower_handle, String following_handle) throws ApplicationException {
        try {
            int follower_id = userDao.getUID(follower_handle);
            int following_id = userDao.getUID(following_handle);

            return getRelationship(follower_id, following_id);
        } catch (UserNotExistsException e) {
            return RelationshipType.NO_RELATIONSHIP;
        } catch (SQLException e) {
            throw new ApplicationException("Problem getting Relationship from DAO.", e);
        }
    }

    /**
     * Returns a JSON representation of a relationship.
     * Example: User 1 follows User 2, but User 2 does <b>not</b> follow User 1.
     * {
     * "following": true,
     * "mutuals": false
     * }
     *
     * @param follower_id  The user whos perspective it is from.
     * @param following_id The user we are comparing the relationship with.
     * @return A JSON representation of a relationship.
     * @throws ApplicationException In DB failiure.
     */
    public JsonObject getRelationshipJson(int follower_id, int following_id) throws ApplicationException {
        RelationshipType relationship = getRelationship(follower_id, following_id);

        boolean following = (relationship == RelationshipType.FOLLOWING || relationship == RelationshipType.MUTUALS);
        boolean mutuals = (relationship == RelationshipType.MUTUALS);

        JsonObject response = new JsonObject();
        response.addProperty("following", following);
        response.addProperty("mutuals", mutuals);

        return response;
    }

    public void setFollowing(String userHandle, String handleToFollow) throws ApplicationException, UserNotExistsException {
        try {
            int userId = userDao.getUID(userHandle);
            int idToFollow = userDao.getUID(handleToFollow);

            relationshipDao.setFollowing(userId, idToFollow);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem setting Relationship in DAO.", e);
        }
    }

    public void unsetFollowing(String userHandle, String handleToUnfollow) throws ApplicationException, UserNotExistsException {
        try {
            int userId = userDao.getUID(userHandle);
            int idToUnfollow = userDao.getUID(handleToUnfollow);

            relationshipDao.unsetFollowing(userId, idToUnfollow);
        } catch (SQLException e) {
            throw new ApplicationException("Problem unsetting Relationship in DAO", e);
        }
    }
}
