package com.guessword.db;

/** Unchecked wrapper for SQL errors so callers don't need try/catch around every query. */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
