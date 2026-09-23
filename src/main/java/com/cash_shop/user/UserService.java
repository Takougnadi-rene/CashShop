package com.cash_shop.user;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    public boolean login(String username, String password_user) {
        User user = userDAO.findByUsername(username);
        if (user == null) return false;
        return password_user.equals(user.getPassword_user());
    }

    public boolean register(String username, String password) {
        if (userDAO.findByUsername(username) != null) return false;
        String hash = password;
        return userDAO.insert(username, hash);
    }
}
