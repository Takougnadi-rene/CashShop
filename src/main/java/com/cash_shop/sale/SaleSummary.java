package com.cash_shop.sale;

import java.math.BigDecimal;

public record SaleSummary(int saleId, String customerName, String cashierName, BigDecimal totalAmount) {
}