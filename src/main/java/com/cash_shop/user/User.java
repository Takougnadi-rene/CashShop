package com.cash_shop.user;

import com.cash_shop.employee.Employee.Role;

/** An application user: login, password, e-mail, role and the linked employee (name and matricule). */
public class User {
    
    private final String username;
    private final String password;
    private final String email;
    private final Role role;
    private final String employeeName;
    private final String employeeMatricule;

    /** Creates a user without a role. */
    public User(String username, String password, String email) {
        this(username, password, email, null);
    }

    /** Creates a user with a role. */
    public User(String username, String password, String email, Role role) {
        this(username, password, email, role, "");
    }

    /** Creates a user with a role and the employee name. */
    public User(String username, String password, String email, Role role, String employeeName) {
        this(username, password, email, role, employeeName, "");
    }

    /** Creates a user with all the data. */
    public User(String username, String password, String email, Role role, String employeeName,
            String employeeMatricule) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.employeeName = employeeName;
        this.employeeMatricule = employeeMatricule;
    }

    public String getUsername() {return username;}
    public String getPassword_user() {return password;}
    public String getEmail() {return email;}
    public Role getRole() {return role;}
    public String getEmployeeName() {return employeeName;}
    public String getEmployeeMatricule() {return employeeMatricule;}
}