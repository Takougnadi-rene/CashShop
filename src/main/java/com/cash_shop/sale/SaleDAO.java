package com.cash_shop.sale;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.cash_shop.common.DBConnection;
import com.cash_shop.customer.Customer;
import com.cash_shop.payment.Payment;
import com.cash_shop.product.Product;

/** Data access object for sales, sale lines, summaries and the transactional saving of a completed sale. */
public class SaleDAO {

    private static final Logger LOGGER = Logger.getLogger(SaleDAO.class.getName());

    /** Inserts a sale. */
    public void insertSale(Sale sale) {
        String sql = "INSERT INTO sales(sale_id, sale_date, cashier) VALUES (?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, sale.getSaleId());
            ps.setTimestamp(2, Timestamp.valueOf(sale.getSaleDate()));
            ps.setString(3, sale.getCashier().getMatricule());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database operation failed.", e);
        }
    }

    /** Updates date and cashier of a sale. */
    public void updateSale(Sale sale) {
        String sql = "UPDATE sales SET sale_date = ?, cashier = ? WHERE sale_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(sale.getSaleDate()));
            ps.setString(2, sale.getCashier().getMatricule());
            ps.setInt(3, sale.getSaleId());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database operation failed.", e);
        }
    }

    /** Deletes a sale. */
    public void deleteSale(int saleId) {
        String sql = "DELETE FROM sales WHERE sale_id = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database operation failed.", e);
        }
    }

    /** Returns the sale with the given id, or null when it does not exist. */
    public Sale getSaleById(int saleId) {
        String sql = "SELECT sale_id, sale_date, cashier FROM sales WHERE sale_id = ?";
        Sale sale = null;
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, saleId);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    sale = new Sale(results.getInt("sale_id"), results.getTimestamp("sale_date").toLocalDateTime(), null);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database operation failed.", e);
        }
        return sale;
    }

    /** Returns all sales ordered by id. */
    public List<Sale> getAllSales() {
        String sql = "SELECT sale_id, sale_date, cashier FROM sales ORDER BY sale_id";
        List<Sale> sales = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                Sale sale = new Sale(results.getInt("sale_id"), results.getTimestamp("sale_date").toLocalDateTime(), null);
                sales.add(sale);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database operation failed.", e);
        }
        return sales;
    }

    /** Returns the summary of every sale (no filter). */
    public List<SaleSummary> getSaleSummaries() {
        return getSaleSummaries(null, null, null);
    }

    /**
     * Returns sale summaries (customer, cashier, total paid), optionally filtered by day, customer and
     * cashier.
     */
    public List<SaleSummary> getSaleSummaries(LocalDate saleDate, Integer customerId, String cashierMatricule) {
        // Total paid per sale; walk-in customers and unknown cashiers get a default label. Filters are appended
        // only when used.
        String sql = "SELECT s.sale_id, COALESCE(c.name, 'Walk-in customer') AS customer_name, "
                + "COALESCE(CONCAT(e.first_name, ' ', e.last_name), 'Unknown cashier') AS cashier_name, "
                + "COALESCE(SUM(p.amount), 0) AS total_amount "
                + "FROM sales s "
                + "LEFT JOIN bills b ON b.sale = s.sale_id "
                + "LEFT JOIN customers c ON c.customer_id = b.customer "
                + "LEFT JOIN employees e ON e.matricule = COALESCE(b.cashier, s.cashier) "
                + "LEFT JOIN payments p ON p.sale = s.sale_id "
                + "WHERE 1 = 1";
        if (saleDate != null) {
            sql += " AND s.sale_date >= ? AND s.sale_date < ?";
        }
        if (customerId != null) {
            sql += " AND b.customer = ?";
        }
        if (cashierMatricule != null && !cashierMatricule.isBlank()) {
            sql += " AND COALESCE(b.cashier, s.cashier) = ?";
        }
        sql += " GROUP BY s.sale_id, c.name, e.first_name, e.last_name ORDER BY s.sale_id DESC";
        List<SaleSummary> summaries = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {
            // Bind the optional filters in the same order as they were appended to the SQL.
            int parameter = 1;
            if (saleDate != null) {
                statement.setTimestamp(parameter++, Timestamp.valueOf(saleDate.atStartOfDay()));
                statement.setTimestamp(parameter++, Timestamp.valueOf(saleDate.plusDays(1).atStartOfDay()));
            }
            if (customerId != null) {
                statement.setInt(parameter++, customerId);
            }
            if (cashierMatricule != null && !cashierMatricule.isBlank()) {
                statement.setString(parameter, cashierMatricule);
            }
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    summaries.add(new SaleSummary(results.getInt("sale_id"), results.getDate("sale_date"), results.getString("customer_name"),
                            results.getString("cashier_name"), results.getBigDecimal("total_amount")));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to load sales history.", exception);
        }
        return summaries;
    }

    /**
     * Saves a completed sale atomically: sale, sale lines, stock decrease, payment, bill and customer total.
     * If anything fails, everything is rolled back. Returns the new bill number.
     */
    public int persistCompletedSale(Sale sale, Customer customer, Payment.PaymentMode paymentMode) {
        if (sale == null || sale.getProductsList() == null || sale.getProductsList().isEmpty()) {
            throw new IllegalArgumentException("A completed sale must contain at least one product.");
        }
        if (paymentMode == null) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        // Use a single timestamp for the sale, the payment and the bill so that they match exactly.
        LocalDateTime validationTime = LocalDateTime.now();
        Timestamp validationTimestamp = Timestamp.valueOf(validationTime);
        sale.setSaleDate(validationTime);

        try (Connection connection = DBConnection.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            int originalIsolation = connection.getTransactionIsolation();
            // Serializable isolation + manual commit: the whole sale is saved atomically and concurrent
            // registers cannot get the same numbers.
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            connection.setAutoCommit(false);
            try {
                // Bill and payment numbers are not auto-increment columns: compute the next free numbers.
                int billNumber = nextId(connection, "bills", "bill_number");
                int paymentNumber = nextId(connection, "payments", "payment_number");

                // 1) Insert the sale and read its generated id.
                String saleSql = "INSERT INTO sales(sale_date, cashier) VALUES (?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(saleSql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    statement.setTimestamp(1, validationTimestamp);
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

                // 2) Insert one sale line per unit sold, and count the quantity sold of each product.
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

                // 3) Decrease the stock. The "stock_quantity >= ?" condition prevents negative stock if another
                // register sold the same product meanwhile.
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

                // 4) Insert the payment (the database uses MOBILE_MONEY for the MOBILE_PAYMENT mode).
                double total = sale.getProductsList().stream().mapToDouble(Product::getSellingPrice).sum();
                String paymentSql = "INSERT INTO payments(payment_number, amount, payment_mode, payment_date, sale) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(paymentSql)) {
                    statement.setInt(1, paymentNumber);
                    statement.setDouble(2, total);
                        statement.setString(3, paymentMode == Payment.PaymentMode.MOBILE_PAYMENT
                            ? "MOBILE_MONEY" : paymentMode.name());
                    statement.setTimestamp(4, validationTimestamp);
                    statement.setInt(5, saleId);
                    statement.executeUpdate();
                }

                // 5) Insert the bill (customer and cashier are optional).
                String billSql = "INSERT INTO bills(bill_number, bill_date, customer, cashier, sale) "
                    + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(billSql)) {
                    statement.setInt(1, billNumber);
                    statement.setTimestamp(2, validationTimestamp);
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

                // 6) Add the amount to the total spent by the customer.
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

                // Everything succeeded: make the changes permanent.
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

    /** Returns the next free value (MAX + 1) of an id column. */
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

    /** Returns the number of sales. */
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
            LOGGER.log(Level.WARNING, "Database operation failed.", e);
        }
    }

    /** Removes the sale lines of a product from a sale. */
    public void removeProductFromSale(int saleId, Product product) {
        String sql = "DELETE FROM sale_lines WHERE sale_id = ? AND product = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, saleId);
            ps.setInt(2, product.getReference());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database operation failed.", e);
        }
    }

    /** Returns the products of a sale (reference only: the other fields are left empty). */
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
            LOGGER.log(Level.WARNING, "Database operation failed.", e);
        }
        return products;
    }
}
