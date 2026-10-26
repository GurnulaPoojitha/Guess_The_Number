package com.guessword.service;

import java.util.regex.Pattern;

/** Input rules from the specification, kept in one place so the screens and services agree. */
public final class ValidationRules {

    /** Letters only, at least 5, and must contain both an upper-case and a lower-case letter. */
    private static final Pattern USERNAME = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])[A-Za-z]{5,30}$");
    public static final String USERNAME_MESSAGE =
            "Username must be at least 5 letters (A-Z, a-z only) and include both upper- and lower-case letters.";

    /** At least 5 characters with at least one letter, one digit and one of $ % * @. */
    private static final Pattern PASSWORD = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$%*@]).{5,64}$");
    public static final String PASSWORD_MESSAGE =
            "Password must be at least 5 characters and include a letter, a number and one of $ % * @.";

    /** Exactly 5 upper-case letters, used for both guesses and stored words. */
    private static final Pattern WORD = Pattern.compile("^[A-Z]{5}$");
    public static final String WORD_MESSAGE = "Please enter exactly 5 upper-case letters (A-Z).";

    private ValidationRules() {
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD.matcher(password).matches();
    }

    public static boolean isValidWord(String word) {
        return word != null && WORD.matcher(word).matches();
    }
}
