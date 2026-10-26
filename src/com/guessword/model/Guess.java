package com.guessword.model;

import java.time.LocalDateTime;

/** A single word a player submitted while trying to guess a game's word. */
public record Guess(long id, long gameId, String guessWord, int attemptNumber, LocalDateTime guessedAt) {
}
