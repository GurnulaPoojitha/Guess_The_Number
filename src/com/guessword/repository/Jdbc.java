package com.guessword.repository;

import com.guessword.db.DataAccessException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Small helpers shared by the repositories to keep the JDBC boilerplate in one place. */
final class Jdbc {

    @FunctionalInterface
    interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private Jdbc() {
    }

    static void bind(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    static <T> List<T> list(PreparedStatement ps, RowMapper<T> mapper) throws SQLException {
        List<T> rows = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(mapper.map(rs));
            }
        }
        return rows;
    }

    static <T> Optional<T> first(PreparedStatement ps, RowMapper<T> mapper) throws SQLException {
        List<T> rows = list(ps, mapper);
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    static long generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("No id was generated");
            }
            return keys.getLong(1);
        }
    }

    static DataAccessException fail(SQLException e) {
        return new DataAccessException("Database error: " + e.getMessage(), e);
    }
}
