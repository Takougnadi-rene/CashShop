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
    public List<Aisle> getAllAisles() {
        String sql = "SELECT aisle_code, aisle_name, category, aisle_chief FROM aisles ORDER BY aisle_code";
        List<Aisle> aisles = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                aisles.add(new Aisle(results.getInt("aisle_code"), results.getString("aisle_name"),
                        results.getString("category"), results.getString("aisle_chief")));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load aisles from the database.", exception);
        }
        return aisles;
    }

    public List<Product> getProductsForAisle(int aisleCode) {
        String sql = "SELECT p.reference, p.designation, p.purchase_price, p.selling_price, p.stock_quantity "
                + "FROM aisle_products ap JOIN products p ON p.reference = ap.product_reference "
                + "WHERE ap.aisle_code = ? ORDER BY p.designation";
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, aisleCode);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    products.add(new Product(results.getInt("reference"), results.getString("designation"),
                            results.getDouble("purchase_price"), results.getDouble("selling_price"),
                            results.getInt("stock_quantity")));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load aisle products from the database.", exception);
        }
        return products;
    }

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
