package com.guessword.service;

import com.guessword.Test;
import com.guessword.TestApp;
import com.guessword.model.Game;
import com.guessword.model.GameStatus;
import com.guessword.model.Guess;
import com.guessword.model.LetterResult;
import com.guessword.model.User;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.guessword.Assert.assertEquals;
import static com.guessword.Assert.assertFalse;
import static com.guessword.Assert.assertNull;
import static com.guessword.Assert.assertThrows;
import static com.guessword.Assert.assertTrue;

public class GameServiceTest extends TestApp {

    private static final String WRONG = "XXXXX"; // never one of the stored words

    private final User player = player("PlayerOne");

    @Test
    void twentyUpperCaseWordsAreSeeded() {
        assertEquals(20L, app.wordRepository.count());
        app.wordRepository.findAll().forEach(w ->
                assertTrue(w.text().matches("[A-Z]{5}"), w.text() + " should be 5 upper-case letters"));
    }

    @Test
    void startingAGamePicksAWordFromTheDatabase() {
        long id = app.games.startOrResumeGame(player);

        Game game = app.gameRepository.findById(id).orElseThrow();
        assertEquals(GameStatus.IN_PROGRESS, game.status());
        assertEquals(LocalDate.of(2026, 10, 14), game.gameDate());
        assertTrue(app.wordRepository.findByText(game.word().text()).isPresent(), "word comes from the database");
    }

    @Test
    void unfinishedGameIsResumedInsteadOfStartingAnother() {
        long first = app.games.startOrResumeGame(player);
        app.games.submitGuess(player, first, WRONG);

        assertEquals(first, app.games.startOrResumeGame(player));
        assertEquals(1L, app.games.summary(player).gamesToday());
    }

    @Test
    void correctGuessWinsTheGame() {
        long id = app.games.startOrResumeGame(player);
        app.games.submitGuess(player, id, WRONG);

        GameView view = app.games.submitGuess(player, id, answerOf(id));

        assertTrue(view.won(), "game should be won");
        assertEquals(2, view.attemptsUsed());
        assertEquals(answerOf(id), view.answer());
        assertEquals(List.of(LetterResult.CORRECT, LetterResult.CORRECT, LetterResult.CORRECT,
                LetterResult.CORRECT, LetterResult.CORRECT), view.rows().get(1).results());
    }

    @Test
    void fiveWrongGuessesLoseTheGame() {
        long id = app.games.startOrResumeGame(player);
        for (int i = 1; i <= 4; i++) {
            assertTrue(app.games.submitGuess(player, id, WRONG).inProgress(), "still playing after " + i);
        }

        GameView view = app.games.submitGuess(player, id, WRONG);

        assertTrue(view.lost(), "game should be lost after 5 wrong guesses");
        assertEquals(5, view.attemptsUsed());
        assertEquals(answerOf(id), view.answer());
    }

    @Test
    void noMoreGuessesAfterFiveAttempts() {
        long id = lose(player);

        assertThrows(AppException.class, "already over", () -> app.games.submitGuess(player, id, WRONG));
        assertEquals(5, app.guessRepository.findByGame(id).size());
    }

    @Test
    void noMoreGuessesAfterWinning() {
        long id = win(player);
        assertThrows(AppException.class, "already over", () -> app.games.submitGuess(player, id, WRONG));
    }

    @Test
    void invalidGuessesAreRejectedAndNotCounted() {
        long id = app.games.startOrResumeGame(player);
        for (String bad : new String[]{"tower", "Tower", "TOWE", "TOWERS", "T0WER", "", "   ", null}) {
            assertThrows(AppException.class, "5 upper-case letters", () -> app.games.submitGuess(player, id, bad));
        }
        assertEquals(0, app.games.getGameView(player, id).attemptsUsed());
    }

    @Test
    void answerIsHiddenWhileTheGameIsInProgress() {
        long id = app.games.startOrResumeGame(player);
        app.games.submitGuess(player, id, WRONG);
        assertNull(app.games.getGameView(player, id).answer());
    }

    @Test
    void guessesAreSavedInOrderWithDateAndTime() {
        long id = app.games.startOrResumeGame(player);
        app.games.submitGuess(player, id, "AUDIO");
        clock.advance(Duration.ofSeconds(30));
        app.games.submitGuess(player, id, "HOMER");

        List<Guess> saved = app.guessRepository.findByGame(id);
        assertEquals(List.of("AUDIO", "HOMER"), saved.stream().map(Guess::guessWord).toList());
        assertEquals(List.of(1, 2), saved.stream().map(Guess::attemptNumber).toList());
        assertEquals(LocalDateTime.of(2026, 10, 14, 10, 0, 0), saved.get(0).guessedAt());
        assertEquals(LocalDateTime.of(2026, 10, 14, 10, 0, 30), saved.get(1).guessedAt());
    }

    @Test
    void exampleBoardFromTheSpecificationIsColouredCorrectly() {
        Game game = app.gameRepository.insert(player.id(), app.wordRepository.findByText("TOWER").orElseThrow(),
                LocalDate.now(clock), LocalDateTime.now(clock));
        for (String guess : List.of("AUDIO", "HOMER", "JOKER", "TONER")) {
            app.games.submitGuess(player, game.id(), guess);
        }
        GameView view = app.games.submitGuess(player, game.id(), "TOWER");

        assertTrue(view.won(), "TOWER should win");
        List<String> colours = view.rows().stream()
                .map(r -> r.results().stream().map(GameServiceTest::colour).collect(Collectors.joining(" ")))
                .toList();
        assertEquals(List.of(
                "grey grey grey grey orange",
                "grey green grey green green",
                "grey green grey green green",
                "green green grey green green",
                "green green green green green"), colours);
    }

    private static String colour(LetterResult r) {
        return switch (r) {
            case CORRECT -> "green";
            case PRESENT -> "orange";
            case ABSENT -> "grey";
        };
    }

    @Test
    void onlyThreeWordsPerDay() {
        for (int i = 0; i < GameService.MAX_GAMES_PER_DAY; i++) {
            win(player);
        }

        assertThrows(AppException.class, "Come back tomorrow", () -> app.games.startOrResumeGame(player));
        PlayerSummary summary = app.games.summary(player);
        assertEquals(3L, summary.gamesToday());
        assertFalse(summary.canStartNewGame(), "no new game allowed");
    }

    @Test
    void lostGamesAlsoCountTowardsTheDailyLimit() {
        for (int i = 0; i < GameService.MAX_GAMES_PER_DAY; i++) {
            lose(player);
        }
        assertThrows(AppException.class, "Come back tomorrow", () -> app.games.startOrResumeGame(player));
    }

    @Test
    void dailyLimitResetsTheNextDay() {
        for (int i = 0; i < GameService.MAX_GAMES_PER_DAY; i++) {
            win(player);
        }
        clock.advance(Duration.ofDays(1));

        long id = app.games.startOrResumeGame(player);

        assertEquals(LocalDate.of(2026, 10, 15), app.gameRepository.findById(id).orElseThrow().gameDate());
        assertEquals(1L, app.games.summary(player).gamesToday());
    }

    @Test
    void dailyLimitIsPerPlayer() {
        User other = player("PlayerTwo");
        for (int i = 0; i < GameService.MAX_GAMES_PER_DAY; i++) {
            win(player);
        }
        assertTrue(app.games.startOrResumeGame(other) > 0, "another player can still play");
    }

    @Test
    void playerIsNotGivenTheSameWordTwice() {
        Set<String> seen = new HashSet<>();
        for (int day = 0; day < 6; day++) {
            for (int i = 0; i < GameService.MAX_GAMES_PER_DAY; i++) {
                seen.add(answerOf(win(player)));
            }
            clock.advance(Duration.ofDays(1));
        }
        assertEquals(18, seen.size());
    }

    @Test
    void playersCannotSeeEachOthersGames() {
        User other = player("PlayerTwo");
        long id = app.games.startOrResumeGame(player);

        assertThrows(AppException.class, "not found", () -> app.games.getGameView(other, id));
        assertThrows(AppException.class, "not found", () -> app.games.submitGuess(other, id, WRONG));
    }

    @Test
    void summaryShowsHistoryNewestFirst() {
        long first = win(player);
        clock.advance(Duration.ofMinutes(5));
        long second = lose(player);

        List<PlayerSummary.HistoryItem> history = app.games.summary(player).history();
        assertEquals(second, history.get(0).gameId());
        assertEquals(GameStatus.LOST, history.get(0).status());
        assertEquals(5, history.get(0).attempts());
        assertEquals(first, history.get(1).gameId());
        assertEquals(GameStatus.WON, history.get(1).status());
    }
}
