package com.cash_shop.supplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cash_shop.product.Product;

/**
 * A purchase order sent to a supplier: number, date, supplier, total amount, status and ordered products (the
 * stock quantity of each Product holds the ordered quantity).
 */
public class SupplierOrder {
/**
 * Life cycle of an order: PENDING -> APPROUVED or DENIDED (reviewed by the counter) -> DELIVERED (confirmed by
 * the manager). The constant names APPROUVED and DENIDED are misspelled but are stored in the database, so
 * they are kept as they are.
 */
public enum OrderStatus {
        PENDING,
        APPROUVED,
        DENIDED,
        DELIVERED
    }

    private int orderNumber;
    private Date orderDate;
    private int supplierCode;
    private String supplierName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private List<Product> listOfProducts = new ArrayList<>();

    /** Creates an order from a list of products (supplier data is left empty). */
    public SupplierOrder(int orderNumber, Date orderDate, List<Product> listOfProducts, OrderStatus status) {
        this(orderNumber, orderDate, 0, "", BigDecimal.ZERO, status);
        this.listOfProducts = listOfProducts == null ? new ArrayList<>() : new ArrayList<>(listOfProducts);
    }

    /** Creates an order header as read from the database. */
    public SupplierOrder(int orderNumber, Date orderDate, int supplierCode, String supplierName,
            BigDecimal totalAmount, OrderStatus status) {
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    // Getters and Setters
    public int getOrderNumber() {return orderNumber;}
    public Date getOrderDate() {return orderDate == null ? null : new Date(orderDate.getTime());}
    public int getSupplierCode() {return supplierCode;}
    public String getSupplierName() {return supplierName;}
    public BigDecimal getTotalAmount() {return totalAmount;}
    public void setTotalAmount(BigDecimal totalAmount) {this.totalAmount = totalAmount;}
    public OrderStatus getStatus() {return status;}
    public void setStatus(OrderStatus status) {this.status = status;}
    public List<Product> getListOfProducts() {return listOfProducts;}
    public void setListOfProducts(List<Product> listOfProducts) {
        this.listOfProducts = listOfProducts == null ? new ArrayList<>() : new ArrayList<>(listOfProducts);
    }
}
