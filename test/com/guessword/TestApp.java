package com.guessword;

import com.guessword.model.User;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base class for tests that need the database. Each test gets its own fresh in-memory database
 * (seeded with the 20 words and the admin), so tests never affect each other or your real data.
 */
public abstract class TestApp implements AutoCloseable {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    protected final MutableClock clock = new MutableClock();
    protected final AppContext app =
            new AppContext("jdbc:h2:mem:test" + COUNTER.incrementAndGet() + ";DB_CLOSE_DELAY=-1", clock);

    protected User player(String username) {
        return app.users.registerPlayer(username, "abc$1", "abc$1");
    }

    protected String answerOf(long gameId) {
        return app.gameRepository.findById(gameId).orElseThrow().word().text();
    }

    /** Starts a game and guesses it right first time. */
    protected long win(User user) {
        long id = app.games.startOrResumeGame(user);
        app.games.submitGuess(user, id, answerOf(id));
        return id;
    }

    /** Starts a game and uses all 5 guesses on a word that is never the answer. */
    protected long lose(User user) {
        long id = app.games.startOrResumeGame(user);
        for (int i = 0; i < 5; i++) {
            app.games.submitGuess(user, id, "XXXXX");
        }
        return id;
    }

    @Override
    public void close() {
        try (var st = app.db.connection().createStatement()) {
            st.execute("SHUTDOWN"); // throw the in-memory database away
        } catch (Exception ignored) {
            // already closed
        }
        app.close();
    }
}
