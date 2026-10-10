package com.myfinance.exception;

/**
 * Custom runtime exception thrown when database operations or JDBC connections fail.
 * Wraps SQLException to avoid leaking JDBC-specific exceptions into service/web layers.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
