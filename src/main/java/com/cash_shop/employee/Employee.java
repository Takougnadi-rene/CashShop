package com.cash_shop.employee;

public class Employee {
    private String matricule;
    private String firstName;
    private String lastName;
    private String email;
    private double salary;
    private Role role;
    public enum Role {
        ADMIN,
        MANAGER,
        COUNTER,
        AISLE_MANAGER,
        CASHIER,
        SECURITY,
        CLEANER,
    }

    public Employee(String matricule, String firstName, String lastName, String email, double salary, Role role) {
        this.matricule = matricule;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.salary = salary;
        this.role = role;
    }

    public String getMatricule() {
        return matricule;
    }
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public double getSalary() {
        return salary;
    }
    public void setSalary(double salary) {
        this.salary = salary;
    }
    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    @Override 
    public String toString() {
        return "Employee{" +
                "matricule='" + matricule + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", salary=" + salary +
                ", role=" + role +
                '}';
    }
}
