package com.cash_shop.user;

import java.util.List;

/** Business operations on users: authentication, registration and password changes (with validation). */
public class UserService {
    private final UserDAO userDAO = new UserDAO();

    /** Returns the authenticated user, or null. */
    public User authenticate(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    /** Tells whether the credentials are valid. */
    public boolean login(String username, String password_user) {
        return authenticate(username, password_user) != null;
    }

    /** Returns all users. */
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    /** Resets a user's password (minimum 8 characters). */
    public boolean updatePassword(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Select a user first.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        return userDAO.updatePassword(username, password);
    }

    /** Changes the password of the signed-in user after validating the current and the new password. */
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("The signed-in user is required.");
        }
        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("Enter your current password.");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must contain at least 8 characters.");
        }
        if (newPassword.equals(currentPassword)) {
            throw new IllegalArgumentException("Choose a password different from the current one.");
        }
        return userDAO.changePassword(username, currentPassword, newPassword);
    }

    /** Not supported without an employee e-mail: always throws. */
    public boolean register(String username, String password) {
        throw new IllegalArgumentException("An employee email is required to register a user.");
    }
}
