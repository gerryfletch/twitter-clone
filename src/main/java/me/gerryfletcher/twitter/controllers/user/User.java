package me.gerryfletcher.twitter.controllers.user;

import me.gerryfletcher.twitter.controllers.sqlite.SQLUtils;
import me.gerryfletcher.twitter.exceptions.BadDataException;
import me.gerryfletcher.twitter.exceptions.UserSqlException;

import java.sql.*;

/**
 * Created by Gerry on 24/06/2017.
 */
public class User implements AutoCloseable{

    private Connection conn = SQLUtils.connect();

    private int uid;
    private String handle;


    public User(String handle) throws BadDataException, UserSqlException {
        if(! Handle.isHandleValid(handle)) {
            throw new BadDataException("Invalid Username.");
        }

        this.handle = handle;

        if(! getUserId(handle))
            throw new UserSqlException("Problem getting user.");
    }

    public User(int uid) throws BadDataException, UserSqlException {
        if(uid < 1)
            throw new BadDataException("Invalid User ID.");
        else if (! checkIfIdExists(uid))
            throw new UserSqlException("User ID does not exist.");

        this.uid = uid;
    }

    @Override
    public void close() throws SQLException{
        this.conn.close();
    }

    private boolean getUserId(String handle) {
        String sql = "SELECT id FROM users WHERE handle = ?";
        try(PreparedStatement st = conn.prepareStatement(sql)) {
            st.setString(1, handle);
            ResultSet rs = st.executeQuery();

            if(rs.next()) {
                this.uid = rs.getInt("id");
                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean getUserHandle(int uid) {
        String sql = "SELECT handle FROM users WHERE id = ?";
        try(PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, uid);
            ResultSet rs = st.executeQuery();

            if(rs.next()) {
                this.handle = rs.getString("handle");
                return true;
            }

            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean checkIfIdExists(int id) {

        String sql = "SELECT TOP 1 id FROM users WHERE id = ?";

        try(PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return false;
    }

    public int getId() {
        return this.uid;
    }

    public String getHandle() throws UserSqlException {

        if(! getUserHandle(this.uid)) {
            throw new UserSqlException("");
        }

        return this.handle;
    }
    
}
