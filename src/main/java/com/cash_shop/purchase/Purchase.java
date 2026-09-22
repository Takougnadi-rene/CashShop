package com.cash_shop.purchase;

import java.util.ArrayList;
import java.util.Date;

import com.cash_shop.customer.Customer;
import com.cash_shop.employee.Employee;
import com.cash_shop.product.Product;


public class Purchase {
    private int purchaseId;
    private Date purchaseDate;
    private Customer customer;
    private Employee cashier;
    private ArrayList<Product> cartItems;

    public Purchase(int purchaseId, Date purchaseDate, Customer customer, Employee cashier, ArrayList<Product> cartItems) {
        this.purchaseId = purchaseId;
        this.purchaseDate = purchaseDate;
        this.customer = customer;
        this.cashier = cashier;
        this.cartItems = cartItems;
    }

    public int getPurchaseId() {
        return purchaseId;
    }
    public void setPurchaseId(int purchaseId) {
        this.purchaseId = purchaseId;
    }
    public Date getPurchaseDate() {
        return purchaseDate;
    }
    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    public Employee getCashier() {
        return cashier;
    }
    public void setCashier(Employee cashier) {
        this.cashier = cashier;
    }
    public ArrayList<Product> getCartItems() {
        return cartItems;
    }
    public void setCartItems(ArrayList<Product> cartItems) {
        this.cartItems = cartItems;
    }

}
