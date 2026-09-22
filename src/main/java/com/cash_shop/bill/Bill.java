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

    public int getBillId() {
        return billId;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public Date getBillDate() {
        return billDate;
    }

    public void setBillDate(Date billDate) {
        this.billDate = billDate;
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
        sb.append("     Rapport d'Achat - Ticket\n");
        sb.append("================================================\n\n");
        sb.append(String.format("Facture N° : %04d        Date : %s\n", billId, sdf.format(billDate)));
        sb.append(String.format("Caissier   : %-15s Client : %s\n",
                cashier != null ? cashier.getFirstName() + " " + cashier.getLastName() : "N/A",
                customer != null ? customer.getFirstName() + " " + customer.getLastName() : "Tout-Venant"));
        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("%-20s %5s %15s\n", "PRODUIT", "QTÉ", "PRIX (FCFA)"));
        sb.append("------------------------------------------------\n");

        for (Product product : purchasedItems) {
            sb.append(String.format("%-20s %5d %15.0f F\n",
                    product.getDesignation(), 1, product.getSellingPrice()));
        }

        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("TOTAL À PAYER :         %15.0f FCFA\n", total));
        sb.append("\n================================================\n");
        sb.append("         Merci de votre confiance !\n");
        sb.append("         À bientôt dans nos rayons.\n");
        sb.append("================================================\n");
        return sb.toString();
    }
}
