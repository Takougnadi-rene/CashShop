package com.cash_shop.supplier;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.cash_shop.common.DBConnection;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.product.Product;

public class SupplierDAO {

    public void initializeSchema() {
        String createSuppliers = "CREATE TABLE IF NOT EXISTS suppliers ("
                + "code INT PRIMARY KEY, supplier_name VARCHAR(150) NOT NULL, "
                + "telephone VARCHAR(50) NOT NULL, address VARCHAR(100) NOT NULL) ENGINE=InnoDB";
        String createOrders = "CREATE TABLE IF NOT EXISTS supplier_orders ("
                + "order_number INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "supplier_code INT NULL, order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', total_amount DECIMAL(14,2) NOT NULL DEFAULT 0, "
                + "CONSTRAINT fk_supplier_order_supplier FOREIGN KEY (supplier_code) "
                + "REFERENCES suppliers(code)) ENGINE=InnoDB";
        String createOrderLines = "CREATE TABLE IF NOT EXISTS supplier_order_lines ("
                + "order_number INT NOT NULL, reference INT NOT NULL, quantity INT NOT NULL, "
                + "unit_purchase_price DECIMAL(14,2) NOT NULL, PRIMARY KEY (order_number, reference), "
                + "CONSTRAINT fk_supplier_line_order FOREIGN KEY (order_number) "
                + "REFERENCES supplier_orders(order_number), "
                + "CONSTRAINT fk_supplier_line_product FOREIGN KEY (reference) "
                + "REFERENCES products(reference)) ENGINE=InnoDB";
        try (Connection connection = DBConnection.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(createSuppliers);
            statement.executeUpdate(createOrders);
            ensureColumn(connection, "supplier_orders", "supplier_code", "INT NULL");
            ensureColumn(connection, "supplier_orders", "total_amount", "DECIMAL(14,2) NOT NULL DEFAULT 0");
            statement.executeUpdate("ALTER TABLE supplier_orders MODIFY status "
                    + "VARCHAR(20) NOT NULL DEFAULT 'PENDING'");
            statement.executeUpdate(createOrderLines);
            seedInitialSuppliers(connection);
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    private void ensureColumn(Connection connection, String table, String column, String definition)
            throws SQLException {
        String lookup = "SELECT COUNT(*) FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(lookup)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                if (results.getInt(1) == 0) {
                    try (Statement alter = connection.createStatement()) {
                        alter.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                    }
                }
            }
        }
    }

    private void seedInitialSuppliers(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM suppliers")) {
            results.next();
            if (results.getInt(1) > 0) {
                return;
            }
        }
        String sql = "INSERT INTO suppliers (code, supplier_name, telephone, address) VALUES (?, ?, ?, ?)";
        Object[][] initialSuppliers = {
                { 1, "Achat Plus", "+221 77 000 00 00", "Dakar" },
                { 2, "Fresh Supply", "+221 77 111 11 11", "Thies" },
                { 3, "TechMarket", "+221 77 222 22 22", "Saint-Louis" }
        };
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Object[] supplier : initialSuppliers) {
                statement.setInt(1, (Integer) supplier[0]);
                statement.setString(2, (String) supplier[1]);
                statement.setString(3, (String) supplier[2]);
                statement.setString(4, (String) supplier[3]);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
    
    public List<Supplier> getSuppliers() {
        String sql = "SELECT code, supplier_name, telephone, address FROM suppliers ORDER BY supplier_name";
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                suppliers.add(new Supplier(results.getInt("code"), results.getString("supplier_name"),
                        results.getString("telephone"), results.getString("address")));
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
        return suppliers;
    }

    public void addSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (code, supplier_name, telephone, address) VALUES (?, ?, ?, ?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplier.getCode());
            statement.setString(2, supplier.getName());
            statement.setString(3, supplier.getTelephone());
            statement.setString(4, supplier.getAddress());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    public void deleteSupplier(int supplierCode) {
        String sql = "DELETE FROM suppliers WHERE code = ?";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierCode);
            if (statement.executeUpdate() == 0) {
                throw new IllegalStateException("Supplier not found.");
            }
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 1451) {
                throw new IllegalStateException("A supplier with orders cannot be deleted.", exception);
            }
            throw databaseError(exception);
        }
    }

    public List<SupplierOrder> getOrders(int supplierCode) {
        String sql = "SELECT o.order_number, o.order_date, o.supplier_code, s.supplier_name, "
                + "o.total_amount, o.status FROM supplier_orders o "
            + "JOIN suppliers s ON s.code = o.supplier_code "
                + "WHERE o.supplier_code = ? ORDER BY o.order_date DESC, o.order_number DESC";
        List<SupplierOrder> orders = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, supplierCode);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Timestamp date = results.getTimestamp("order_date");
                    orders.add(new SupplierOrder(results.getInt("order_number"), date,
                            results.getInt("supplier_code"), results.getString("supplier_name"),
                            results.getBigDecimal("total_amount"),
                            parseStatus(results.getString("status"))));
                }
            }
        } catch (SQLException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to load supplier orders.", exception);
        }
        return orders;
    }

    public List<Product> getOrderLines(int orderNumber) {
        String sql = "SELECT l.reference, p.designation, l.quantity, l.unit_purchase_price "
                + "FROM supplier_order_lines l JOIN products p ON p.reference = l.reference "
                + "WHERE l.order_number = ? ORDER BY p.designation";
        List<Product> lines = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderNumber);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                        lines.add(new Product(results.getInt("reference"), results.getString("designation"),
                            results.getBigDecimal("unit_purchase_price").doubleValue(), 0,
                            results.getInt("quantity")));
                }
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
        return lines;
    }

    public int createOrder(int supplierCode, Role actor) {
        requireOrderCreator(actor);
        String sql = "INSERT INTO supplier_orders (supplier_code) VALUES (?)";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, supplierCode);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new IllegalStateException("Unable to create supplier order.");
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    public void addOrderLine(int orderNumber, int productReference, int quantity, Role actor) {
        requireOrderCreator(actor);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requirePendingOrder(connection, orderNumber);
                BigDecimal purchasePrice = getPurchasePrice(connection, productReference);
                String sql = "INSERT INTO supplier_order_lines "
                        + "(order_number, reference, quantity, unit_purchase_price) VALUES (?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, orderNumber);
                    statement.setInt(2, productReference);
                    statement.setInt(3, quantity);
                    statement.setBigDecimal(4, purchasePrice);
                    statement.executeUpdate();
                }
                updateOrderTotal(connection, orderNumber);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    public void removeOrderLine(int orderNumber, int productReference, Role actor) {
        requireOrderCreator(actor);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requirePendingOrder(connection, orderNumber);
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM supplier_order_lines WHERE order_number = ? AND reference = ?")) {
                    statement.setInt(1, orderNumber);
                    statement.setInt(2, productReference);
                    if (statement.executeUpdate() == 0) {
                        throw new IllegalStateException("Order line not found.");
                    }
                }
                updateOrderTotal(connection, orderNumber);
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    public void updateOrderStatus(int orderNumber, SupplierOrder.OrderStatus status, Role actor) {
        if (status == null) {
            throw new IllegalArgumentException("Order status is required.");
        }
        if (status == SupplierOrder.OrderStatus.APPROUVED || status == SupplierOrder.OrderStatus.DENIDED) {
            requireOrderReviewer(actor);
            reviewOrder(orderNumber, status);
            return;
        }
        if (status == SupplierOrder.OrderStatus.DELIVERED) {
            requireOrderCreator(actor);
            markOrderDelivered(orderNumber);
            return;
        }
        throw new IllegalArgumentException("An order cannot be reset to pending.");
    }

    private void reviewOrder(int orderNumber, SupplierOrder.OrderStatus status) {
        String sql = "UPDATE supplier_orders SET status = ? WHERE order_number = ? AND status = 'PENDING'";
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setInt(2, orderNumber);
            if (statement.executeUpdate() != 1) {
                throw new IllegalStateException("Only pending orders can be approved or refused.");
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    private void markOrderDelivered(int orderNumber) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                requireOrderStatus(connection, orderNumber, SupplierOrder.OrderStatus.APPROUVED);
                List<Product> lines = getOrderLinesForUpdate(connection, orderNumber);
                if (lines.isEmpty()) {
                    throw new IllegalStateException("An approved order must contain at least one product.");
                }
                String updateStock = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE reference = ?";
                try (PreparedStatement statement = connection.prepareStatement(updateStock)) {
                    for (Product line : lines) {
                        statement.setInt(1, line.getStockQuantity());
                        statement.setInt(2, line.getReference());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE supplier_orders SET status = 'DELIVERED' "
                        + "WHERE order_number = ? AND status = 'APPROUVED'")) {
                    statement.setInt(1, orderNumber);
                    if (statement.executeUpdate() != 1) {
                    throw new IllegalStateException("Only approved orders can be marked as delivered.");
                    }
                }
                connection.commit();
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    private void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }

    private void requirePendingOrder(Connection connection, int orderNumber) throws SQLException {
        requireOrderStatus(connection, orderNumber, SupplierOrder.OrderStatus.PENDING);
    }

    private void requireOrderStatus(Connection connection, int orderNumber, SupplierOrder.OrderStatus expected)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT status FROM supplier_orders WHERE order_number = ? FOR UPDATE")) {
            statement.setInt(1, orderNumber);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next() || parseStatus(results.getString("status")) != expected) {
                    throw new IllegalStateException("Order must have status " + expected + ".");
                }
            }
        }
    }

    private void requireOrderCreator(Role actor) {
        if (actor != Role.STOREKEEPER && actor != Role.MANAGER) {
            throw new SecurityException("Only a storekeeper can create or edit supplier orders.");
        }
    }

    private void requireOrderReviewer(Role actor) {
        if (actor != Role.ACCOUNTANT) {
            throw new SecurityException("Only an accountant can approve or refuse supplier orders.");
        }
    }

    private SupplierOrder.OrderStatus parseStatus(String status) {
        return switch (status.trim().toUpperCase()) {
            case "APPROVED", "APPROUVED" -> SupplierOrder.OrderStatus.APPROUVED;
            case "REFUSED", "REJECTED", "DENIDED", "CANCELLED" -> SupplierOrder.OrderStatus.DENIDED;
            case "COMPLETED", "DELIVERED" -> SupplierOrder.OrderStatus.DELIVERED;
            default -> SupplierOrder.OrderStatus.valueOf(status.trim().toUpperCase());
        };
    }

    private BigDecimal getPurchasePrice(Connection connection, int productReference) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT purchase_price FROM products WHERE reference = ?")) {
            statement.setInt(1, productReference);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new IllegalArgumentException("Product not found.");
                }
                return results.getBigDecimal("purchase_price");
            }
        }
    }

    private List<Product> getOrderLinesForUpdate(Connection connection, int orderNumber)
            throws SQLException {
        String sql = "SELECT reference, quantity, unit_purchase_price FROM supplier_order_lines "
                + "WHERE order_number = ? FOR UPDATE";
        List<Product> lines = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderNumber);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                        lines.add(new Product(results.getInt("reference"), "",
                            results.getBigDecimal("unit_purchase_price").doubleValue(), 0,
                            results.getInt("quantity")));
                }
            }
        }
        return lines;
    }

    private void updateOrderTotal(Connection connection, int orderNumber) throws SQLException {
        String sql = "UPDATE supplier_orders SET total_amount = (SELECT COALESCE(SUM(quantity * unit_purchase_price), 0) "
                + "FROM supplier_order_lines WHERE order_number = ?) WHERE order_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderNumber);
            statement.setInt(2, orderNumber);
            statement.executeUpdate();
        }
    }

    private IllegalStateException databaseError(SQLException exception) {
        return new IllegalStateException("Unable to access supplier data: " + exception.getMessage(), exception);
    }
}
