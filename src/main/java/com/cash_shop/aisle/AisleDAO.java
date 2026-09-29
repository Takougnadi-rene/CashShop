package com.cash_shop.aisle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "INSERT INTO aisle_lines (aisle_code, reference) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisle.getAisleCode());
            ps.setInt(2, product.getReference());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
    }

    public void removeProductFromAisle(Aisle aisle, Product product) {
        String sql = "DELETE FROM aisle_lines WHERE aisle_code = ? AND reference = ?";
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisle.getAisleCode());
            ps.setInt(2, product.getReference());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
    }

    public void searchProductInAisleRef(Aisle aisle, String productReference) {
        String sql = "SELECT * FROM aisle_lines WHERE aisle_code = ? AND reference = ?";
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
        String sql = "SELECT * FROM aisle_lines ap JOIN products p ON ap.reference = p.reference WHERE ap.aisle_code = ? AND p.designation = ?";
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

    public List<Aisle> getAllAisles() {
        String sql = "SELECT aisle_code, aisle_name, category, aisle_chief FROM aisles ORDER BY aisle_code";
        List<Aisle> aisles = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                aisles.add(new Aisle(
                    rs.getInt("aisle_code"),
                    rs.getString("aisle_name"),
                    rs.getString("category"),
                    rs.getString("aisle_chief")
                ));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
        return aisles;
    }

    public List<Product> getProductsForAisle(int aisleCode) {
        String sql = "SELECT p.reference, p.designation, p.purchase_price, p.selling_price, p.stock_quantity " +
                     "FROM aisle_lines ap " +
                     "JOIN products p ON ap.reference = p.reference " +
                     "WHERE ap.aisle_code = ?";
        List<Product> products = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, aisleCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(
                        rs.getInt("reference"),
                        rs.getString("designation"),
                        rs.getDouble("purchase_price"),
                        rs.getDouble("selling_price"),
                        rs.getInt("stock_quantity")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", e);
        }
        return products;
    }

}
