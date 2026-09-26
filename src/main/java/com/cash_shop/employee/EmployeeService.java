package com.cash_shop.employee;

import com.cash_shop.employee.Employee.Role;

public class EmployeeService {

    // add
    public void addEmployee(String matricule, String firstName, String lastName, String email, double salary,
            Role role) {
        new EmployeeDAO().insertEmployee(matricule, firstName, lastName, email, salary, role);
    }

    // remove
    public void removeEmployee(Employee emp) {
        new EmployeeDAO().deleteEmployee(emp.getMatricule());
    }

    // update
    public void updateEmployee(Employee emp) {
        new EmployeeDAO().updateEmployee(emp);
    }

    // cashier
    public void openCashRegister(Employee employee) {
        if (employee.getRole() == Employee.Role.CASHIER) {
            System.out.println("Cash register opened by " + employee.getFirstName() + " " + employee.getLastName());
        } else {
            System.out.println("Only cashiers can open the cash register.");
        }
    }

    public void performSale(Employee employee) {
        if (employee.getRole() == Employee.Role.CASHIER || employee.getRole() == Employee.Role.MANAGER) {
            System.out.println("Sale performed by " + employee.getFirstName() + " " + employee.getLastName());
        } else {
            System.out.println("Only cashiers and managers can perform sales.");
        }
    }

    public void processPayment(Employee employee) {
        if (employee.getRole() == Employee.Role.CASHIER) {
            System.out.println("Payment processed by " + employee.getFirstName() + " " + employee.getLastName());
        } else {
            System.out.println("Only cashiers can process payments.");
        }
    }

}
