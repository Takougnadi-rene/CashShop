package com.cash_shop.aisle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.cash_shop.common.DBConnection;
import com.cash_shop.product.Product;

public class AisleDAO {
    public void addAisle(Aisle aisle) {
        String sql = "INSERT INTO aisles (aisle_code, aisle_name, category, aisle_chief) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisle.getAisleCode());
            ps.setString(2, aisle.getAisleName());
            ps.setString(3, aisle.getCategory());
            ps.setString(4, aisle.getAisleChief());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
    }

    public void removeAisle(Aisle aisle) {
        String sql = "DELETE FROM aisles WHERE aisle_code = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisle.getAisleCode());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
    }

    public void addProductToAisle(Aisle aisle, Product product) {
        String sql = "INSERT INTO aisle_products (aisle_code, product_reference) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisle.getAisleCode());
            ps.setString(2, String.valueOf(product.getReference()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
    }

    public void removeProductFromAisle(Aisle aisle, Product product) {
        String sql = "DELETE FROM aisle_products WHERE aisle_code = ? AND product_reference = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisle.getAisleCode());
            ps.setString(2, String.valueOf(product.getReference()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
    }
    public void searchProductInAisleRef(Aisle aisle, String productReference) {
        String sql = "SELECT * FROM aisle_products WHERE aisle_code = ? AND product_reference = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisle.getAisleCode());
            ps.setString(2, productReference);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Product found in aisle: " + aisle.getAisleName());
                } else {
                    System.out.println("Product not found in aisle: " + aisle.getAisleName());
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
    }

    public void searchProductInAisleDes(Aisle aisle, String productDesignation) {
        String sql = "SELECT * FROM aisle_products ap JOIN products p ON ap.product_reference = p.reference WHERE ap.aisle_code = ? AND p.designation = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisle.getAisleCode());
            ps.setString(2, productDesignation);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Product found in aisle: " + aisle.getAisleName());
                } else {
                    System.out.println("Product not found in aisle: " + aisle.getAisleName());
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
    }

}
