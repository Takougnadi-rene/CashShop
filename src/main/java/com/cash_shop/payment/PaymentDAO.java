package com.cash_shop.payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.cash_shop.common.DBConnection;

public class PaymentDAO {

    public void insertPayment(Payment payment) {
        String sql = "INSERT INTO payments(payment_number, amount, payment_mode, payment_date, sale) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, payment.getPaymentNumber());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMode().name());
            ps.setDate(4, (java.sql.Date) payment.getPaymentDate());
            ps.setInt(5, payment.getSale().getSaleId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
