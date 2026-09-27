package com.cash_shop.employee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cash_shop.common.DBConnection;
import com.cash_shop.user.AccountCredentialGenerator;
import com.cash_shop.user.AccountCredentials;

public class EmployeeDAO {

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT matricule, first_name, last_name, email, salary, role "
                + "FROM employees ORDER BY matricule";

        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                employees.add(new Employee(
                        resultSet.getString("matricule"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getDouble("salary"),
                        Employee.Role.valueOf(resultSet.getString("role").toUpperCase())));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible de charger les employés.", exception);
        }

        return employees;
    }

    public AccountCredentials insertEmployee(String matricule, String first_name, String last_name, String email, double salary,
            Employee.Role role) {
        AccountCredentials generated = new AccountCredentials(AccountCredentialGenerator.createLogin(first_name,
                last_name), AccountCredentialGenerator.createPassword());
        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                String employeeSql = "INSERT INTO employees "
                        + "(matricule, first_name, last_name, email, salary, role) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(employeeSql)) {
                    statement.setString(1, matricule);
                    statement.setString(2, first_name);
                    statement.setString(3, last_name);
                    statement.setString(4, email);
                    statement.setDouble(5, salary);
                    statement.setString(6, role.name());
                    statement.executeUpdate();
                }

                String login = findAvailableLogin(connection, generated.login());
                String userSql = "INSERT INTO users (login, password_user, email) VALUES (?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(userSql)) {
                    statement.setString(1, login);
                    statement.setString(2, generated.password());
                    statement.setString(3, email);
                    statement.executeUpdate();
                }
                connection.commit();
                return new AccountCredentials(login, generated.password());
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to create employee and login: " + exception.getMessage(),
                    exception);
        }
    }

    private String findAvailableLogin(Connection connection, String baseLogin) throws SQLException {
        String login = baseLogin;
        int suffix = 2;
        String sql = "SELECT 1 FROM users WHERE login = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            while (true) {
                statement.setString(1, login);
                try (ResultSet results = statement.executeQuery()) {
                    if (!results.next()) return login;
                }
                login = baseLogin + suffix++;
            }
        }
    }

    public void updateEmployee(Employee emp) {
        String sql = "UPDATE employees SET first_name = ?, last_name = ?, email = ?, "
                + "salary = ?, role = ? WHERE matricule = ?";
        execute(sql, statement -> {
            statement.setString(1, emp.getFirstName());
            statement.setString(2, emp.getLastName());
            statement.setString(3, emp.getEmail());
            statement.setDouble(4, emp.getSalary());
            statement.setString(5, emp.getRole().name());
            statement.setString(6, emp.getMatricule());
        });
    }

    public void deleteEmployee(String matricule) {
        String sql = "DELETE FROM employees WHERE matricule = ?";
        execute(sql, statement -> statement.setString(1, matricule));
    }

    public void openCashRegister(Employee employee) {
        if (employee.getRole() == Employee.Role.CASHIER) {
            System.out.println("Cash register opened by " + employee.getFirstName() + " " + employee.getLastName());
        } else {
            System.out.println("Only cashiers can open the cash register.");
        }
    }

    public void performSale(Employee employee) {
        if (employee.getRole() == Employee.Role.CASHIER || employee.getRole() == Employee.Role.COUNTER) {
            System.out.println("Sale performed by " + employee.getFirstName() + " " + employee.getLastName());
        } else {
            System.out.println("Only cashiers can perform sales.");
        }
    }

    public void processPayment(Employee employee) {
        if (employee.getRole() == Employee.Role.CASHIER) {
            System.out.println("Payment processed by " + employee.getFirstName() + " " + employee.getLastName());
        } else {
            System.out.println("Only cashiers can process payments.");
        }
    }

    private void execute(String sql, StatementParameters parameters) {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            parameters.set(statement);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", exception);
        }
    }

    @FunctionalInterface
    private interface StatementParameters {
        void set(PreparedStatement statement) throws SQLException;
    }
}
