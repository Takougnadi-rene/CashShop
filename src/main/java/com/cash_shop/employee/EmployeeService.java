package com.cash_shop.employee;

import com.cash_shop.employee.Employee.Role;
import com.cash_shop.user.AccountCredentials;

/** Business operations on employees (add, remove, update) and role-based cash register checks. */
public class EmployeeService {

    // add an employee
    public AccountCredentials addEmployee(String matricule, String firstName, String lastName, String email, double salary,
            Role role) {
        return new EmployeeDAO().insertEmployee(matricule, firstName, lastName, email, salary, role);
    }

    // remove an employee
    public void removeEmployee(Employee emp) {
        new EmployeeDAO().deleteEmployee(emp.getMatricule());
    }

    // update an employee
    public void updateEmployee(Employee emp) {
        new EmployeeDAO().updateEmployee(emp);
    }

}
