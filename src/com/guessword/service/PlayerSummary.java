package com.guessword.service;

import com.guessword.model.GameStatus;

import java.time.LocalDate;
import java.util.List;

/** Data for the player's home window: today's allowance, any unfinished game, and recent history. */
public record PlayerSummary(long gamesToday, int maxPerDay, Long activeGameId, List<HistoryItem> history) {

    /** {@code word} is null for a game that is still in progress. */
    public record HistoryItem(long gameId, LocalDate date, String word, GameStatus status, int attempts) {
    }

    public long remaining() {
        return Math.max(0, maxPerDay - gamesToday);
    }

    public boolean hasActiveGame() {
        return activeGameId != null;
    }

    public boolean canStartNewGame() {
        return activeGameId == null && remaining() > 0;
    }
}
