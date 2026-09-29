package com.cash_shop.supplier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cash_shop.product.Product;

public class SupplierOrder {
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

    public SupplierOrder(int orderNumber, Date orderDate, List<Product> listOfProducts, OrderStatus status) {
        this(orderNumber, orderDate, 0, "", BigDecimal.ZERO, status);
        this.listOfProducts = listOfProducts == null ? new ArrayList<>() : new ArrayList<>(listOfProducts);
    }

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
    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Date getOrderDate() {
        return new Date(orderDate.getTime());
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public int getSupplierCode() {
        return supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<Product> getListOfProducts() {
        return listOfProducts;
    }

    public void setListOfProducts(List<Product> listOfProducts) {
        this.listOfProducts = listOfProducts == null ? new ArrayList<>() : new ArrayList<>(listOfProducts);
    }
}
