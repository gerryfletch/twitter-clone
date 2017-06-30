package me.gerryfletcher.twitter.exceptions;

public class UserExistsException extends Exception {
    public UserExistsException(String message) {
        super(message);
    }

    public UserExistsException(String message, Exception e) {
        super(message, e);
    }
}
