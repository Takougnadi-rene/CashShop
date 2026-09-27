package com.cash_shop.user;

import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    public boolean login(String username, String password_user) {
        return authenticate(username, password_user) != null;
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public boolean updatePassword(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Select a user first.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        return userDAO.updatePassword(username, password);
    }

    public boolean register(String username, String password) {
        throw new IllegalArgumentException("An employee email is required to register a user.");
    }

    public boolean register(String username, String password, String employeeEmail) {
        if (employeeEmail == null || employeeEmail.trim().isEmpty()) return false;
        if (userDAO.findByUsername(username) != null) return false;
        String hash = password;
        return userDAO.insert(username, hash, employeeEmail);
    }
}
