package com.guessword.model;

/** A 5-letter, upper-case English word that can be given to players. */
public record Word(long id, String text) {

    @Override
    public String toString() {
        return text;
    }
}
