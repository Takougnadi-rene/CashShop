package com.cash_shop.sale;

import java.math.BigDecimal;
import java.sql.Date;

/** Read-only line of the sales history: sale id, customer name, cashier name and total amount paid. */
public record SaleSummary(int saleId, Date date, String customerName, String cashierName, BigDecimal totalAmount) {
}