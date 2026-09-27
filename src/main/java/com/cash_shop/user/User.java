package com.cash_shop.user;

import com.cash_shop.employee.Employee.Role;

public class User {
    
    private final String username;
    private final String password;
    private final String email;
    private final Role role;
    private final String employeeName;

    public User(String username, String password, String email) {
        this(username, password, email, null);
    }

    public User(String username, String password, String email, Role role) {
        this(username, password, email, role, "");
    }

    public User(String username, String password, String email, Role role, String employeeName) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.employeeName = employeeName;
    }

    public String getUsername() {return username;}
    public String getPassword_user() {return password;}
    public String getEmail() {return email;}
    public Role getRole() {return role;}
    public String getEmployeeName() {return employeeName;}
}