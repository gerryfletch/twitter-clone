package me.gerryfletcher.twitter.services.tweets;

import me.gerryfletcher.twitter.DAO.tweets.LikeDao;
import me.gerryfletcher.twitter.controllers.HashId;
import me.gerryfletcher.twitter.exceptions.ApplicationException;

import java.sql.SQLException;

public class LikeService {
    private static LikeService instance = null;
    private LikeDao likeDao;
    private HashId hash;

    public static LikeService getInstance() {
        if (instance == null) {
            instance = new LikeService();
        }
        return instance;
    }

    private LikeService() {
        likeDao = new LikeDao();
        hash = new HashId();
    }

    public void setLike(int uid, String hashid) throws ApplicationException {
        long tweetid = hash.decode(hashid)[0];

        try {
            likeDao.setLike(uid, tweetid);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem Liking Tweet in LikeDao.", e);
        }
    }

    public void unsetLike(int uid, String hashid) throws ApplicationException {
        long tweetid = hash.decode(hashid)[0];
        try {
            likeDao.unsetLike(uid, tweetid);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem Unliking Tweet in LikeDao.", e);
        }
    }
}
