package com.guessword.repository;

import com.guessword.db.Database;
import com.guessword.model.Guess;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

public class GuessRepository {

    private final Database db;

    public GuessRepository(Database db) {
        this.db = db;
    }

    public Guess insert(long gameId, String guessWord, int attemptNumber, LocalDateTime guessedAt) {
        String sql = "INSERT INTO guess (game_id, guess_word, attempt_number, guessed_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = db.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Jdbc.bind(ps, gameId, guessWord, attemptNumber, guessedAt);
            ps.executeUpdate();
            return new Guess(Jdbc.generatedId(ps), gameId, guessWord, attemptNumber, guessedAt);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    /** All guesses for a game, in the order they were made. */
    public List<Guess> findByGame(long gameId) {
        String sql = "SELECT id, game_id, guess_word, attempt_number, guessed_at FROM guess "
                + "WHERE game_id = ? ORDER BY attempt_number";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            Jdbc.bind(ps, gameId);
            return Jdbc.list(ps, rs -> new Guess(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getInt(4),
                    rs.getObject(5, LocalDateTime.class)));
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }
}
