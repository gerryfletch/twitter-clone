package me.gerryfletcher.twitter.DAO;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A utility class that all Dao extends. Gives additional functionality, such as checking
 * if a row exists or how many records there are of an identifier.
 */
public abstract class UtilDao {

    private HikariDataSource dataSource;

    UtilDao(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Selects the number of rows for a query using an identifier.
     * Example - Selecting number of Tweets of a user:
     * int numberOfTweets = selectCountByIdentifier("users", "id", 2);
     * <p>
     * Example - Selecting number of Followers of a user:
     * int numberOfFollowers = selectCountByIdentifier("followers", "following_id", 4);
     *
     * @param table      The name of the table.
     * @param column     The name of the column.
     * @param identifier The <b>integer</b> identifier.
     * @return The number of rows.
     * @throws SQLException In DB failiure.
     */
    protected int selectCountByIdentifier(String table, String column, int identifier) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + table + " WHERE " + column + "=?";
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, identifier);
            ResultSet count = stmt.executeQuery();
            count.next();

            return count.getInt(1);
        }
    }

    /**
     * Checks if a record exists using an integer identifier.
     * Example - Does user exist:
     * boolean userExists = doesRecordExist("users", "id", 4);
     * @param table The name of the table.
     * @param column    The name of the column.
     * @param identifier    The <b>integer</b> identifier.
     * @return  True/False if the record exists.
     * @throws SQLException In DB failiure.
     */
    protected boolean doesRecordExist(String table, String column, int identifier) throws SQLException {
        String query = "SELECT EXISTS(SELECT 1 FROM " + table + " WHERE " + column + "=? LIMIT 1)";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setInt(1, identifier);
            ResultSet result = stmt.executeQuery();
            result.next();

            return result.getInt(1) == 1;
        }
    }

    /**
     * Checks if a record exists using a String identifier.
     * Example - Does email exist:
     * boolean emailExists = doesRecordExist("users", "email", "example@gmail.com");
     * @param table The name of the table.
     * @param column    The name of the column.
     * @param identifier    The <b>String</b> identifier.
     * @return  In DB failiure.
     * @throws SQLException
     */
    protected boolean doesRecordExist(String table, String column, String identifier) throws SQLException {
        String query = "SELECT EXISTS(SELECT 1 FROM " + table + " WHERE lower(" + column + ")=? LIMIT 1)";
        try(Connection connection = dataSource.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, identifier);
            ResultSet result = stmt.executeQuery();
            result.next();

            return result.getInt(1) == 1;
        }
    }

}
