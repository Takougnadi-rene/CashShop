package com.cash_shop.bill;

import com.cash_shop.customer.Customer;
import com.cash_shop.employee.Employee;
import com.cash_shop.sale.Sale;

import java.util.Date;

/** Business operations around bills: creation and display of receipts. */
public class BillService {

    private int billNumber = 0;
    BillDAO billDAO = new BillDAO();
    /** Creates and saves a bill with the next number of this service instance. */
    public void newBill(Date billDate, Customer customer, Employee cashier, Sale purchase) {
        this.billNumber++;
        Bill bill = new Bill(billNumber, billDate, customer, cashier, purchase);
        billDAO.addBill(bill);
    }

    /** Prints the receipt of the given bill number to the console. */
    public void affichageBill(int billNumber) {
        Bill bill = billDAO.getBill(billNumber);
        if (bill == null) {
            System.out.println("Bill #" + billNumber + " not found.");
            return;
        }
        System.out.println(bill.genererFacture());
    }

    /** Prints the receipt of the given bill number to the console. */
    public void printBill(int billNumber) {
        Bill bill = billDAO.getBill(billNumber);
        if (bill == null) {
            System.out.println("Bill #" + billNumber + " not found.");
            return;
        }
        System.out.println(bill.genererFacture());
    }
}