package com.cash_shop.bill;

import com.cash_shop.customer.Customer;
import com.cash_shop.employee.Employee;
import com.cash_shop.sale.Sale;

import java.util.Date;

public class BillService {

    private int billNumber = 0;
    public void newBill(Date billDate, Customer customer, Employee cashier, Sale purchase) {
        this.billNumber++;
        Bill bill = new Bill(billNumber, billDate, customer, cashier, purchase);
        BillDAO billDAO = new BillDAO();
        billDAO.addBill(bill);
    }

    public void affichageBill(int billNumber) {
        BillDAO billDAO = new BillDAO();
        Bill bill = billDAO.getBill(billNumber);
        if (bill == null) {
            System.out.println("Bill #" + billNumber + " not found.");
            return;
        }
        System.out.println(bill.genererFacture());
    }

    public void printBill(int billNumber) {
        BillDAO billDAO = new BillDAO();
        Bill bill = billDAO.getBill(billNumber);
        if (bill == null) {
            System.out.println("Bill #" + billNumber + " not found.");
            return;
        }
        System.out.println(bill.genererFacture());
    }
}