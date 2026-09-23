package com.cash_shop.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cash_shop.common.DBConnection;

class UserDAO {
    User findByUsername(String username) {
        String sql = "SELECT login, password_user FROM users WHERE login = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getString("login"), rs.getString("password_user"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
        return null;
    }

    boolean insert(String username, String password) {
        String sql = "INSERT INTO users (login, password_user, email) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, username + "@local.invalid");
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'enregistrer l'utilisateur.", e);
        }
    }
}