package me.gerryfletcher.twitter.exceptions;

import java.sql.SQLException;

/**
 * Created by Gerry on 15/06/2017.
 */
public class UserSqlException extends Exception{
    public UserSqlException(String message) {super(message); }
    public UserSqlException(String message, SQLException e) {
        super(message, e);
        e.getStackTrace();
    }
}
