package com.cash_shop.bill;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.cash_shop.customer.Customer;
import com.cash_shop.employee.Employee;
import com.cash_shop.product.Product;

public class Bill {
    private int billId;
    private Date billDate;
    private Customer customer;
    private Employee cashier;
    private ArrayList<Product> purchasedItems;

    public Bill(int billId, Date billDate, Customer customer, Employee cashier, ArrayList<Product> purchasedItems) {
        this.billId = billId;
        this.billDate = billDate;
        this.customer = customer;
        this.cashier = cashier;
        this.purchasedItems = purchasedItems != null ? purchasedItems : new ArrayList<>();
    }

    public int getBillId() {return billId;}
    public void setBillId(int billId) {this.billId = billId;}
    public Date getBillDate() {return billDate;}
    public void setBillDate(Date billDate) {this.billDate = billDate;}
    public Customer getCustomer() {return customer;}
    public void setCustomer(Customer customer) {this.customer = customer;}
    public Employee getCashier() {return cashier;}

    public void setCashier(Employee cashier) {
        this.cashier = cashier;
    }

    public ArrayList<Product> getPurchasedItems() {
        return purchasedItems;
    }

    public void setPurchasedItems(ArrayList<Product> purchasedItems) {
        this.purchasedItems = purchasedItems != null ? purchasedItems : new ArrayList<>();
    }

    public String genererFacture() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        double total = 0.0;

        for (Product product : purchasedItems) {
            total += product.getSellingPrice();
        }

        sb.append("================================================\n");
        sb.append("        SUPER MARKET PLUS\n");
        sb.append("         Purchase Receipt\n");
        sb.append("================================================\n\n");
        sb.append(String.format("Receipt No.: %04d        Date: %s\n", billId, sdf.format(billDate)));
        sb.append(String.format("Cashier    : %-15s Customer: %s\n",
                cashier != null ? cashier.getFirstName() + " " + cashier.getLastName() : "N/A",
            customer != null ? customer.getName() : "Walk-in"));
        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("%-20s %5s %15s\n", "PRODUCT", "QTY", "PRICE (FCFA)"));
        sb.append("------------------------------------------------\n");

        for (Product product : purchasedItems) {
            sb.append(String.format("%-20s %5d %15.0f F\n",
                    product.getDesignation(), 1, product.getSellingPrice()));
        }

        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("TOTAL DUE:               %15.0f FCFA\n", total));
        sb.append("\n================================================\n");
        sb.append("         Thank you for shopping with us!\n");
        sb.append("         See you again soon.\n");
        sb.append("================================================\n");
        return sb.toString();
    }
}
