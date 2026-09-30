package com.cash_shop.sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.cash_shop.common.DBConnection;

public class RegisterSessionDAO {
    private static final int ACTIVE_WINDOW_SECONDS = 30;

    public String openSession(String cashierMatricule) {
        String sessionId = UUID.randomUUID().toString();
        try (Connection connection = DBConnection.getConnection()) {
            ensureSchema(connection);
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO cash_register_sessions (session_id, cashier, last_heartbeat) "
                            + "VALUES (?, ?, CURRENT_TIMESTAMP)")) {
                statement.setString(1, sessionId);
                statement.setString(2, cashierMatricule);
                statement.executeUpdate();
            }
            return sessionId;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to register the active cash register.", exception);
        }
    }

    public void heartbeat(String sessionId) {
        executeSessionUpdate("UPDATE cash_register_sessions SET last_heartbeat = CURRENT_TIMESTAMP "
                + "WHERE session_id = ?", sessionId);
    }

    public void closeSession(String sessionId) {
        executeSessionUpdate("DELETE FROM cash_register_sessions WHERE session_id = ?", sessionId);
    }

    public int getActiveSessionCount() {
        String sql = "SELECT COUNT(*) FROM cash_register_sessions "
                + "WHERE last_heartbeat >= CURRENT_TIMESTAMP - INTERVAL " + ACTIVE_WINDOW_SECONDS + " SECOND";
        try (Connection connection = DBConnection.getConnection()) {
            ensureSchema(connection);
            try (PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet results = statement.executeQuery()) {
                return results.next() ? results.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load active cash registers.", exception);
        }
    }

    private void executeSessionUpdate(String sql, String sessionId) {
        if (sessionId == null) {
            return;
        }
        try (Connection connection = DBConnection.getConnection()) {
            ensureSchema(connection);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, sessionId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update the cash register session.", exception);
        }
    }

    private void ensureSchema(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS cash_register_sessions ("
                + "session_id VARCHAR(36) PRIMARY KEY, "
                + "cashier VARCHAR(64) NOT NULL, "
                + "last_heartbeat TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "INDEX idx_cash_register_heartbeat (last_heartbeat)) ENGINE=InnoDB";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
}