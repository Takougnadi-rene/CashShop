package com.cash_shop.bill;

import java.util.ArrayList;
import java.util.Date;

import com.cash_shop.customer.Customer;
import com.cash_shop.employee.Employee;
import com.cash_shop.product.Product;

public class BillService {
    public Bill createBill(int billId, Customer customer, Employee cashier, ArrayList<Product> purchasedItems) {
        return new Bill(billId, new Date(), customer, cashier, purchasedItems);
    }

    public double calculateTotal(Bill bill) {
        if (bill == null || bill.getPurchasedItems() == null) {
            return 0.0;
        }
        double total = 0.0;
        for (Product product : bill.getPurchasedItems()) {
            total += product.getSellingPrice();
        }
        return total;
    }

    public void printBill(Bill bill) {
        if (bill == null) {
            System.out.println("Aucune facture à afficher.");
            return;
        }
        System.out.println(bill.genererFacture());
    }
}
