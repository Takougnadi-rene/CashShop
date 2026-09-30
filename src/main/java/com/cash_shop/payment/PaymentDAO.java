package com.cash_shop.payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.cash_shop.common.DBConnection;

public class PaymentDAO {

    private static final Logger LOGGER = Logger.getLogger(PaymentDAO.class.getName());

    public void insertPayment(Payment payment) {
        String sql = "INSERT INTO payments(payment_number, amount, payment_mode, payment_date, sale) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, payment.getPaymentNumber());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMode().name());
            ps.setTimestamp(4, new java.sql.Timestamp(payment.getPaymentDate().getTime()));
            ps.setInt(5, payment.getSale().getSaleId());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert payment", e);
        }
    }

    public double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            return results.next() ? results.getDouble(1) : 0.0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load total revenue.", exception);
        }
    }

}
