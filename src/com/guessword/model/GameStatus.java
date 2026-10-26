package com.guessword.model;

public enum GameStatus {
    IN_PROGRESS("In progress"),
    WON("Won"),
    LOST("Lost");

    private final String label;

    GameStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
