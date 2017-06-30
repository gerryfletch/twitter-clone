package me.gerryfletcher.twitter.services;

import me.gerryfletcher.twitter.DAO.RegisterDao;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserExistsException;

import java.sql.SQLException;

public class RegisterService {

    private static RegisterService instance = null;
    RegisterDao registerDao;

    RegisterService() {
        this.registerDao = new RegisterDao();
    }

    public static RegisterService getInstance() {
        if (instance == null) {
            instance = new RegisterService();
        }

        return instance;
    }


    public String registerUser(String handle, String displayName, String email, String password) throws BadDataException, UserExistsException, ApplicationException {
        try {
            return registerDao.registerUser(handle, displayName, email, password);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem registering user in DAO.", e);
        }
    }
}
