package com.cash_shop.user;

public class User {
    private final String username;
    private final String password;
    private final int email;

    public User(String username, String password, int email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    public String getUsername() {return username;}  
    public String getPassword() {return password;}
    public int getEmail() {return email;}
}