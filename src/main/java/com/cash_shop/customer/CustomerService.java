package com.cash_shop.customer;

import java.util.ArrayList;

import com.cash_shop.sale.Sale;

public class CustomerService {
    private final ArrayList<Customer> customers = new ArrayList<>();

    public void addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        customers.add(customer);
    }

    public void removeCustomer(Customer customer) {
        if (customer != null) {
            customers.remove(customer);
        }
    }

    public ArrayList<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public void addLoyaltyPoints(Customer customer, int points) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
    }

    public void registerPurchase(Customer customer, Sale purchase) {
        if (customer == null || purchase == null) {
            throw new IllegalArgumentException("Customer and purchase are required");
        }
        customer.addPurchase(purchase);
        customer.setTotalSpent(customer.getTotalSpent() + calculatePurchaseTotal(purchase));
        int earnedPoints = (int) (calculatePurchaseTotal(purchase) / 10);
        addLoyaltyPoints(customer, earnedPoints);
    }

    public double calculatePurchaseTotal(Sale purchase) {
        if (purchase == null || purchase.getProductsList() == null) {
            return 0.0;
        }
        double total = 0.0;
        for (var product : purchase.getProductsList()) {
            total += product.getSellingPrice();
        }
        return total;
    }
}
