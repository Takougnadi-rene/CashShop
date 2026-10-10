package com.cash_shop.bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.cash_shop.common.DBConnection;

/** Data access object for the {code bills} table. */
public class BillDAO {

    /** Inserts a bill; a null customer or cashier is stored as SQL NULL. */
    public void addBill(Bill bill) {
        String sql = "INSERT INTO bills(bill_number, bill_date, customer, cashier) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, bill.getBillNumber());
            ps.setTimestamp(2, new java.sql.Timestamp(bill.getBillDate().getTime()));
            if (bill.getCustomer() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, bill.getCustomer().getCustomerId());
            }
            if (bill.getCashier() == null) {
                ps.setNull(4, java.sql.Types.VARCHAR);
            } else {
                ps.setString(4, bill.getCashier().getMatricule());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Loads a bill header by number (customer, cashier and sale are not loaded here). */
    public Bill getBill(int billNumber) {
        String sql = "SELECT bill_number, bill_date FROM bills WHERE bill_number = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, billNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Bill(
                        rs.getInt("bill_number"),
                        rs.getTimestamp("bill_date"),
                        null,  // customer: extend query if needed
                        null,  // cashier: extend query if needed
                        null   // sale: extend query if needed
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

