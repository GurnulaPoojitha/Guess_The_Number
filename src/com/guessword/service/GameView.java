package com.guessword.service;

import com.guessword.model.GameStatus;
import com.guessword.model.LetterResult;

import java.util.List;

/**
 * Everything the game window needs. {@code answer} is only filled in once the game is over,
 * so the word is never available to the screen while the player is still guessing.
 */
public record GameView(long id, GameStatus status, List<Row> rows, int maxAttempts, String answer) {

    public record Row(String word, List<LetterResult> results) {
    }

    public int attemptsUsed() {
        return rows.size();
    }

    public boolean inProgress() {
        return status == GameStatus.IN_PROGRESS;
    }

    public boolean won() {
        return status == GameStatus.WON;
    }

    public boolean lost() {
        return status == GameStatus.LOST;
    }
}
