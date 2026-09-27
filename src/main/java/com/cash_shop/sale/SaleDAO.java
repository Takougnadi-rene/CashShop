package com.cash_shop.sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cash_shop.common.DBConnection;
import com.cash_shop.product.Product;

public class SaleDAO {

    public void insertSale(Sale sale) {
        String sql = "INSERT INTO sales(sale_id, sale_date, cashier) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sale.getSaleId());
            ps.setDate(2, java.sql.Date.valueOf(sale.getSaleDate()));
            ps.setString(3, sale.getCashier().getMatricule());
            ps.executeUpdate();
        } catch (SQLException e) {
           // e.printStackTrace();
        }
    }

    public void updateSale(Sale sale) {
        String sql = "UPDATE sales SET sale_date = ?, cashier = ? WHERE sale_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(sale.getSaleDate()));
            ps.setString(2, sale.getCashier().getMatricule());
            ps.setInt(3, sale.getSaleId());
            ps.executeUpdate();
        } catch (SQLException e) {
           // e.printStackTrace();
        }
    }

    public void deleteSale(int saleId) {
        String sql = "DELETE FROM sales WHERE sale_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    public Sale getSaleById(int saleId) {
        String sql = "SELECT sale_id, sale_date, cashier FROM sales WHERE sale_id = ?";
        Sale sale = null;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            if (results.next()) {
                sale = new Sale(results.getInt("sale_id"), results.getDate("sale_date").toLocalDate(), null);
            }
        } catch (SQLException e) {
           // e.printStackTrace();
        }
        return sale;
    }

    public List<Sale> getAllSales() {
        String sql = "SELECT sale_id, sale_date, cashier FROM sales ORDER BY sale_id";
        List<Sale> sales = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Sale sale = new Sale(results.getInt("sale_id"), results.getDate("sale_date").toLocalDate(), null);
                sales.add(sale);
            }
        } catch (SQLException e) {
           // e.printStackTrace();
        }
        return sales;
    }

    public int getSalesCount() {
        String sql = "SELECT COUNT(*) FROM sales";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            return results.next() ? results.getInt(1) : 0;
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load sales count.", exception);
        }
    }

    // concerning products of a sale
    public void insertProductIntoSale(int saleId, Product product) {
        String sql = "INSERT INTO sale_lines(sale_id, product) VALUES (?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.setInt(2, product.getReference());
            ps.executeUpdate();
        } catch (SQLException e) {
           // e.printStackTrace();
        }
    }

    public void removeProductFromSale(int saleId, Product product) {
        String sql = "DELETE FROM sale_lines WHERE sale_id = ? AND product = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.setInt(2, product.getReference());
            ps.executeUpdate();
        } catch (SQLException e) {
            //e.printStackTrace();
        }
    }

    public List<Product> getProductsFromSale(int saleId) {
        String sql = "SELECT product FROM sale_lines WHERE sale_id = ?";
        List<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, saleId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Product product = new Product(
                            results.getInt("product"),
                            "",
                            0.0,
                            0.0,
                            0);
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            //e.printStackTrace();
        }
        return products;
    }
}
