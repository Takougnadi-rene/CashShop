package com.cash_shop.sale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cash_shop.common.DBConnection;
import com.cash_shop.customer.Customer;
import com.cash_shop.payment.Payment;
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

    public int persistCompletedSale(Sale sale, Customer customer, Payment.PaymentMode paymentMode) {
        if (sale == null || sale.getProductsList() == null || sale.getProductsList().isEmpty()) {
            throw new IllegalArgumentException("A completed sale must contain at least one product.");
        }
        if (paymentMode == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            int originalIsolation = connection.getTransactionIsolation();
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            try {
                int billNumber = nextId(connection, "bills", "bill_number");
                int paymentNumber = nextId(connection, "payments", "payment_number");

                String saleSql = "INSERT INTO sales(sale_date, cashier) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(saleSql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setDate(1, java.sql.Date.valueOf(sale.getSaleDate()));
                    if (sale.getCashier() == null) {
                        statement.setNull(2, Types.VARCHAR);
                    } else {
                        statement.setString(2, sale.getCashier().getMatricule());
                    }
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Database did not return a sale ID.");
                        }
                        sale.setSaleId(keys.getInt(1));
                    }
                }
                int saleId = sale.getSaleId();

                Map<Integer, Integer> quantities = new LinkedHashMap<>();
                String lineSql = "INSERT INTO sale_lines(sale_id, product) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(lineSql)) {
                    for (Product product : sale.getProductsList()) {
                        quantities.merge(product.getReference(), 1, Integer::sum);
                        statement.setInt(1, saleId);
                        statement.setInt(2, product.getReference());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }

                String stockSql = "UPDATE products SET stock_quantity = stock_quantity - ? "
                        + "WHERE reference = ? AND stock_quantity >= ?";
                try (PreparedStatement statement = connection.prepareStatement(stockSql)) {
                    for (Map.Entry<Integer, Integer> entry : quantities.entrySet()) {
                        int quantity = entry.getValue();
                        statement.setInt(1, quantity);
                        statement.setInt(2, entry.getKey());
                        statement.setInt(3, quantity);
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("Insufficient stock for product " + entry.getKey());
                        }
                    }
                }

                double total = sale.getProductsList().stream().mapToDouble(Product::getSellingPrice).sum();
                String paymentSql = "INSERT INTO payments(payment_number, amount, payment_mode, payment_date, sale) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(paymentSql)) {
                    statement.setInt(1, paymentNumber);
                    statement.setDouble(2, total);
                        statement.setString(3, paymentMode == Payment.PaymentMode.MOBILE_PAYMENT
                            ? "MOBILE_MONEY" : paymentMode.name());
                    statement.setDate(4, java.sql.Date.valueOf(sale.getSaleDate()));
                    statement.setInt(5, saleId);
                    statement.executeUpdate();
                }

                String billSql = "INSERT INTO bills(bill_number, bill_date, customer, cashier, sale) "
                    + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(billSql)) {
                    statement.setInt(1, billNumber);
                    statement.setDate(2, java.sql.Date.valueOf(sale.getSaleDate()));
                    if (customer == null) {
                        statement.setNull(3, Types.INTEGER);
                    } else {
                        statement.setInt(3, customer.getCustomerId());
                    }
                    if (sale.getCashier() == null) {
                        statement.setNull(4, Types.VARCHAR);
                    } else {
                        statement.setString(4, sale.getCashier().getMatricule());
                    }
                    statement.setInt(5, saleId);
                    statement.executeUpdate();
                }

                if (customer != null) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE customers SET total_spent = total_spent + ? WHERE customer_id = ?")) {
                        statement.setDouble(1, total);
                        statement.setInt(2, customer.getCustomerId());
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("Customer record no longer exists.");
                        }
                    }
                }

                connection.commit();
                return billNumber;
            } catch (SQLException | RuntimeException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            } finally {
                connection.setTransactionIsolation(originalIsolation);
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to save the sale. No sale, payment, or bill was recorded: "
                    + exception.getMessage(), exception);
        }
    }

    private int nextId(Connection connection, String table, String column) throws SQLException {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) + 1 FROM " + table;
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            if (!results.next()) {
                throw new SQLException("Unable to allocate an identifier for " + table);
            }
            return results.getInt(1);
        }
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
