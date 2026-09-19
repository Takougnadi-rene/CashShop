package com.cash_shop.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Bill {
    private int billId;
    private Date billDate;
    private Customer customer;
    private Employee cashier;
    
    public Bill(int billId, Date billDate, Customer customer, Employee cashier, ArrayList<Product> purchasedItems) {
        this.billId = billId;
        this.billDate = billDate;
        this.customer = customer;
        this.cashier = cashier;
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
    public String genererFacture() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        sb.append("================================================\n");
        sb.append("         \uD83C\uDFEA SUPER MARKET PLUS\n");
        sb.append("         Rapport d'Achat - Ticket\n");
        sb.append("================================================\n\n");
        sb.append(String.format("Facture N° : %04d        Date : %s\n", billId, sdf.format(billDate)));
        sb.append(String.format("Caissier   : %-15s Client : %s\n",
                cashier != null ? cashier.toString() : "N/A",
                customer != null ? customer.toString() : "Tout-Venant"));
        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("%-20s %5s %15s\n", "PRODUIT", "QTÉ", "PRIX (FCFA)"));
        sb.append("------------------------------------------------\n");
        for (Product p : purchasedItems) {
            int qte = vente.getQuantite(p);
            sb.append(String.format("%-20s %5d %15.0f F\n", p.getDesignation(), qte, p.getPrixVente() * qte));
        }
        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("\uD83D\uDD35 TOTAL À PAYER :         %15.0f FCFA\n", vente.calculerTotal()));
        sb.append(String.format("\nRèglement : %s\n", payment.getModePaiement()));
        if (payment.getMonnaie() > 0)
            sb.append(String.format("Monnaie    : %.0f F CFA\n", payment.getMonnaie()));
        sb.append("\n================================================\n");
        sb.append("         Merci de votre confiance !\n");
        sb.append("         À bientôt dans nos rayons.\n");
        sb.append("================================================\n");
        return sb.toString();
    }
}
