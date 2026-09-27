package com.cash_shop.bill;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.cash_shop.customer.Customer;
import com.cash_shop.employee.Employee;
import com.cash_shop.sale.Sale;
import com.cash_shop.sale.SaleService;
import com.cash_shop.product.Product;

public class Bill {
    private int billNumber;
    private Date billDate;
    private Customer customer;
    private Employee cashier;
    private Sale sale;

    public Bill(int billNumber, Date billDate, Customer customer, Employee cashier, Sale sale) {
        this.billNumber = billNumber;
        this.billDate = billDate;
        this.customer = customer;
        this.cashier = cashier;
        this.sale = sale;
    }

    public int getBillNumber() {return billNumber;}
    public Date getBillDate() {return billDate;}
    public Customer getCustomer() {return customer;}
    public Employee getCashier() {return cashier;}
    public Sale getSale() {return sale;}

    //bill generator method
    public String genererFacture() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder sb = new StringBuilder();
        double total = new SaleService().calculateTotal(sale);


        sb.append("================================================\n");
        sb.append("        SUPER MARKET PLUS\n");
        sb.append("         Purchase Receipt\n");
        sb.append("================================================\n\n");
        sb.append(String.format("Receipt No.: %04d        Date: %s\n", billNumber, sdf.format(billDate)));
        sb.append(String.format("Cashier    : %-15s Customer: %s\n",
                cashier != null ? cashier.getFirstName() + " " + cashier.getLastName() : "N/A",
                customer != null ? customer.getName() : "Walk-in"));
        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("%-20s %5s %15s\n", "PRODUCT", "QTY", "PRICE (FCFA)"));
        sb.append("------------------------------------------------\n");

        for (Product product : sale.getProductsList()) {
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
