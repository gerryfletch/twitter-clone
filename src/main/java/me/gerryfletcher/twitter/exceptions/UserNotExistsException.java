package me.gerryfletcher.twitter.exceptions;

import java.sql.SQLException;

/**
 * The UserNotExistsException is used when a query
 * doesn't return a user.
 */
public class UserNotExistsException extends Exception {
    public UserNotExistsException(String message) {
        super(message);
    }

    public UserNotExistsException(String message, Exception e) {
        super(message, e);
        e.getStackTrace();
    }
}