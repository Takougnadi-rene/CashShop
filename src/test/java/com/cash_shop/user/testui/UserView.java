package com.cash_shop.user.testui;

import java.util.LinkedList;

import javax.swing.JFrame;

import com.cash_shop.user.User;

public interface UserView {

    JFrame createWindow();

    boolean login(String username, String password);


    public default LinkedList<User> getUsers() {
        // Implémentation fictive pour les tests
        LinkedList<User> users = new LinkedList<>();
        users.add(new User("user1", "password1"));
        users.add(new User("user2", "password2"));
        return users;
    }
}