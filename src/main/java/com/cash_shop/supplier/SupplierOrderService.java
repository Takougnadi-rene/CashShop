package com.cash_shop.supplier;

import java.math.BigDecimal;
import java.util.List;

import com.cash_shop.product.Product;

public class SupplierOrderService {
    public void addProduct(SupplierOrder order, Product product) {
        addProduct(order, product, 1);
    }

    public void addProduct(SupplierOrder order, Product product, int quantity) {
        if (order == null || product == null) {
            throw new IllegalArgumentException("Order and product are required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        List<Product> items = order.getListOfProducts();
        for (int index = 0; index < items.size(); index++) {
            Product item = items.get(index);
            if (item.getReference() == product.getReference()) {
                items.set(index, copyAsOrderItem(item, item.getStockQuantity() + quantity));
                return;
            }
        }
        items.add(copyAsOrderItem(product, quantity));
    }

    public void removeProduct(SupplierOrder order, Product product) {
        if (order == null || product == null) {
            return;
        }
        order.getListOfProducts().removeIf(item -> item.getReference() == product.getReference());
    }

    public double calculateTotal(SupplierOrder order) {
        if (order == null) {
            return 0.0;
        }
        return order.getListOfProducts().stream()
            .map(item -> BigDecimal.valueOf(item.getPurchasePrice())
                .multiply(BigDecimal.valueOf(item.getStockQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .doubleValue();
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
        for (Product item : order.getListOfProducts()) {
            System.out.println("- " + item.getDesignation() + " : " + item.getStockQuantity() + " units");
        }
    }

    private Product copyAsOrderItem(Product product, int quantity) {
        return new Product(product.getReference(), product.getDesignation(), product.getPurchasePrice(),
                product.getSellingPrice(), quantity);
    }
}
