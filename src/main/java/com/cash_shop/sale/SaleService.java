package com.cash_shop.sale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.cash_shop.employee.Employee;
import com.cash_shop.product.Product;

/** Business operations on sales: creation, product management, totals and validation. */
public class SaleService {

    private final SaleDAO saleDAO;

    /** Creates the service with its own DAO. */
    public SaleService() {
        this.saleDAO = new SaleDAO();
    }

    /**
     * Creates a new sale from a date-only value.
     */
    public void newSale(int saleId, LocalDate saleDate, Employee cashier) {
        newSale(saleId, saleDate == null ? null : saleDate.atStartOfDay(), cashier);
    }

    /**
     * Creates a sale with a full timestamp and the cashier responsible for it.
     */
    public void newSale(int saleId, LocalDateTime saleDate, Employee cashier) {
        if (cashier == null) {
            throw new IllegalArgumentException("Cashier cannot be null");
        }
        Sale sale = new Sale(saleId, saleDate, cashier);
        saleDAO.insertSale(sale);
    }

    /** Saves a modified sale. */
    public void updateSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale cannot be null");
        }
        saleDAO.updateSale(sale);
    }

    /** Deletes a sale. */
    public void deleteSale(int saleId) {
        saleDAO.deleteSale(saleId);
    }

    /** Returns a sale by id. */
    public Sale getSaleById(int saleId) {
        return saleDAO.getSaleById(saleId);
    }

    /** Returns all sales. */
    public List<Sale> getAllSales() {
        return saleDAO.getAllSales();
    }

    /**
     * Adds a product to the selected sale.
     * The sale's product list is created if it is still null.
     */
    public void addProductToSale(Sale sale, Product product) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale cannot be null");
        }
        else if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (sale.getProductsList() == null) {
            sale.setProductsList(new java.util.ArrayList<>());
        }
        sale.getProductsList().add(product);
        saleDAO.insertProductIntoSale(sale.getSaleId(), product);
    }

    /**
     * Removes a product from the current sale and clears it from persistence.
     */
    public void removeProductFromSale(Sale sale, Product product) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale cannot be null");
        }
        else if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        else if (sale.getProductsList() == null || !sale.getProductsList().contains(product)) {
            throw new IllegalArgumentException("Product not found in sale");
        }
        else {
            sale.getProductsList().remove(product);
            saleDAO.removeProductFromSale(sale.getSaleId(), product);
        }
    }

    /**
     * Adds up the selling prices of every product currently in the sale.
     */
    public double calculateTotal(Sale sale) {
        if (sale == null || sale.getProductsList() == null) {
            return 0.0;
        }

        double total = 0.0;
        for (Product product : sale.getProductsList()) {
            total += product.getSellingPrice();
        }
        return total;
    }

    /**
     * Final validation step for a sale: it checks the total and logs it.
     */
    public void validateSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale cannot be null");
        }
        double total = calculateTotal(sale);
        System.out.println("Sale validated. Total amount: " + total);
    }
}
