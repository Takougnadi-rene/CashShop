package com.cash_shop.product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.cash_shop.common.DBConnection;

/**
 * Data access object for products. The base data lives in {@code products}; each specialised type has its own
 * table (electronic_products, fresh_products, artisanal_products). Reads go through the {@code all_products}
 * view.
 */
public class ProductDAO {

    /** Inserts the common data of a product. */
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

    /** Inserts the electronic-specific data (brand, warranty). */
    public void insertElectronicProduct(ElectronicProduct product) {
        String sql = "INSERT INTO electronic_products (reference, brand, warranty) VALUES (?, ?, ?)";
        execute(sql, statement -> {
            statement.setInt(1, product.getReference());
            statement.setString(2, product.getBrand());
            statement.setInt(3, product.getWarranty());
        });
    }

    /** Inserts the fresh-specific data (expiration date, storage temperature). */
    public void insertFreshProduct(FreshProduct product) {
        String sql = "INSERT INTO fresh_products (reference, expiration_date, storage_temperature) VALUES (?, ?, ?)";
        execute(sql, statement -> {
            statement.setInt(1, product.getReference());
            statement.setString(2, product.getExpirationDate());
            statement.setDouble(3, product.getStorageTemperature());
        });
    }

    /** Inserts the artisanal type. */
    public void insertArtisanalProduct(ArtisanalProduct product) {
        String sql = "INSERT INTO artisanal_products (reference, type) VALUES (?,?)";
        execute(sql, statement -> {
            statement.setInt(1, product.getReference());
            statement.setString(2, product.getType());
        });
    }

    /** Updates the common data of a product. */
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

    /** Updates the electronic-specific data. */
    public void updateElectronicProduct(ElectronicProduct product) {
        String sql = "UPDATE electronic_products SET brand = ?, warranty = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getBrand());
            statement.setInt(2, product.getWarranty());
            statement.setInt(3, product.getReference());
        });
    }

    /** Updates the fresh-specific data. */
    public void updateFreshProduct(FreshProduct product) {
        String sql = "UPDATE fresh_products SET expiration_date = ?, storage_temperature = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getExpirationDate());
            statement.setDouble(2, product.getStorageTemperature());
            statement.setInt(3, product.getReference());
        });
    }

    /** Updates the artisanal type. */
    public void updateArtisanalProduct(ArtisanalProduct product) {
        String sql = "UPDATE artisanal_products SET type = ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setString(1, product.getType());
            statement.setInt(2, product.getReference());
        });
    }

    /** Deletes the product row. */
    public void deleteProduct(Product product) {
        String sql = "DELETE FROM products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }

    /** Deletes the electronic-specific row. */
    public void deleteElectronicProduct(ElectronicProduct product) {
        String sql = "DELETE FROM electronic_products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }

    /** Deletes the fresh-specific row. */
    public void deleteFreshProduct(FreshProduct product) {
        String sql = "DELETE FROM fresh_products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }

    /** Deletes the artisanal-specific row. */
    public void deleteArtisanalProduct(ArtisanalProduct product) {
        String sql = "DELETE FROM artisanal_products WHERE reference = ?";
        execute(sql, statement -> statement.setInt(1, product.getReference()));
    }

    /** Changes only the selling price of a product. */
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
        String sql = "SELECT reference, designation, purchase_price, selling_price, stock_quantity, "
            + "electronic_brand, warranty, expiration_date, storage_temperature, artisanal_type "
            + "FROM all_products ORDER BY reference";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                    products.add(mapProduct(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to access the database.", exception);
        }
        return products;
    }

    /** Converts the current row of the {@code all_products} view into the right Product subclass. */
    private Product mapProduct(ResultSet resultSet) throws SQLException {
        int reference = resultSet.getInt("reference");
        String designation = resultSet.getString("designation");
        double purchasePrice = resultSet.getDouble("purchase_price");
        double sellingPrice = resultSet.getDouble("selling_price");
        int stockQuantity = resultSet.getInt("stock_quantity");
        String expirationDate = resultSet.getString("expiration_date");
        String artisanalType = resultSet.getString("artisanal_type");
        if (resultSet.getObject("warranty") != null) {
            return new ElectronicProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                    resultSet.getString("electronic_brand"), resultSet.getInt("warranty"));
        }
        if (expirationDate != null) {
            return new FreshProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                    expirationDate, resultSet.getDouble("storage_temperature"));
        }
        if (artisanalType != null) {
            return new ArtisanalProduct(reference, designation, purchasePrice, sellingPrice, stockQuantity,
                    ArtisanalProduct.TypeArtisanal.from(artisanalType));
        }
        return new Product(reference, designation, purchasePrice, sellingPrice, stockQuantity);
    }

    /**
     * Returns the product with the given reference; throws IllegalArgumentException when it does not exist.
     */
    
    public ArrayList<Product> getProductById(int reference) {
        String sql = "SELECT * FROM all_products WHERE reference = ?";
        ArrayList<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reference);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                        products.add(mapProduct(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to access the database.", exception);
        }
        if (products.isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + reference);
        }
        return products;
    }

    /** Returns the products whose designation contains the given text. */
    public ArrayList<Product> getProductsByName(String name) {
        String sql = "SELECT * FROM all_products WHERE designation LIKE ?";
        ArrayList<Product> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + name + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                        products.add(mapProduct(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to access the database.", exception);
        }
        return products;
    }

    /** Increases the stock of a product. */
    public void addQuantity(int reference, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setInt(1, quantity);
            statement.setInt(2, reference);
        });
    }

    /** Decreases the stock of a product. */
    public void removeQuantity(int reference, int quantity) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE reference = ?";
        execute(sql, statement -> {
            statement.setInt(1, quantity);
            statement.setInt(2, reference);
        });
    }

    /** Runs an INSERT/UPDATE/DELETE statement after binding its parameters. */
    private void execute(String sql, StatementParameters parameters) {
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            parameters.set(statement);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to access the database.", exception);
        }
    }

    /** Callback that binds the parameters of a prepared statement. */
    @FunctionalInterface
    private interface StatementParameters {
        void set(PreparedStatement statement) throws SQLException;
    }
}
