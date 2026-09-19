package com.cash_shop.service;
import com.cash_shop.model.Product;

public class StockService {
    public void inOfStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() + quantity);
    }
    public void outOfStock(Product product, int quantity) {
        if (product.getStockQuantity() <= quantity) {
            product.setStockQuantity(product.getStockQuantity() - quantity);
        } else {
            throw new IllegalArgumentException("Not enough stock available.");
        }
    }
    public void verifyMinimumStock(Product product, int minimumStock) {
        if (product.getStockQuantity() < minimumStock) {
            System.out.println("Warning: Stock for product " + product.getDesignation() + " is below the minimum threshold.");
        }
    }
}
