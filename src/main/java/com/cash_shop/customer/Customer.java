package com.cash_shop.customer;

import java.util.ArrayList;

import com.cash_shop.purchase.Purchase;

public class Customer {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private double totalSpent;
    private int loyaltyPoints;
    private ArrayList<Purchase> purchaseHistory;

    public Customer(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.totalSpent = 0.0;
        this.loyaltyPoints = 0;
        this.purchaseHistory = new ArrayList<>();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public ArrayList<Purchase> getPurchaseHistory() {
        return purchaseHistory;
    }

    public void setPurchaseHistory(ArrayList<Purchase> purchaseHistory) {
        this.purchaseHistory = purchaseHistory != null ? purchaseHistory : new ArrayList<>();
    }

    public void addPurchase(Purchase purchase) {
        if (purchase != null) {
            purchaseHistory.add(purchase);
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
