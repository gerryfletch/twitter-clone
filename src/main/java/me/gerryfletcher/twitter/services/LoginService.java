package me.gerryfletcher.twitter.services;

import me.gerryfletcher.twitter.DAO.account.LoginDao;
import me.gerryfletcher.twitter.exceptions.ApplicationException;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserNotExistsException;

import java.sql.SQLException;

public class LoginService {

    private static LoginService instance = null;
    private LoginDao loginDao;

    private LoginService() {
        this.loginDao = new LoginDao();
    }

    public static LoginService getInstance() {
        if (instance == null) {
            instance = new LoginService();
        }

        return instance;
    }

    /**
     * Checks if the handle and password are valid, then attempts login.
     *
     * @param handle   Handle to be checked
     * @param password Plain-text password
     * @return The JSON Web token
     * @throws UserNotExistsException If the user does not exist
     * @throws BadDataException       If a detail is wrong
     */
    public String loginUser(String handle, String password) throws BadDataException, ApplicationException, UserNotExistsException {
        try {
            return loginDao.loginUser(handle, password);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem logging User in DAO.", e);
        }
    }

}
