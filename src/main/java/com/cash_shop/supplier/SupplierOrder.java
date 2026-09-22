package com.cash_shop.supplier;

import java.util.Date;
import java.util.List;

import com.cash_shop.product.Product;

public class SupplierOrder {
    
    private int orderNumber;
    private Date orderDate;
    private List<Product> listOfProducts;
    private enum OrderStatus {
        PENDING, COMPLETED, CANCELLED
    }
    private OrderStatus status;

    public SupplierOrder(int orderNumber, Date orderDate, List<Product> listOfProducts, OrderStatus status) {
        this.orderNumber = orderNumber;
        this.orderDate = orderDate;
        this.listOfProducts = listOfProducts;
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
        return orderDate;
    }
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    public List<Product> getListOfProducts() {
        return listOfProducts;
    }
    public void setListOfProducts(List<Product> listOfProducts) {
        this.listOfProducts = listOfProducts;
    }
    public OrderStatus getStatus() {
        return status;
    }
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
