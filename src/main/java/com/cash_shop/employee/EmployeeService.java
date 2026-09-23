package com.cash_shop.employee;
public class EmployeeService {

    public void addEmployee(Employee employee) {
        //Employee emp = new Employee();
        System.out.println("Employee added: " + employee.getFirstName() + " " + employee.getLastName());
    }

    public void removeEmployee(Employee employee) {
        System.out.println("Employee removed: " + employee.getFirstName() + " " + employee.getLastName());
    }

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
