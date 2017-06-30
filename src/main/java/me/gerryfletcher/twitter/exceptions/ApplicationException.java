package me.gerryfletcher.twitter.exceptions;

/**
 * This exception is thrown when there is an SQL exception or DB error.
 * It is a critical error, and provides a layer of abstraction between
 * the DB and service.
 * <p>
 * The Exception must always be passed into an ApplicationException.
 */
public class ApplicationException extends Exception {
    public ApplicationException(String message, Exception e) {
        super(message, e);
    }
}
