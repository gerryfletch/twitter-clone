package me.gerryfletcher.twitter.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariDataSource;
import me.gerryfletcher.twitter.DAO.UserDao;
import me.gerryfletcher.twitter.controllers.sqlite.DBHandler;
import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.models.Handle;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;
import me.gerryfletcher.twitter.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    private static UserService instance = null;
    private UserDao userDao;

    protected UserService() {
        this.userDao = new UserDao(DBHandler.getDataSource());
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public User getUser(int uid) throws UserNotExistsException, ApplicationException {
        Gson gson = new Gson();
        JsonObject user = new JsonObject();

        try {
            user = userDao.getProfile(uid);
        } catch (SQLException e) {
            throw new ApplicationException("Problem getting User from DAO.", e);
        }

        return gson.fromJson(user, User.class);
    }
}
