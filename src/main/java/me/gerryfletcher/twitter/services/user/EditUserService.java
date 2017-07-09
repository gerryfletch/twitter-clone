package me.gerryfletcher.twitter.services.user;

import me.gerryfletcher.twitter.DAO.profile.EditUserDao;
import me.gerryfletcher.twitter.exceptions.ApplicationException;

import java.net.URL;
import java.sql.SQLException;

public class EditUserService {

    private static EditUserService instance = null;
    private EditUserDao editUserDao;

    private EditUserService() {
        this.editUserDao = new EditUserDao();
    }

    public static EditUserService getInstance() {
        if (instance == null) {
            instance = new EditUserService();
        }

        return instance;
    }

    public void updateDisplayName(int uid, String displayName) throws ApplicationException {
        try {
            editUserDao.updateDisplayName(uid, displayName);
        } catch (SQLException e) {
            throw new ApplicationException("Problem updating DisplayName in DAO.", e);
        }
    }

    public void updateProfilePicture(int uid, URL profileHref) throws ApplicationException {
        try {
            editUserDao.updateProfilePicture(uid, profileHref);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem updating Profile Picture in DAO.", e);
        }
    }

    public void updateBio(int uid, String bio) throws ApplicationException {
        try {
            editUserDao.updateBio(uid, bio);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ApplicationException("Problem updating Bio in DAO.", e);
        }
    }
}
