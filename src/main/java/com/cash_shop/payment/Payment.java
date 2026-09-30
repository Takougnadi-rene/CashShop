package com.cash_shop.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.cash_shop.sale.Sale;

public class Payment {
    private int paymentNumber;
    private double amount;
    private Date paymentDate;
    private Sale sale;
    private String paymentMode;
    public enum PaymentMode {
        CASH,
        CREDIT_CARD,
        MOBILE_PAYMENT
    }

    public Payment(int paymentNumber, double amount, LocalDateTime paymentDate, Sale sale, PaymentMode paymentMode) {
        this.paymentNumber = paymentNumber;
        this.amount = amount;
        this.paymentDate = paymentDate == null ? null : java.sql.Timestamp.valueOf(paymentDate);
        this.sale = sale;
        this.paymentMode = paymentMode.name();
    }

    public Payment(int paymentNumber, double amount, LocalDate paymentDate, Sale sale, PaymentMode paymentMode) {
        this(paymentNumber, amount, paymentDate == null ? null : paymentDate.atStartOfDay(), sale, paymentMode);
    }

    public int getPaymentNumber() {return paymentNumber;}
    public void setPaymentNumber(int paymentNumber) {this.paymentNumber = paymentNumber;}
    public double getAmount() {return amount;}
    public void setAmount(double amount) {this.amount = amount;}
    public PaymentMode getPaymentMode() {return PaymentMode.valueOf(this.paymentMode);}
    public void setPaymentMode(PaymentMode paymentMode) {this.paymentMode = paymentMode.name();}
    public Date getPaymentDate() {return paymentDate;}
    public void setPaymentDate(Date paymentDate) {this.paymentDate = paymentDate;}
    public Sale getSale() {return sale;}
    public void setSale(Sale sale) {this.sale = sale;}
}
