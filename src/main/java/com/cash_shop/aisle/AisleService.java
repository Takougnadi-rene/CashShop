package com.cash_shop.aisle;
import com.cash_shop.product.Product;

/** In-memory business operations on the products of an aisle (add, remove, search, display, stock value). */
public class AisleService {
    
    /** Adds a product to the aisle's in-memory product list. */
    public void addProductToAisle(Aisle aisle, Product product) {
        aisle.getProductsList().add(product);
    }
    /** Removes a product from the aisle's in-memory product list. */
    public void removeProductFromAisle(Aisle aisle, Product product) {
        aisle.getProductsList().remove(product);
    }
    /** Searches the aisle for a product by reference and prints the result. */
    public void searchProductInAisleRef(Aisle aisle, String productReference) {
        for (Product product : aisle.getProductsList()) {
            if (String.valueOf(product.getReference()).equals(productReference)) {
                System.out.println("Product found: " + product.getDesignation());
                return;
            }
        }
        System.out.println("Product not found in aisle: " + aisle.getAisleName());
    }
    
    /** Searches the aisle for a product by designation (case-insensitive) and prints the result. */
    public void searchProductInAisleDes(Aisle aisle, String productDesignation) {
        for (Product product : aisle.getProductsList()) {
            if (product.getDesignation().equalsIgnoreCase(productDesignation)) {
                System.out.println("Product found: " + product.getDesignation());
                return;
            }
        }
        System.out.println("Product not found in aisle: " + aisle.getAisleName());
    }
    /** Prints the designation of every product in the aisle. */
    public void displayProductsInAisle(Aisle aisle) {
        System.out.println("Products in aisle " + aisle.getAisleName() + ":");
        for (Product product : aisle.getProductsList()) {
            System.out.println("- " + product.getDesignation());
        }
    }
    /** Computes the value of the aisle stock (selling price x quantity, summed over all products). */
    public double totalValueOfProductsInAisle(Aisle aisle) {
        double totalValue = 0.0;
        for (Product product : aisle.getProductsList()) {
            totalValue += product.getSellingPrice() * product.getStockQuantity();
        }
        return totalValue;
    }
}
