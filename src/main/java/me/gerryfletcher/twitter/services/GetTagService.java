package me.gerryfletcher.twitter.services;

import com.google.gson.JsonObject;
import me.gerryfletcher.twitter.DAO.tweets.GetTagDao;
import me.gerryfletcher.twitter.exceptions.ApplicationException;

import java.sql.SQLException;

public class GetTagService {

    private static GetTagService instance = null;
    private GetTagDao getTagDao;
    private GetTagService() {
        this.getTagDao = new GetTagDao();
    }

    public static GetTagService getInstance() {
        if (instance == null) {
            instance = new GetTagService();
        }

        return instance;
    }

    public JsonObject getTags(String search, int uid) throws ApplicationException {
        try {
            return getTagDao.getTags(search, uid);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Failed to get tags from GetTags dao.", e);
        }
    }

}
