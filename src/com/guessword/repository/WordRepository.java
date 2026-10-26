package com.guessword.repository;

import com.guessword.db.Database;
import com.guessword.model.Word;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class WordRepository {

    private final Database db;

    public WordRepository(Database db) {
        this.db = db;
    }

    private static Word map(ResultSet rs) throws SQLException {
        return new Word(rs.getLong("id"), rs.getString("word_text"));
    }

    public long count() {
        try (PreparedStatement ps = db.connection().prepareStatement("SELECT COUNT(*) FROM words");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public List<Word> findAll() {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT id, word_text FROM words ORDER BY word_text")) {
            return Jdbc.list(ps, WordRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public Optional<Word> findByText(String text) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT id, word_text FROM words WHERE word_text = ?")) {
            Jdbc.bind(ps, text);
            return Jdbc.first(ps, WordRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public Optional<Word> findById(long id) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT id, word_text FROM words WHERE id = ?")) {
            Jdbc.bind(ps, id);
            return Jdbc.first(ps, WordRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    /** Words this player has never been given, so nobody gets the same word twice. */
    public List<Word> findNotYetGivenTo(long userId) {
        String sql = "SELECT id, word_text FROM words "
                + "WHERE id NOT IN (SELECT word_id FROM game WHERE user_id = ?) ORDER BY id";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            Jdbc.bind(ps, userId);
            return Jdbc.list(ps, WordRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public boolean isUsedInAnyGame(long wordId) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT COUNT(*) FROM game WHERE word_id = ?")) {
            Jdbc.bind(ps, wordId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public Word insert(String text) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "INSERT INTO words (word_text) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            Jdbc.bind(ps, text);
            ps.executeUpdate();
            return new Word(Jdbc.generatedId(ps), text);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public void delete(long id) {
        try (PreparedStatement ps = db.connection().prepareStatement("DELETE FROM words WHERE id = ?")) {
            Jdbc.bind(ps, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }
}
