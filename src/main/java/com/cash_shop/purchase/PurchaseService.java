package com.cash_shop.purchase;

import com.cash_shop.product.Product;
public class PurchaseService {
    public void addProductInCart(Purchase purchase, Product product) {
        purchase.getCartItems().add(product);
        System.out.println("Product added to cart: " + product);
    }

    public void removeProductFromCart(Purchase purchase, Product product) {
        if (purchase.getCartItems().remove(product)) {
            System.out.println("Product removed from cart: " + product);
        } else {
            System.out.println("Product not found in cart: " + product);
        }
    }
    public double calculateTotalAmount(Purchase purchase) {
        double totalAmount = 0.0;
        for (Product product : purchase.getCartItems()) {
            totalAmount += product.getSellingPrice();
        }
        return totalAmount;
    }

    public void validatePurchase(Purchase purchase) {
        double totalAmount = calculateTotalAmount(purchase);
        System.out.println("Purchase validated. Total amount: " + totalAmount);
    }
}
