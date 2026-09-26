package com.cash_shop.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import com.cash_shop.common.DBConnection;
import com.cash_shop.employee.Employee.Role;

class UserDAO {
    User findByUsername(String username) {
        String sql = "SELECT login, password_user, email FROM users WHERE login = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getString("login"), rs.getString("password_user"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
        return null;
    }

    User authenticate(String username, String password) {
        String sql = "SELECT u.login, u.password_user, u.email, e.role "
                + "FROM users u INNER JOIN employees e "
                + "ON LOWER(TRIM(u.email)) = LOWER(TRIM(e.email)) "
                + "WHERE u.login = ? AND u.password_user = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    Role role = Role.valueOf(results.getString("role").trim().toUpperCase(Locale.ROOT));
                    return new User(results.getString("login"), results.getString("password_user"),
                            results.getString("email"), role);
                }
            }
        } catch (SQLException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to authenticate user or load the linked employee role.",
                    exception);
        }
        return null;
    }

    boolean insert(String username, String password, String email) {
        String sql = "INSERT INTO users (login, password_user, email) "
            + "SELECT ?, ?, e.email FROM employees e "
            + "WHERE LOWER(TRIM(e.email)) = LOWER(TRIM(?))";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible to register.", e);
        }
    }
}