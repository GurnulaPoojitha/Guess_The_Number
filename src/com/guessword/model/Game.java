package com.guessword.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** One word given to one player on one day. The guesses for it are stored in the guess table. */
public record Game(long id, long userId, Word word, LocalDate gameDate, LocalDateTime startedAt,
                   LocalDateTime finishedAt, GameStatus status) {

    public boolean isInProgress() {
        return status == GameStatus.IN_PROGRESS;
    }
}
