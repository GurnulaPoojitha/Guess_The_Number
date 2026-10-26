package com.guessword.service;

/**
 * A rule was broken (invalid username, daily limit reached, game already over...).
 * The message is written for the user and is shown as-is in the window.
 */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }
}
