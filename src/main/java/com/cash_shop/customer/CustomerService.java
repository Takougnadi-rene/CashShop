package com.cash_shop.customer;

import java.util.ArrayList;

import com.cash_shop.sale.Sale;

/** In-memory business rules for customers: registration, loyalty points and purchase totals. */
public class CustomerService {
    private final ArrayList<Customer> customers = new ArrayList<>();

    /** Registers a customer (null is rejected). */
    public void addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        customers.add(customer);
    }

    /** Unregisters a customer. */
    public void removeCustomer(Customer customer) {
        if (customer != null) {
            customers.remove(customer);
        }
    }

    /** Returns a copy of the registered customers. */
    public ArrayList<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    /** Adds a positive number of loyalty points to a customer. */
    public void addLoyaltyPoints(Customer customer, int points) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (points < 0) {
            throw new IllegalArgumentException("Points must be positive");
        }
        customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
    }

    /** Records a purchase: updates history, total spent, and awards 1 loyalty point per 10 spent. */
    public void registerPurchase(Customer customer, Sale purchase) {
        if (customer == null || purchase == null) {
            throw new IllegalArgumentException("Customer and purchase are required");
        }
        customer.addPurchase(purchase);
        customer.setTotalSpent(customer.getTotalSpent() + calculatePurchaseTotal(purchase));
        // One loyalty point for every 10 spent.
        int earnedPoints = (int) (calculatePurchaseTotal(purchase) / 10);
        addLoyaltyPoints(customer, earnedPoints);
    }

    /** Sums the selling prices of the products of a sale. */
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
