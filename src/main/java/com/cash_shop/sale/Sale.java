package com.cash_shop.sale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.cash_shop.employee.Employee;
import com.cash_shop.product.Product;

/** A sale made by a cashier at a given date, with the list of the products sold (one list entry per unit). */
public class Sale {
    private int saleId;
    private LocalDateTime saleDate;
    private Employee cashier;
    private ArrayList<Product> productsList;

    /** Creates an empty sale. */
    public Sale(int saleId, LocalDateTime saleDate, Employee cashier) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.cashier = cashier;
        this.productsList = new ArrayList<>();
    }

    /** Creates an empty sale from a date only (time set to midnight). */
    public Sale(int saleId, LocalDate saleDate, Employee cashier) {
        this(saleId, saleDate == null ? null : saleDate.atStartOfDay(), cashier);
    }

    //getters and setters
    public int getSaleId() {return saleId;}
    public void setSaleId(int saleId) {this.saleId = saleId;}
    public LocalDateTime getSaleDate() {return saleDate;}
    public void setSaleDate(LocalDateTime saleDate) {this.saleDate = saleDate;}
    public void setSaleDate(LocalDate saleDate) {this.saleDate = saleDate == null ? null : saleDate.atStartOfDay();}
    public Employee getCashier() {return cashier;}
    public void setCashier(Employee cashier) {this.cashier = cashier;}
    public ArrayList<Product> getProductsList() {return productsList;}
    public void setProductsList(ArrayList<Product> productsList) {this.productsList = productsList;}

    // Readable representation used for logging and debugging.
    @Override
    public String toString() {
        return "Sale [saleId=" + saleId + ", saleDate=" + saleDate + ", cashier="
                + (cashier == null ? "N/A" : cashier.getFirstName() + " " + cashier.getLastName()) + ", productsList="
                + productsList + "]";
    }
}
