package me.gerryfletcher.twitter.DAO.profile;

import me.gerryfletcher.twitter.DAO.UtilDao;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EditUserDao extends UtilDao {

    public EditUserDao() {
    }

    private final String UPDATE_DISPLAY_NAME_QUERY = "UPDATE users SET display_name = ? WHERE id = ?";

    public void updateDisplayName(int uid, String displayName) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_DISPLAY_NAME_QUERY)) {

            stmt.setString(1, displayName);
            stmt.setInt(2, uid);

            stmt.executeUpdate();
            System.out.println("Statement executed.");
        }
    }


    private final String UPDATE_PROFILE_PICTURE_QUERY = "UPDATE account_details SET profile_picture = ? WHERE id = ?";
    private final String CREATE_PROFILE_PICTURE_QUERY = "INSERT INTO account_details(id, profile_picture) VALUES(?,?)";

    public void updateProfilePicture(int uid, URL pictureHref) throws SQLException {
        UserDao userDao = new UserDao();
        boolean doesRecordExist = userDao.doesRecordExist("account_details", "id", uid);

        // If the record exists, update it. If not, create a new one.

        if (doesRecordExist) {
            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(UPDATE_PROFILE_PICTURE_QUERY)) {

                stmt.setString(1, String.valueOf(pictureHref));
                stmt.setInt(2, uid);

                stmt.executeUpdate();
            }
        } else {
            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(CREATE_PROFILE_PICTURE_QUERY)) {

                stmt.setInt(1, uid);
                stmt.setString(2, String.valueOf(pictureHref));

                stmt.executeUpdate();
            }
        }
    }

    private final String UPDATE_BIO_QUERY = "UPDATE account_details SET bio = ? WHERE id = ?";
    private final String CREATE_BIO_QUERY = "INSERT INTO account_details(id, bio) VALUES (?,?)";

    public void updateBio(int uid, String bio) throws SQLException {
        UserDao userDao = new UserDao();
        boolean doesRecordExist = userDao.doesRecordExist("account_details", "id", uid);

        // If the record exists, update it. If not, create a new one.

        if (doesRecordExist) {
            try (Connection connection = getConnection();
                 PreparedStatement stmt = connection.prepareStatement(UPDATE_BIO_QUERY)) {

                stmt.setString(1, bio);
                stmt.setInt(2, uid);

                stmt.executeUpdate();
            }
        } else {
            try(Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement(CREATE_BIO_QUERY)) {

                stmt.setInt(1, uid);
                stmt.setString(2, bio);

                stmt.executeUpdate();
            }
        }

    }
}
