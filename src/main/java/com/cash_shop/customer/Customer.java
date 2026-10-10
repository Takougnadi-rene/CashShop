package com.cash_shop.customer;

import java.util.ArrayList;

import com.cash_shop.sale.Sale;

/** A supermarket customer with contact details, total amount spent, loyalty points and purchase history. */
public class Customer {
    private final int customerId;
    private String name;
    private String email;
    private String phoneNumber;
    private double totalSpent;
    private int fidelityPoints;
    private ArrayList<Sale> purchaseHistory;

    /** Creates a customer with zero loyalty points and an empty purchase history. */
    public Customer(int customerId, String name, String email, String phoneNumber, double totalSpent) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.totalSpent = totalSpent;
        this.purchaseHistory = new ArrayList<>();
    }

    // getter and setters

    public int getCustomerId() {return customerId;}
    public String getName() {return name;}
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email;}
    public void setEmail(String email) { this.email = email;}
    public String getPhoneNumber() { return phoneNumber;}
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber;}
    public double getTotalSpent() { return totalSpent;}
    public void setTotalSpent(double totalSpent) { this.totalSpent = totalSpent;}
    public int getLoyaltyPoints() { return fidelityPoints;}
    public void setLoyaltyPoints(int loyaltyPoints) { this.fidelityPoints = loyaltyPoints;}
    public ArrayList<Sale> getPurchaseHistory() { return purchaseHistory;}
    public void setPurchaseHistory(ArrayList<Sale> purchaseHistory) { this.purchaseHistory = purchaseHistory != null ? purchaseHistory : new ArrayList<>();}

    /** Adds a sale to the purchase history (null is ignored). */
    public void addPurchase(Sale sale) {
        if (sale != null) {
            purchaseHistory.add(sale);
        }
    }
}
