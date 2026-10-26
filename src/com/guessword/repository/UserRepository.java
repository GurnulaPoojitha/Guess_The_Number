package com.guessword.repository;

import com.guessword.db.Database;
import com.guessword.model.Role;
import com.guessword.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    private static final String COLUMNS = "id, username, password_hash, role, created_at";

    private final Database db;

    public UserRepository(Database db) {
        this.db = db;
    }

    private static User map(ResultSet rs) throws SQLException {
        return new User(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"),
                Role.valueOf(rs.getString("role")), rs.getObject("created_at", LocalDateTime.class));
    }

    /** Usernames are matched ignoring case, so "PlayerOne" and "playerone" are the same account. */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT " + COLUMNS + " FROM app_user WHERE LOWER(username) = LOWER(?)";
        try (PreparedStatement ps = db.connection().prepareStatement(sql)) {
            Jdbc.bind(ps, username);
            return Jdbc.first(ps, UserRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public Optional<User> findById(long id) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT " + COLUMNS + " FROM app_user WHERE id = ?")) {
            Jdbc.bind(ps, id);
            return Jdbc.first(ps, UserRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    public List<User> findByRole(Role role) {
        try (PreparedStatement ps = db.connection().prepareStatement(
                "SELECT " + COLUMNS + " FROM app_user WHERE role = ? ORDER BY username")) {
            Jdbc.bind(ps, role.name());
            return Jdbc.list(ps, UserRepository::map);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }

    public User insert(String username, String passwordHash, Role role, LocalDateTime createdAt) {
        String sql = "INSERT INTO app_user (username, password_hash, role, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = db.connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            Jdbc.bind(ps, username, passwordHash, role.name(), createdAt);
            ps.executeUpdate();
            return new User(Jdbc.generatedId(ps), username, passwordHash, role, createdAt);
        } catch (SQLException e) {
            throw Jdbc.fail(e);
        }
    }
}
