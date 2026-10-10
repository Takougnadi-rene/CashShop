package com.cash_shop.payment;
/** Business operations for payments: validation, processing and display. */
public class PaymentService {

    /** Validates the payment and saves it. Returns false when the payment is invalid. */
    public boolean processPayment(Payment payment) {
        if (validatePayment(payment)) {
            PaymentDAO paymentDAO = new PaymentDAO();
            paymentDAO.insertPayment(payment);
            return true;
        } else {
            return false;
        }
    }

    /** A payment is valid when it has a positive amount, a date, a mode and a sale. */
    public boolean validatePayment(Payment payment) {
        return payment.getAmount() > 0 && payment.getPaymentDate() != null && payment.getPaymentMode() != null
                && payment.getSale() != null;
    }

    /** Prints the payment details to the console. */
    public void displayPaymentDetails(Payment payment) {
        System.out.println("Payment Details:");
        System.out.println("Payment Number: " + payment.getPaymentNumber());
        System.out.println("Amount: " + payment.getAmount());
        System.out.println("Payment Mode: " + payment.getPaymentMode());
        System.out.println("Payment Date: " + payment.getPaymentDate());
        System.out.println("Sale ID: " + payment.getSale().getSaleId());
    }
}
