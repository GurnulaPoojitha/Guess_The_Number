package com.guessword;

import com.guessword.db.Database;
import com.guessword.repository.GameRepository;
import com.guessword.repository.GuessRepository;
import com.guessword.repository.UserRepository;
import com.guessword.repository.WordRepository;
import com.guessword.service.DataSeeder;
import com.guessword.service.GameService;
import com.guessword.service.ReportService;
import com.guessword.service.UserService;
import com.guessword.service.WordService;

import java.time.Clock;

/** Creates the database, repositories and services once and hands them to the screens. */
public final class AppContext implements AutoCloseable {

    public final Database db;
    public final UserRepository userRepository;
    public final WordRepository wordRepository;
    public final GameRepository gameRepository;
    public final GuessRepository guessRepository;

    public final UserService users;
    public final GameService games;
    public final ReportService reports;
    public final WordService words;
    public final Clock clock;

    public AppContext(String jdbcUrl, Clock clock) {
        this.clock = clock;
        this.db = new Database(jdbcUrl);
        this.userRepository = new UserRepository(db);
        this.wordRepository = new WordRepository(db);
        this.gameRepository = new GameRepository(db);
        this.guessRepository = new GuessRepository(db);

        this.users = new UserService(userRepository, clock);
        this.games = new GameService(db, gameRepository, guessRepository, wordRepository, clock);
        this.reports = new ReportService(gameRepository, userRepository);
        this.words = new WordService(wordRepository);

        DataSeeder.seed(wordRepository, userRepository, users);
    }

    @Override
    public void close() {
        db.close();
    }
}
