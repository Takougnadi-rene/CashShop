package com.cash_shop.customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cash_shop.common.DBConnection;

public class CustomerDAO {

    public void insertCustomer(int customerId, String name, String email, String phoneNumber, double totalSpent)
            throws SQLException {
        insertCustomer(customerId, name, phoneNumber, totalSpent);
    }

    public void insertCustomer(int customerId, String name, String phoneNumber, double totalSpent)
            throws SQLException {
        String sql = "INSERT INTO customers(customer_id, name, phone_number, total_spent, fidelity_points) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.setString(2, name);
            ps.setString(3, phoneNumber);
            ps.setDouble(4, totalSpent);
            ps.setInt(5, 0);
            ps.executeUpdate();
        }
    }

    public void deleteCustomer(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void addFidelityPoint(int customerId, int points) throws SQLException {
        String sql = "UPDATE customers SET fidelity_points = fidelity_points + ? WHERE customer_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    public void removeFidelityPoint(int customerId, int points) throws SQLException {
        String sql = "UPDATE customers SET fidelity_points = fidelity_points - ? WHERE customer_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    public void updateTotalSpent(int customerId, double amount) throws SQLException {
        String sql = "UPDATE customers SET total_spent = total_spent + ? WHERE customer_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    public List<Customer> getAllCustomers() {
        String sql = "SELECT customer_id, name, phone_number, total_spent, fidelity_points "
            + "FROM customers ORDER BY customer_id";
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Customer customer = new Customer(results.getInt("customer_id"), results.getString("name"), "",
                    results.getString("phone_number"), results.getDouble("total_spent"));
                customer.setLoyaltyPoints(results.getInt("fidelity_points"));
                customers.add(customer);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load customers from the database.", exception);
        }
        return customers;
    }

    public Customer getCustomerByPhone(String phoneNumber) {
        String sql = "SELECT customer_id, name, phone_number, total_spent, fidelity_points "
            + "FROM customers WHERE phone_number = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setString(1, phoneNumber);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                Customer customer = new Customer(results.getInt("customer_id"), results.getString("name"), "",
                    results.getString("phone_number"), results.getDouble("total_spent"));
                customer.setLoyaltyPoints(results.getInt("fidelity_points"));
                return customer;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load customer from the database.", exception);
        }
        return null;
    }
}