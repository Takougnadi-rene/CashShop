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

/**
 * Data access object for suppliers and supplier orders. It also enforces who may do what: managers create,
 * edit and deliver orders, counter staff approve or refuse them. Multi-step changes run in transactions.
 */
public class SupplierDAO {

    /**
     * Creates (or upgrades) the supplier tables when needed and inserts the default suppliers if the table is
     * empty.
     */
    public void initializeSchema() {
        String createSuppliers = "CREATE TABLE IF NOT EXISTS suppliers ("
                + "code INT PRIMARY KEY, supplier_name VARCHAR(150) NOT NULL, "
                + "telephone VARCHAR(50) NOT NULL, address VARCHAR(100) NOT NULL) ENGINE=InnoDB";
        String createOrders = "CREATE TABLE IF NOT EXISTS supplier_orders ("
                + "order_number INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "supplier_code INT NULL, order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "status VARCHAR(20) NOT NULL DEFAULT 'PENDING', total_amount DECIMAL(14,2) NOT NULL DEFAULT 0, "
                + "CONSTRAINT fk_supplier_order_supplier FOREIGN KEY (supplier_code) "
                + "REFERENCES suppliers(code) ON DELETE SET NULL) ENGINE=InnoDB";
        String createOrderLines = "CREATE TABLE IF NOT EXISTS supplier_order_lines ("
                + "order_number INT NOT NULL, reference INT NOT NULL, quantity INT NOT NULL, "
                + "unit_purchase_price DECIMAL(14,2) NOT NULL, PRIMARY KEY (order_number, reference), "
                + "CONSTRAINT fk_supplier_line_order FOREIGN KEY (order_number) "
                + "REFERENCES supplier_orders(order_number) ON DELETE CASCADE, "
                + "CONSTRAINT fk_supplier_line_product FOREIGN KEY (reference) "
                + "REFERENCES products(reference)) ENGINE=InnoDB";
        try (Connection connection = DBConnection.getConnection(); Statement statement = connection.createStatement()) {
            try { statement.executeUpdate(createSuppliers); } catch (SQLException ignored) {}
            try { statement.executeUpdate(createOrders); } catch (SQLException ignored) {}
            try { ensureColumn(connection, "supplier_orders", "supplier_code", "INT NULL"); } catch (SQLException ignored) {}
            try { ensureColumn(connection, "supplier_orders", "total_amount", "DECIMAL(14,2) NOT NULL DEFAULT 0"); } catch (SQLException ignored) {}
            try {
                statement.executeUpdate("ALTER TABLE supplier_orders MODIFY status VARCHAR(20) NOT NULL DEFAULT 'PENDING'");
            } catch (SQLException ignored) {}
            try { statement.executeUpdate(createOrderLines); } catch (SQLException ignored) {}
            seedInitialSuppliers(connection);
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    /** Adds a column to a table if it does not exist yet. */
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

    /** Inserts three sample suppliers when there is none. */
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
    
    /** Returns all suppliers ordered by name. */
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

    /** Inserts a supplier. */
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

    /** Deletes a supplier; refused when it still has orders. */
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

    /** Returns the orders of every supplier. */
    public List<SupplierOrder> getAllOrders() {
        return getOrders(0);
    }

    /** Returns the orders of one supplier (or of all suppliers when the code is 0), newest first. */
    public List<SupplierOrder> getOrders(int supplierCode) {
        String sql = "SELECT o.order_number, o.order_date, o.supplier_code, "
                + "COALESCE(s.supplier_name, 'No supplier') AS supplier_name, "
                + "o.total_amount, o.status FROM supplier_orders o "
                + "LEFT JOIN suppliers s ON s.code = o.supplier_code "
                + (supplierCode > 0 ? "WHERE o.supplier_code = ? " : "")
                + "ORDER BY o.order_date DESC, o.order_number DESC";
        List<SupplierOrder> orders = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            if (supplierCode > 0) {
                statement.setInt(1, supplierCode);
            }
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Timestamp date = results.getTimestamp("order_date");
                    BigDecimal totalAmount = results.getBigDecimal("total_amount");
                    if (totalAmount == null) {
                        totalAmount = BigDecimal.ZERO;
                    }
                    orders.add(new SupplierOrder(results.getInt("order_number"), date,
                            results.getInt("supplier_code"), results.getString("supplier_name"),
                            totalAmount,
                            parseStatus(results.getString("status"))));
                }
            }
        } catch (SQLException | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to load supplier orders: " + exception.getMessage(), exception);
        }
        return orders;
    }

    /**
     * Returns the lines of an order as products (stock quantity = ordered quantity, purchase price = unit
     * price).
     */
    public List<Product> getOrderLines(int orderNumber) {
        String sql = "SELECT l.reference, COALESCE(p.designation, CONCAT('Ref #', l.reference)) AS designation, "
                + "l.quantity, l.unit_purchase_price "
                + "FROM supplier_order_lines l LEFT JOIN products p ON p.reference = l.reference "
                + "WHERE l.order_number = ? ORDER BY designation";
        List<Product> lines = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderNumber);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    BigDecimal unitPrice = results.getBigDecimal("unit_purchase_price");
                    double price = unitPrice != null ? unitPrice.doubleValue() : 0.0;
                    lines.add(new Product(results.getInt("reference"), results.getString("designation"),
                            price, 0,
                            results.getInt("quantity")));
                }
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
        return lines;
    }

    /**
     * Creates a PENDING order with its lines in one transaction (manager only) and returns the order number.
     */
    public int createOrder(int supplierCode, List<Product> items, Role actor) {
        requireOrderCreator(actor);
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("An order must contain at least one product.");
        }
        for (Product item : items) {
            if (item.getStockQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than zero for product: " + item.getDesignation());
            }
        }
        try (Connection connection = DBConnection.getConnection()) {
            // The order header and all its lines are saved in one transaction.
            connection.setAutoCommit(false);
            try {
                int orderNumber;
                String insertOrder = "INSERT INTO supplier_orders (supplier_code, status) VALUES (?, 'PENDING')";
                try (PreparedStatement statement = connection.prepareStatement(
                        insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, supplierCode);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new IllegalStateException("Unable to create supplier order.");
                        }
                        orderNumber = keys.getInt(1);
                    }
                }

                // When a line has no price yet, use the current purchase price of the product.
                String insertLine = "INSERT INTO supplier_order_lines "
                        + "(order_number, reference, quantity, unit_purchase_price) VALUES (?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";
                try (PreparedStatement statement = connection.prepareStatement(insertLine)) {
                    for (Product item : items) {
                        BigDecimal purchasePrice = BigDecimal.valueOf(item.getPurchasePrice());
                        if (purchasePrice.compareTo(BigDecimal.ZERO) <= 0) {
                            purchasePrice = getPurchasePrice(connection, item.getReference());
                        }
                        statement.setInt(1, orderNumber);
                        statement.setInt(2, item.getReference());
                        statement.setInt(3, item.getStockQuantity());
                        statement.setBigDecimal(4, purchasePrice);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                updateOrderTotal(connection, orderNumber);
                connection.commit();
                return orderNumber;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw databaseError(exception);
        }
    }

    /** Creates an order containing a single product. */
    public int createOrder(int supplierCode, int productReference, int quantity, Role actor) {
        requireOrderCreator(actor);
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        Product product = new Product(productReference, "", 0, 0, quantity);
        return createOrder(supplierCode, List.of(product), actor);
    }

    /**
     * Adds a line to a pending order (manager only); the quantity is added if the product is already in the
     * order.
     */
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

    /** Removes a line from a pending order (manager only). */
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

    /** Approves/refuses (counter) or delivers (manager) an order; an order can never go back to pending. */
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

    /** Approves or refuses a pending order. */
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

    /** Marks an approved order as delivered and adds its quantities to the stock, in one transaction. */
    private void markOrderDelivered(int orderNumber) {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Lock the order and make sure it is approved before touching the stock.
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

    /** Rolls the transaction back, keeping any rollback failure as suppressed. */
    private void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }

    /** Fails unless the order is pending. */
    private void requirePendingOrder(Connection connection, int orderNumber) throws SQLException {
        requireOrderStatus(connection, orderNumber, SupplierOrder.OrderStatus.PENDING);
    }

    /** Locks the order row and fails unless it has the expected status. */
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

    /** Only managers may create, edit or deliver orders. */
    private void requireOrderCreator(Role actor) {
        if (actor != Role.MANAGER) {
            throw new SecurityException("Only a manager can create, edit, or deliver supplier orders.");
        }
    }

    /** Only counter staff may approve or refuse orders. */
    private void requireOrderReviewer(Role actor) {
        if (actor != Role.COUNTER) {
            throw new SecurityException("Only a counter can approve or refuse supplier orders.");
        }
    }

    /**
     * Converts a stored status text to an OrderStatus, accepting legacy spellings; unknown values are treated
     * as PENDING.
     */
    private SupplierOrder.OrderStatus parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return SupplierOrder.OrderStatus.PENDING;
        }
        return switch (status.trim().toUpperCase()) {
            case "APPROVED", "APPROUVED" -> SupplierOrder.OrderStatus.APPROUVED;
            case "REFUSED", "REJECTED", "DENIDED", "CANCELLED" -> SupplierOrder.OrderStatus.DENIDED;
            case "COMPLETED", "DELIVERED" -> SupplierOrder.OrderStatus.DELIVERED;
            case "PENDING" -> SupplierOrder.OrderStatus.PENDING;
            default -> {
                try {
                    yield SupplierOrder.OrderStatus.valueOf(status.trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    yield SupplierOrder.OrderStatus.PENDING;
                }
            }
        };
    }

    /** Reads the current purchase price of a product. */
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

    /** Reads and locks the lines of an order. */
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

    /** Recomputes the total amount of an order from its lines. */
    private void updateOrderTotal(Connection connection, int orderNumber) throws SQLException {
        String sql = "UPDATE supplier_orders SET total_amount = (SELECT COALESCE(SUM(quantity * unit_purchase_price), 0) "
                + "FROM supplier_order_lines WHERE order_number = ?) WHERE order_number = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderNumber);
            statement.setInt(2, orderNumber);
            statement.executeUpdate();
        }
    }

    /** Wraps an SQL error in an unchecked exception. */
    private IllegalStateException databaseError(SQLException exception) {
        return new IllegalStateException("Unable to access supplier data: " + exception.getMessage(), exception);
    }
}
