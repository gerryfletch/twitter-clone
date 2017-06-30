package me.gerryfletcher.twitter.exceptions;

/**
 * This exception is thrown if an SQL query returns no rows.
 */
public class RecordNotExistsException extends Exception{
    public RecordNotExistsException() {
        super("No records returned.");
    }

    public RecordNotExistsException(Exception e) {
        super("No records returned", e);
    }

    public RecordNotExistsException(String message) {
        super(message);
    }

    public RecordNotExistsException(String message, Exception e) {
        super(message, e);
    }
}
