package com.cash_shop.user;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.cash_shop.common.DBConnection;
import com.cash_shop.employee.Employee.Role;

class UserDAO {
    List<User> findAll() {
        String sql = "SELECT login, email FROM users_role ORDER BY login";
        List<User> users = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                users.add(new User(results.getString("login"), "", results.getString("email")));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load application users.", exception);
        }
        return users;
    }

    boolean updatePassword(String username, String password) {
        String sql = "UPDATE users SET password_user = ? WHERE login = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, password);
            statement.setString(2, username);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update the user password.", exception);
        }
    }

    boolean changePassword(String username, String currentPassword, String newPassword) {
        String sql = "UPDATE users SET password_user = ? WHERE login = ? AND password_user = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setString(2, username);
            statement.setString(3, currentPassword);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to change the account password.", exception);
        }
    }

    User findByUsername(String username) {
        String sql = "SELECT login, password_user, email FROM users_role WHERE login = ?";
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
        String sql = "SELECT login, password_user, email, role "
                + "FROM users_role WHERE login = ? AND password_user = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    String roleName = results.getString("role");
                    try {
                        Role role = Role.valueOf(roleName.trim().toUpperCase(Locale.ROOT));
                        String email = results.getString("email");
                        return new User(results.getString("login"), results.getString("password_user"),
                            email, role, findEmployeeName(connection, email),
                            findEmployeeMatricule(connection, email));
                    } catch (IllegalArgumentException | NullPointerException exception) {
                        throw new IllegalStateException("Unsupported role in users_role: " + roleName, exception);
                    }
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to authenticate user from users_role: " + exception.getMessage(),
                    exception);
        }
        return null;
    }

    private String findEmployeeName(Connection connection, String email) {
        String sql = "SELECT CONCAT(first_name, ' ', last_name) AS employee_name "
                + "FROM employees WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? results.getString("employee_name") : email;
            }
        } catch (SQLException exception) {
            return email;
        }
    }

    private String findEmployeeMatricule(Connection connection, String email) {
        String sql = "SELECT matricule FROM employees "
                + "WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? results.getString("matricule") : "";
            }
        } catch (SQLException exception) {
            return "";
        }
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