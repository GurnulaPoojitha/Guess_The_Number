package com.guessword.service;

import com.guessword.model.User;
import com.guessword.repository.GameRepository;
import com.guessword.repository.UserRepository;

import java.time.LocalDate;

public class ReportService {

    private final GameRepository games;
    private final UserRepository users;

    public ReportService(GameRepository games, UserRepository users) {
        this.games = games;
        this.users = users;
    }

    /** Number of users who played on {@code date} and number of words they guessed correctly. */
    public DayReport dayReport(LocalDate date) {
        return new DayReport(date, games.countsByUserOn(date).stream()
                .map(r -> new DayReport.UserRow(r.key(), r.wordsTried(), r.correctGuesses()))
                .toList());
    }

    /** For one player: each date they played, the number of words tried and guessed correctly. */
    public UserReport userReport(long userId) {
        User user = users.findById(userId).orElseThrow(() -> new AppException("User not found."));
        return new UserReport(user.username(), games.countsByDateFor(userId).stream()
                .map(r -> new UserReport.DateRow(r.key(), r.wordsTried(), r.correctGuesses()))
                .toList());
    }
}
