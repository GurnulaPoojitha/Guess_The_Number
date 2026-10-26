package com.guessword.service;

import com.guessword.Test;
import com.guessword.TestApp;
import com.guessword.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static com.guessword.Assert.assertEquals;
import static com.guessword.Assert.assertThrows;

public class ReportServiceTest extends TestApp {

    private static final LocalDate DAY_ONE = LocalDate.of(2026, 10, 14);
    private static final LocalDate DAY_TWO = LocalDate.of(2026, 10, 15);

    private final User one = player("PlayerOne");
    private final User two = player("PlayerTwo");
    private final User idle = player("PlayerIdle");

    public ReportServiceTest() {
        // Day one: PlayerOne wins one and loses one, PlayerTwo wins one
        win(one);
        lose(one);
        win(two);
        // Day two: PlayerOne wins one
        clock.advance(Duration.ofDays(1));
        win(one);
    }

    @Test
    void dayReportCountsUsersAndCorrectGuesses() {
        DayReport report = app.reports.dayReport(DAY_ONE);

        assertEquals(2L, report.usersPlayed());
        assertEquals(2L, report.correctGuesses());
        assertEquals(3L, report.wordsTried());
        assertEquals(List.of(
                new DayReport.UserRow("PlayerOne", 2, 1),
                new DayReport.UserRow("PlayerTwo", 1, 1)), report.perUser());
    }

    @Test
    void dayReportOnlyIncludesThatDay() {
        DayReport report = app.reports.dayReport(DAY_TWO);

        assertEquals(1L, report.usersPlayed());
        assertEquals(1L, report.correctGuesses());
        assertEquals(List.of(new DayReport.UserRow("PlayerOne", 1, 1)), report.perUser());
    }

    @Test
    void dayReportForAQuietDayIsEmpty() {
        DayReport report = app.reports.dayReport(LocalDate.of(2026, 1, 1));

        assertEquals(0L, report.usersPlayed());
        assertEquals(0L, report.correctGuesses());
        assertEquals(List.of(), report.perUser());
    }

    @Test
    void userReportListsEachDateNewestFirst() {
        UserReport report = app.reports.userReport(one.id());

        assertEquals("PlayerOne", report.username());
        assertEquals(List.of(
                new UserReport.DateRow(DAY_TWO, 1, 1),
                new UserReport.DateRow(DAY_ONE, 2, 1)), report.rows());
        assertEquals(3L, report.totalWordsTried());
        assertEquals(2L, report.totalCorrectGuesses());
    }

    @Test
    void userReportForAPlayerWhoNeverPlayedIsEmpty() {
        assertEquals(List.of(), app.reports.userReport(idle.id()).rows());
    }

    @Test
    void userReportForUnknownUserFails() {
        assertThrows(AppException.class, "not found", () -> app.reports.userReport(999_999L));
    }
}
