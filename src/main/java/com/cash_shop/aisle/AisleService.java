package com.cash_shop.aisle;
import com.cash_shop.product.Product;

public class AisleService {
    public void addProductToAisle(Aisle aisle, Product product) {
        aisle.getProductsList().add(product);
    }
    public void removeProductFromAisle(Aisle aisle, Product product) {
        aisle.getProductsList().remove(product);
    }
    public void searchProductInAisleRef(Aisle aisle, String productReference) {
        for (Product product : aisle.getProductsList()) {
            if (String.valueOf(product.getReference()).equals(productReference)) {
                System.out.println("Product found: " + product.getDesignation());
                return;
            }
        }
        System.out.println("Product not found in aisle: " + aisle.getAisleName());
    }
    
    public void searchProductInAisleDes(Aisle aisle, String productDesignation) {
        for (Product product : aisle.getProductsList()) {
            if (product.getDesignation().equalsIgnoreCase(productDesignation)) {
                System.out.println("Product found: " + product.getDesignation());
                return;
            }
        }
        System.out.println("Product not found in aisle: " + aisle.getAisleName());
    }
    public void displayProductsInAisle(Aisle aisle) {
        System.out.println("Products in aisle " + aisle.getAisleName() + ":");
        for (Product product : aisle.getProductsList()) {
            System.out.println("- " + product.getDesignation());
        }
    }
    public double totalValueOfProductsInAisle(Aisle aisle) {
        double totalValue = 0.0;
        for (Product product : aisle.getProductsList()) {
            totalValue += product.getSellingPrice() * product.getStockQuantity();
        }
        return totalValue;
    }
}
