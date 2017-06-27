package me.gerryfletcher.twitter.exceptions;

import java.sql.SQLException;

/**
 * Created by Gerry on 27/06/2017.
 */
public class UserNotExistsException extends SQLException {
    public UserNotExistsException(String message) {
        super(message);
    }

    public UserNotExistsException(String message, SQLException e) {
        super(message, e);
        e.getStackTrace();
    }
}