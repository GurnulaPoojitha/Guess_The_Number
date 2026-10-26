package com.guessword.model;

/** Colour feedback for a single letter of a guess. */
public enum LetterResult {
    /** Letter is in the word and in the right position: green. */
    CORRECT,
    /** Letter is in the word but in the wrong position: orange. */
    PRESENT,
    /** Letter is not in the word: grey. */
    ABSENT
}
