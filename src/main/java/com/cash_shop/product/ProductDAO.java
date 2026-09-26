package com.cash_shop.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cash_shop.common.DBConnection;

public class ProductDAO {

    public void insertProduct(Product product) {
        String sql = "INSERT INTO products (reference, designation, purchase_price, selling_price, stock_quantity) VALUES (?, ?, ?, ?, ?)";
        execute(sql, statement -> {
            statement.setInt(1, product.getReference());
            statement.setString(2, product.getDesignation());
            statement.setDouble(3, product.getPurchasePrice());
            statement.setDouble(4, product.getSellingPrice());
            statement.setInt(5, product.getStockQuantity());
        });
    }

    public void insertElectronicProduct(ElectronicProduct product) {
        String sql = "INSERT INTO electronic_products (reference, brand, warranty) VALUES (?, ?, ?)";
        execute(sql, statement -> {
            statement.setInt(1, product.getReference());
            statement.setString(2, product.getBrand());
            statement.setInt(3, product.getWarranty());
        });
    }

    public void insertFreshProduct(FreshProduct product) {
        String sql = "INSERT INTO fresh_products (reference, expiration_date, storage_temperature) VALUES (?, ?, ?)";
        execute(sql, statement -> {
            statement.setInt(1, product.getReference());
            statement.setString(2, product.getExpirationDate());
            statement.setDouble(3, product.getStorageTemperature());
        });
    }

    public void insertArtisanalProduct(ArtisanalProduct product) {
        String sql = "INSERT INTO artisanal_products (reference, type) VALUES (?,?)";
        execute(sql, statement -> {
            statement.setInt(1, product.getReference());
            statement.setString(2, product.getType().toString());
        });
    }

    public void updateProduct(Product product) {
        String sql = "UPDATE products SET designation = ?, purchase_price = ?, "
                + "selling_price = ?, stock_quantity = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getDesignation());
            statement.setDouble(2, product.getPurchasePrice());
            statement.setDouble(3, product.getSellingPrice());
            statement.setInt(4, product.getStockQuantity());
            statement.setInt(5, product.getReference());
        });
    }

    public void updateElectronicProduct(ElectronicProduct product) {
        String sql = "UPDATE electronic_products SET brand = ?, warranty = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getBrand());
            statement.setInt(2, product.getWarranty());
            statement.setInt(3, product.getReference());
        });
    }

    public void updateFreshProduct(FreshProduct product) {
        String sql = "UPDATE fresh_products SET expiration_date = ?, storage_temperature = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getExpirationDate());
            statement.setDouble(2, product.getStorageTemperature());
            statement.setInt(3, product.getReference());
        });
    }

    public void updateArtisanalProduct(ArtisanalProduct product) {
        String sql = "UPDATE artisanal_products SET type = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getType());
            statement.setInt(2, product.getReference());
        });
    }

    public void deleteProduct(Product product) {
        String sql = "DELETE FROM products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }

    public void deleteElectronicProduct(ElectronicProduct product) {
        String sql = "DELETE FROM electronic_products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }

    public void deleteFreshProduct(FreshProduct product) {
        String sql = "DELETE FROM fresh_products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }

    public void deleteArtisanalProduct(ArtisanalProduct product) {
        String sql = "DELETE FROM artisanal_products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }

    /*
     * public void addStockQuantity(int reference, int stockQuantityToAdd) {
     * String sql = "UPDATE products SET stock_quantity = ? WHERE reference = ?";
     * execute(sql, statement -> {
     * int currentStock = getStockQuantity(reference);
     * statement.setInt(1, currentStock + stockQuantityToAdd);
     * statement.setInt(2, reference);
     * if (stockQuantityToAdd < 0) {
     * throw new IllegalStateException("Stock quantity cannot be negative.");
     * }
     * });
     * }
     * 
     * public void removeStockQuantity(int reference, int stockQuantityToRemove) {
     * String sql = "UPDATE products SET stock_quantity = ? WHERE reference = ?";
     * execute(sql, statement -> {
     * int currentStock = getStockQuantity(reference);
     * statement.setInt(1, currentStock - stockQuantityToRemove);
     * statement.setInt(2, reference);
     * if (stockQuantityToRemove < 0) {
     * throw new IllegalStateException("Stock quantity cannot be negative.");
     * }
     * });
     * }
     * 
     */

    public void setSellingPrice(int reference, double sellingPrice) {
        String sql = "UPDATE products SET selling_price = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setDouble(1, sellingPrice);
            statement.setInt(2, reference);
        });
    }

    // get all products
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                products.add(new Product(
                        resultSet.getInt("reference"),
                        resultSet.getString("designation"),
                        resultSet.getDouble("purchase_price"),
                        resultSet.getDouble("selling_price"),
                        resultSet.getInt("stock_quantity")));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", exception);
        }
        return products;
    }

    public ArrayList<Product> getProductById(int reference) {
        String sql = "SELECT * FROM products WHERE reference = ?";
        ArrayList<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reference);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    products.add(new Product(
                            resultSet.getInt("reference"),
                            resultSet.getString("designation"),
                            resultSet.getDouble("purchase_price"),
                            resultSet.getDouble("selling_price"),
                            resultSet.getInt("stock_quantity")));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", exception);
        }
        throw new IllegalArgumentException("Product not found: " + reference);
    }

    public ArrayList<Product> getProductsByName(String name) {
        String sql = "SELECT * FROM products WHERE designation LIKE ?";
        ArrayList<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + name + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    products.add(new Product(
                            resultSet.getInt("reference"),
                            resultSet.getString("designation"),
                            resultSet.getDouble("purchase_price"),
                            resultSet.getDouble("selling_price"),
                            resultSet.getInt("stock_quantity")));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Impossible d'acceder a la base de donnees.", exception);
        }
        return products;
    }

    /*
     * private int getStockQuantity(int reference) {
     * String sql = "SELECT stock_quantity FROM products WHERE reference = ?";
     * try (Connection connection = DBConnection.getConnection();
     * PreparedStatement statement = connection.prepareStatement(sql)) {
     * statement.setInt(1, reference);
     * try (ResultSet resultSet = statement.executeQuery()) {
     * if (resultSet.next()) {
     * return resultSet.getInt("stock_quantity");
     * }
     * }
     * } catch (SQLException exception) {
     * throw new IllegalStateException("Impossible d'acceder a la base de donnees.",
     * exception);
     * }
     * throw new IllegalArgumentException("Product not found: " + reference);
     * }
     */
    public void addQuantity(int reference, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setInt(1, quantity);
            statement.setInt(2, reference);
        });
    }

    public void removeQuantity(int reference, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setInt(1, quantity);
            statement.setInt(2, reference);
        });
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
