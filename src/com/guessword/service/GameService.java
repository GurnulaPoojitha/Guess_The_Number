package com.guessword.service;

import com.guessword.db.Database;
import com.guessword.model.Game;
import com.guessword.model.GameStatus;
import com.guessword.model.Guess;
import com.guessword.model.User;
import com.guessword.model.Word;
import com.guessword.repository.GameRepository;
import com.guessword.repository.GuessRepository;
import com.guessword.repository.WordRepository;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GameService {

    public static final int MAX_GUESSES = 5;
    public static final int MAX_GAMES_PER_DAY = 3;

    private final Database db;
    private final GameRepository games;
    private final GuessRepository guesses;
    private final WordRepository words;
    private final Clock clock;
    private final Random random = new SecureRandom();

    public GameService(Database db, GameRepository games, GuessRepository guesses, WordRepository words,
                       Clock clock) {
        this.db = db;
        this.games = games;
        this.guesses = guesses;
        this.words = words;
        this.clock = clock;
    }

    public PlayerSummary summary(User user) {
        long playedToday = games.countByUserAndDate(user.id(), LocalDate.now(clock));
        Long activeId = games.findInProgress(user.id()).map(Game::id).orElse(null);
        List<PlayerSummary.HistoryItem> history = games.findRecent(user.id(), 10).stream()
                .map(g -> new PlayerSummary.HistoryItem(g.id(), g.gameDate(),
                        g.isInProgress() ? null : g.word().text(),
                        g.status(), guesses.findByGame(g.id()).size()))
                .toList();
        return new PlayerSummary(playedToday, MAX_GAMES_PER_DAY, activeId, history);
    }

    /**
     * Returns the player's unfinished game if there is one, otherwise gives them a new random
     * word, as long as they haven't already had {@value #MAX_GAMES_PER_DAY} words today.
     */
    public long startOrResumeGame(User user) {
        return db.inTransaction(() -> {
            Optional<Game> active = games.findInProgress(user.id());
            if (active.isPresent()) {
                return active.get().id();
            }
            LocalDate today = LocalDate.now(clock);
            if (games.countByUserAndDate(user.id(), today) >= MAX_GAMES_PER_DAY) {
                throw new AppException("You have already played " + MAX_GAMES_PER_DAY
                        + " words today. Come back tomorrow!");
            }
            Word word = pickRandomWord(user);
            return games.insert(user.id(), word, today, LocalDateTime.now(clock)).id();
        });
    }

    public GameView submitGuess(User user, long gameId, String rawGuess) {
        return db.inTransaction(() -> {
            Game game = loadOwnedGame(user, gameId);
            if (!game.isInProgress()) {
                throw new AppException("This game is already over.");
            }
            String guess = rawGuess == null ? "" : rawGuess.trim();
            if (!ValidationRules.isValidWord(guess)) {
                throw new AppException(ValidationRules.WORD_MESSAGE);
            }

            LocalDateTime now = LocalDateTime.now(clock);
            int attempt = guesses.findByGame(gameId).size() + 1;
            guesses.insert(gameId, guess, attempt, now);
            if (guess.equals(game.word().text())) {
                games.finish(gameId, GameStatus.WON, now);
            } else if (attempt >= MAX_GUESSES) {
                games.finish(gameId, GameStatus.LOST, now);
            }
            return toView(loadOwnedGame(user, gameId));
        });
    }

    public GameView getGameView(User user, long gameId) {
        return toView(loadOwnedGame(user, gameId));
    }

    private Word pickRandomWord(User user) {
        List<Word> candidates = words.findNotYetGivenTo(user.id());
        if (candidates.isEmpty()) {
            // The player has seen every word; start recycling them.
            candidates = words.findAll();
        }
        if (candidates.isEmpty()) {
            throw new AppException("No words are configured yet. Please ask an admin to add some.");
        }
        return candidates.get(random.nextInt(candidates.size()));
    }

    private GameView toView(Game game) {
        String answer = game.word().text();
        List<GameView.Row> rows = guesses.findByGame(game.id()).stream()
                .map(Guess::guessWord)
                .map(w -> new GameView.Row(w, WordEvaluator.evaluate(answer, w)))
                .toList();
        return new GameView(game.id(), game.status(), rows, MAX_GUESSES, game.isInProgress() ? null : answer);
    }

    private Game loadOwnedGame(User user, long gameId) {
        return games.findByIdForUser(gameId, user.id())
                .orElseThrow(() -> new AppException("Game not found."));
    }
}
