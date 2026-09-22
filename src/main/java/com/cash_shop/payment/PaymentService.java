package com.cash_shop.payment;

public class PaymentService {
    public void validatePayment(double amount) {
        if (amount > 0) {
            System.out.println("Payment validated. Amount: " + amount);
        } else {
            System.out.println("Invalid payment amount.");
        }
    }
    public void displayPaymentDetails(int paymentId, double amount, String paymentMethod, String paymentDate) {
        System.out.println("Payment Details:");
        System.out.println("Payment ID: " + paymentId);
        System.out.println("Amount: " + amount);
        System.out.println("Payment Method: " + paymentMethod);
        System.out.println("Payment Date: " + paymentDate);
    }
}
