package com.cash_shop.payment;

public class PaymentService {

    public boolean processPayment(Payment payment) {
        if (validatePayment(payment)) {
            PaymentDAO paymentDAO = new PaymentDAO();
            paymentDAO.insertPayment(payment);
            return true;
        } else {
            return false;
        }
    }

    public boolean validatePayment(Payment payment) {
        if (payment.getAmount() > 0 && payment.getPaymentDate() != null && payment.getPaymentMode() != null
                && payment.getSale() != null) {
            return true;
        } else {
            return false;
        }
    }

    public void displayPaymentDetails(Payment payment) {
        System.out.println("Payment Details:");
        System.out.println("Payment Number: " + payment.getPaymentNumber());
        System.out.println("Amount: " + payment.getAmount());
        System.out.println("Payment Mode: " + payment.getPaymentMode());
        System.out.println("Payment Date: " + payment.getPaymentDate());
        System.out.println("Sale ID: " + payment.getSale().getSaleId());
    }
}
