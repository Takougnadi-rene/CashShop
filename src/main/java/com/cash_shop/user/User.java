package com.cash_shop.user;

public class User {
    private final String username;
    private final String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public String getUsername() {return username;}  
    public String getPassword_user() {return password;}
}