package me.gerryfletcher.twitter.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.DAO.UserDao;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.User;

import java.sql.SQLException;

public class UserService {

    private static UserService instance = null;
    private UserDao userDao;

    private UserService() {
        this.userDao = new UserDao();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    /**
     * Returns a JSON object containing a users profile.
     * - UID
     * - Handle
     * - Display Name
     * - Profile Picture
     * - Bio
     * - Statistics
     *  - Number of Tweets
     *  - Number of Followers
     *  - Number of Following
     * @param uid   The users ID.
     * @return  JSON representation of a users profile.
     * @throws UserNotExistsException   If the user does not exist.
     * @throws ApplicationException In DB failiure.
     */
    public JsonObject getUserJson(int uid) throws UserNotExistsException, ApplicationException {
        try {
            return userDao.getProfile(uid);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem getting User JSON from DAO.", e);
        }
    }

    public User getUser(int uid) throws UserNotExistsException, ApplicationException {
        Gson gson = new Gson();
        JsonObject user = new JsonObject();

        try {
            user = userDao.getProfile(uid);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem getting User from DAO.", e);
        }

        return gson.fromJson(user, User.class);
    }

    /**
     * Gets a users ID from a handle.
     * @param handle    The users handle.
     * @return  The users integer ID.
     * @throws UserNotExistsException   If the user does not exist.
     * @throws ApplicationException In DB failiure.
     */
    public int getUserId(String handle) throws UserNotExistsException, ApplicationException {
        try {
            return userDao.getUID(handle);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem getting user ID from DAO.", e);
        }
    }

    /**
     * Gets a users handle from UID.
     * @param uid   The users ID.
     * @return  The users String handle.
     * @throws UserNotExistsException   If the user does not exist.
     * @throws ApplicationException In DB failiure.
     */
    public String getUserHandle(int uid) throws UserNotExistsException, ApplicationException {
        try {
            return userDao.getHandle(uid);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem getting user Handle from DAO.", e);
        }
    }

    public boolean doesHandleExist(String handle) throws ApplicationException {
        try {
            return userDao.doesHandleExist(handle);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem checking if Handle exists in DAO.", e);
        }
    }

    boolean doesEmailExist(String email) throws ApplicationException {
        try {
            return userDao.doesEmailExist(email);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem checking if Email exists in DAO.", e);
        }
    }
}
