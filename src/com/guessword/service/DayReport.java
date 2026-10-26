package com.guessword.service;

import java.time.LocalDate;
import java.util.List;

/** Admin report for a single day: how many players played and how many words were guessed correctly. */
public record DayReport(LocalDate date, List<UserRow> perUser) {

    public record UserRow(String username, long wordsTried, long correctGuesses) {
    }

    public long usersPlayed() {
        return perUser.size();
    }

    public long wordsTried() {
        return perUser.stream().mapToLong(UserRow::wordsTried).sum();
    }

    public long correctGuesses() {
        return perUser.stream().mapToLong(UserRow::correctGuesses).sum();
    }
}
