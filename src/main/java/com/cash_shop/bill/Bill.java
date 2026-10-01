package com.cash_shop.bill;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.cash_shop.customer.Customer;
import com.cash_shop.employee.Employee;
import com.cash_shop.product.Product;
import com.cash_shop.sale.Sale;
import com.cash_shop.sale.SaleService;

/**
 * A purchase receipt: number, date, customer (may be null for walk-in customers), cashier and the related
 * sale.
 */
public class Bill {
    private int billNumber;
    private Date billDate;
    private Customer customer;
    private Employee cashier;
    private Sale sale;

    /** Creates a bill for a sale. */
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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
        sb.append(String.format("%-20s %5s %15s\n", "PRODUCT", "QTY", "PRICE ($)"));
        sb.append("------------------------------------------------\n");

        // The sale holds one entry per unit: group them by reference to print one line per product with its
        // quantity.
        Map<Integer, Product> productsByReference = new LinkedHashMap<>();
        Map<Integer, Integer> quantitiesByReference = new LinkedHashMap<>();
        
        for (Product product : sale.getProductsList()) {
            productsByReference.putIfAbsent(product.getReference(), product);
            quantitiesByReference.merge(product.getReference(), 1, Integer::sum);
        }
        for (Map.Entry<Integer, Product> entry : productsByReference.entrySet()) {
            Product product = entry.getValue();
            sb.append(String.format("%-20s %5d %15.0f $\n",
                    product.getDesignation(), quantitiesByReference.get(entry.getKey()), product.getSellingPrice()));
        }

        sb.append("\n------------------------------------------------\n");
        sb.append(String.format("TOTAL DUE:               %15.0f $\n", total));
        sb.append("\n================================================\n");
        sb.append("         Thank you for shopping with us!\n");
        sb.append("         See you again soon.\n");
        sb.append("================================================\n");
        return sb.toString();
    }
}
