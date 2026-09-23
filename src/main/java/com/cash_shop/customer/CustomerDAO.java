package com.cash_shop.customer;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cash_shop.common.DBConnection;

public class CustomerDAO {


    public void addCustomer(String name, String phone, double totalSpent) throws SQLException {
        String sql = "INSERT INTO customers(name, phone, fidelity_points) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setDouble(3, totalSpent);
            ps.setInt(4, 0); // Initial points
            ps.executeUpdate();
        }
    }

    public void deleteCustomer(int id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void addFidelityPoint(int customerId, int points) throws SQLException {
        String sql = "UPDATE customers SET fidelity_points = fidelity_points + ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    public void removeFidelityPoint(int customerId, int points) throws SQLException {
        String sql = "UPDATE customers SET fidelity_points = fidelity_points - ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    public void updateTotalSpent(int customerId, double amount) throws SQLException {
        String sql = "UPDATE customers SET total_spent = total_spent + ? WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, customerId);
            ps.executeUpdate();
        }
    }

    public ResultSet getAllCustomers() throws SQLException {
        String sql = "SELECT id, name, phone, total_spent, fidelity_points FROM customers";
        return DBConnection.getConnection().createStatement().executeQuery(sql);
    }
}

