package com.guessword.service;

import java.time.LocalDate;
import java.util.List;

/** Admin report for a single player: per date, the number of words tried and guessed correctly. */
public record UserReport(String username, List<DateRow> rows) {

    public record DateRow(LocalDate date, long wordsTried, long correctGuesses) {
    }

    public long totalWordsTried() {
        return rows.stream().mapToLong(DateRow::wordsTried).sum();
    }

    public long totalCorrectGuesses() {
        return rows.stream().mapToLong(DateRow::correctGuesses).sum();
    }
}
