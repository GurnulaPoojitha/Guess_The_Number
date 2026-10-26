package com.guessword.repository;

import com.guessword.db.Database;
import com.guessword.model.Game;
import com.guessword.model.GameStatus;
import com.guessword.model.Word;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class GameRepository {

    private static final String SELECT = "SELECT g.id, g.user_id, g.game_date, g.started_at, g.finished_at, "
            + "g.status, w.id AS word_id, w.word_text FROM game g JOIN words w ON w.id = g.word_id ";

    /** One row of a report: a label (username or date), how many words were tried and how many won. */
    public record CountRow<K>(K key, long wordsTried, long correctGuesses) {
    }

    private final Database db;

    public GameRepository(Database db) {
        this.db = db;
    }

    private static Game map(ResultSet rs) throws SQLException {
        return new Game(rs.getLong("id"), rs.getLong("user_id"),
                new Word(rs.getLong("word_id"), rs.getString("word_text")),
                rs.getObject("game_date", LocalDate.class),
                rs.getObject("started_at", LocalDateTime.class),
                rs.getObject("finished_at", LocalDateTime.class),
                GameStatus.valueOf(rs.getString("status")));
    }

    public Game insert(long userId, Word word, LocalDate gameDate, LocalDateTime startedAt) {
        String sql = "INSERT INTO game (user_id, word_id, game_date, started_at, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = db.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Jdbc.bind(ps, userId, word.id(), gameDate, startedAt, GameStatus.IN_PROGRESS.name());
            ps.executeUpdate();
            return new Game(Jdbc.generatedId(ps), userId, word, gameDate, startedAt, null, GameStatus.IN_PROGRESS);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public Optional<Game> findById(long id) {
        try (PreparedStatement ps = db.connection().prepareStatement(SELECT + "WHERE g.id = ?")) {
            Jdbc.bind(ps, id);
            return Jdbc.first(ps, GameRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    /** Only returns the game if it belongs to this user. */
    public Optional<Game> findByIdForUser(long id, long userId) {
        try (PreparedStatement ps = db.connection().prepareStatement(SELECT + "WHERE g.id = ? AND g.user_id = ?")) {
            Jdbc.bind(ps, id, userId);
            return Jdbc.first(ps, GameRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public Optional<Game> findInProgress(long userId) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                SELECT + "WHERE g.user_id = ? AND g.status = ? ORDER BY g.started_at DESC")) {
            Jdbc.bind(ps, userId, GameStatus.IN_PROGRESS.name());
            return Jdbc.first(ps, GameRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public long countByUserAndDate(long userId, LocalDate date) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT COUNT(*) FROM game WHERE user_id = ? AND game_date = ?")) {
            Jdbc.bind(ps, userId, date);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public List<Game> findRecent(long userId, int limit) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                SELECT + "WHERE g.user_id = ? ORDER BY g.started_at DESC, g.id DESC LIMIT ?")) {
            Jdbc.bind(ps, userId, limit);
            return Jdbc.list(ps, GameRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public void finish(long gameId, GameStatus status, LocalDateTime finishedAt) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "UPDATE game SET status = ?, finished_at = ? WHERE id = ?")) {
            Jdbc.bind(ps, status.name(), finishedAt, gameId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    /** For the day report: per player on {@code date}, words tried and words guessed correctly. */
    public List<CountRow<String>> countsByUserOn(LocalDate date) {
        String sql = "SELECT u.username, COUNT(*) AS tried, "
                + "SUM(CASE WHEN g.status = 'WON' THEN 1 ELSE 0 END) AS correct "
                + "FROM game g JOIN app_user u ON u.id = g.user_id "
                + "WHERE g.game_date = ? GROUP BY u.username ORDER BY u.username";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            Jdbc.bind(ps, date);
            return Jdbc.list(ps, rs -> new CountRow<>(rs.getString(1), rs.getLong(2), rs.getLong(3)));
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    /** For the user report: per date, words tried and words guessed correctly, newest first. */
    public List<CountRow<LocalDate>> countsByDateFor(long userId) {
        String sql = "SELECT game_date, COUNT(*) AS tried, "
                + "SUM(CASE WHEN status = 'WON' THEN 1 ELSE 0 END) AS correct "
                + "FROM game WHERE user_id = ? GROUP BY game_date ORDER BY game_date DESC";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            Jdbc.bind(ps, userId);
            return Jdbc.list(ps, rs -> new CountRow<>(rs.getObject(1, LocalDate.class), rs.getLong(2), rs.getLong(3)));
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }
}
