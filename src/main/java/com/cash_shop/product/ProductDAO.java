package com.cash_shop.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.cash_shop.common.DBConnection;

public class ProductDAO {
    public void addProduct(Product product) {
        String sql = "INSERT INTO products (reference, designation, buying_price, selling_price, stock_quantity) VALUES (?, ?, ?, ?, ?)";
        executeProductUpdate(sql, product);
    }
    public void addElectronicProduct(ElectronicProduct product) {
        String sql = "INSERT INTO electronic_products "
                + "(reference, designation, buying_price, selling_price, stock_quantity, warranty_period) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        execute(sql, statement -> {
            setCommonProductValues(statement, product);
            statement.setInt(6, product.getWarranty());
        });
    }
    
    public void updateProduct(Product product) {
        String sql = "UPDATE products SET designation = ?, buying_price = ?, "
                + "selling_price = ?, stock_quantity = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getDesignation());
            statement.setDouble(2, product.getPurchasePrice());
            statement.setDouble(3, product.getSellingPrice());
            statement.setInt(4, product.getStockQuantity());
            statement.setInt(5, product.getReference());
        });
    }
    
    public void removeProduct(Product product) {
        String sql = "DELETE FROM products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }
    
    public void addFreshProduct(FreshProduct product) {
        String sql = "INSERT INTO fresh_products "
                + "(reference, designation, buying_price, selling_price, stock_quantity, "
                + "expiration_date, storage_temperature) VALUES (?, ?, ?, ?, ?, ?, ?)";
        execute(sql, statement -> {
            setCommonProductValues(statement, product);
            statement.setString(6, product.getExpirationDate());
            statement.setDouble(7, product.getStorageTemperature());
        });
    }
    
    public void updateFreshProduct(FreshProduct product) {
        String sql = "UPDATE fresh_products SET designation = ?, buying_price = ?, "
                + "selling_price = ?, stock_quantity = ?, expiration_date = ?, "
                + "storage_temperature = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getDesignation());
            statement.setDouble(2, product.getPurchasePrice());
            statement.setDouble(3, product.getSellingPrice());
            statement.setInt(4, product.getStockQuantity());
            statement.setString(5, product.getExpirationDate());
            statement.setDouble(6, product.getStorageTemperature());
            statement.setInt(7, product.getReference());
        });
    }
    
    public void removeFreshProduct(FreshProduct product) {
        removeByReference("fresh_products", product.getReference());
    }
    
    public void addArtisanalProduct(ArtisanalProduct product) {
        String sql = "INSERT INTO artisanal_products "
                + "(reference, designation, buying_price, selling_price, stock_quantity, type) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        execute(sql, statement -> {
            setCommonProductValues(statement, product);
            statement.setString(6, product.getType().name());
        });
    }
    
    public void updateArtisanalProduct(ArtisanalProduct product) {
        String sql = "UPDATE artisanal_products SET designation = ?, buying_price = ?, "
                + "selling_price = ?, stock_quantity = ?, type = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getDesignation());
            statement.setDouble(2, product.getPurchasePrice());
            statement.setDouble(3, product.getSellingPrice());
            statement.setInt(4, product.getStockQuantity());
            statement.setString(5, product.getType().name());
            statement.setInt(6, product.getReference());
        });
    }
    
    public void removeArtisanalProduct(ArtisanalProduct product) {
        removeByReference("artisanal_products", product.getReference());
    }
    
    private void executeProductUpdate(String sql, Product product) {
        execute(sql, statement -> setCommonProductValues(statement, product));
    }
    
    private void setCommonProductValues(PreparedStatement statement, Product product) throws SQLException {
        statement.setInt(1, product.getReference());
        statement.setString(2, product.getDesignation());
        statement.setDouble(3, product.getPurchasePrice());
        statement.setDouble(4, product.getSellingPrice());
        statement.setInt(5, product.getStockQuantity());
    }
    
    private void removeByReference(String tableName, int reference) {
        execute("DELETE FROM " + tableName + " WHERE reference = ?",
                statement -> statement.setInt(1, reference));
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
