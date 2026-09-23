package com.cash_shop.supplier;

import java.util.List;

import com.cash_shop.product.Product;

public class SupplierOrderService {
    public void addProduct(SupplierOrder order, Product product) {
        if (order == null || product == null) {
            throw new IllegalArgumentException("Order and product are required");
        }
        List<Product> products = order.getListOfProducts();
        if (products != null && !products.contains(product)) {
            products.add(product);
        }
    }

    public void removeProduct(SupplierOrder order, Product product) {
        if (order == null || order.getListOfProducts() == null || product == null) {
            return;
        }
        order.getListOfProducts().remove(product);
    }

    public double calculateTotal(SupplierOrder order) {
        if (order == null || order.getListOfProducts() == null) {
            return 0.0;
        }
        double total = 0.0;
        for (Product product : order.getListOfProducts()) {
            total += product.getPurchasePrice() * product.getStockQuantity();
        }
        return total;
    }

    public void updateStatus(SupplierOrder order, SupplierOrder.OrderStatus status) {
        if (order != null) {
            order.setStatus(status);
        }
    }

    public void displayOrder(SupplierOrder order) {
        if (order == null) {
            System.out.println("No order to display");
            return;
        }
        System.out.println("Order #" + order.getOrderNumber() + " | status=" + order.getStatus());
        for (Product product : order.getListOfProducts()) {
            System.out.println("- " + product.getDesignation() + " : " + product.getStockQuantity() + " units");
        }
    }
}
