package com.cash_shop.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Read-only line of the sales history, including the date and time of the sale. */
public record SaleSummary(int saleId, LocalDateTime saleDateTime, String customerName, String cashierName,
        BigDecimal totalAmount) {
}