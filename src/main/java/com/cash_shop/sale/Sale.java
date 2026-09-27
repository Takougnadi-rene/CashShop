package com.cash_shop.sale;

import java.time.LocalDate;
import java.util.ArrayList;

import com.cash_shop.employee.Employee;
import com.cash_shop.product.Product;

public class Sale {
    private int saleId;
    private LocalDate saleDate;
    private Employee cashier;
    private ArrayList<Product> productsList;

    public Sale(int saleId, LocalDate saleDate, Employee cashier) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.cashier = cashier;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public Employee getCashier() {
        return cashier;
    }

    public void setCashier(Employee cashier) {
        this.cashier = cashier;
    }

    public ArrayList<Product> getProductsList() {
        return productsList;
    }

    public void setProductsList(ArrayList<Product> productsList) {
        this.productsList = productsList;
    }

    @Override
    public String toString() {
        return "Sale [saleId=" + saleId + ", saleDate=" + saleDate + ", cashier="
                + cashier.getFirstName() + " " + cashier.getLastName() + ", productsList="
                + productsList + "]";
    }
}
