package com.cash_shop.user;


public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) {
        return userDAO.authenticate(username, password);
    }

    public boolean login(String username, String password_user) {
        return authenticate(username, password_user) != null;
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
