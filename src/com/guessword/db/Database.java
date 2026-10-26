package com.guessword.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Holds the single JDBC connection used by the desktop app and creates the tables on first use.
 * <p>
 * The app uses an embedded H2 database stored in {@code ./data/guessword.mv.db}, so there is no
 * database server to install. Only {@code lib/h2-2.2.224.jar} needs to be on the classpath.
 */
public class Database implements AutoCloseable {

    public static final String DEFAULT_URL = "jdbc:h2:./data/guessword";

    private static final String[] SCHEMA = {
            """
            CREATE TABLE IF NOT EXISTS app_user (
                id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                username      VARCHAR(30)  NOT NULL UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                role          VARCHAR(10)  NOT NULL,
                created_at    TIMESTAMP    NOT NULL
            )""",
            """
            CREATE TABLE IF NOT EXISTS words (
                id        BIGINT AUTO_INCREMENT PRIMARY KEY,
                word_text VARCHAR(5) NOT NULL UNIQUE
            )""",
            """
            CREATE TABLE IF NOT EXISTS game (
                id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id     BIGINT      NOT NULL REFERENCES app_user(id),
                word_id     BIGINT      NOT NULL REFERENCES words(id),
                game_date   DATE        NOT NULL,
                started_at  TIMESTAMP   NOT NULL,
                finished_at TIMESTAMP,
                status      VARCHAR(15) NOT NULL
            )""",
            "CREATE INDEX IF NOT EXISTS idx_game_user_date ON game(user_id, game_date)",
            """
            CREATE TABLE IF NOT EXISTS guess (
                id             BIGINT AUTO_INCREMENT PRIMARY KEY,
                game_id        BIGINT     NOT NULL REFERENCES game(id),
                guess_word     VARCHAR(5) NOT NULL,
                attempt_number INT        NOT NULL,
                guessed_at     TIMESTAMP  NOT NULL,
                UNIQUE (game_id, attempt_number)
            )""",
    };

    /** Work that runs inside a transaction. */
    @FunctionalInterface
    public interface TransactionWork<T> {
        T run() throws SQLException;
    }

    private final Connection connection;

    public Database(String url) {
        try {
            this.connection = DriverManager.getConnection(url, "sa", "");
            try (Statement st = connection.createStatement()) {
                for (String ddl : SCHEMA) {
                    st.execute(ddl);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not open the database at " + url, e);
        }
    }

    public Connection connection() {
        return connection;
    }

    /** Runs {@code work} as one transaction: everything is saved, or nothing is. */
    public <T> T inTransaction(TransactionWork<T> work) {
        try {
            connection.setAutoCommit(false);
            try {
                T result = work.run();
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error", e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DataAccessException("Could not close the database", e);
        }
    }
}
