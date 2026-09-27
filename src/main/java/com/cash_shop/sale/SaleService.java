package com.cash_shop.sale;

import java.time.LocalDate;
import java.util.List;

import com.cash_shop.employee.Employee;
import com.cash_shop.product.Product;

public class SaleService {

    private final SaleDAO saleDAO;

    public SaleService() {
        this.saleDAO = new SaleDAO();
    }

    public void newSale(int saleId, LocalDate saleDate, Employee cashier) {
        if (cashier == null) {
            throw new IllegalArgumentException("Cashier cannot be null");
        }
        Sale sale = new Sale(saleId, saleDate, cashier);
        saleDAO.insertSale(sale);
    }

    public void updateSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale cannot be null");
        }
        saleDAO.updateSale(sale);
    }

    public void deleteSale(int saleId) {
        saleDAO.deleteSale(saleId);
    }

    public Sale getSaleById(int saleId) {
        return saleDAO.getSaleById(saleId);
    }

    public List<Sale> getAllSales() {
        return saleDAO.getAllSales();
    }

    //concerning products of a sale
    public void addProductToSale(Sale sale, Product product) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale cannot be null");
        }
        else if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        else if (sale.getProductsList() == null) {
            sale.setProductsList(new java.util.ArrayList<>());
        }
        else {
            sale.getProductsList().add(product);
            saleDAO.insertProductIntoSale(sale.getSaleId(), product);  
        }
    }

    public void removeProductFromSale(Sale sale, Product product) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale cannot be null");
        }
        else if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        else if (sale.getProductsList().contains(product) == false) {
            throw new IllegalArgumentException("Product not found in sale");
        }
        else {
            sale.getProductsList().remove(product);
            saleDAO.removeProductFromSale(sale.getSaleId(), product);  
        }
    }

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

    public void validateSale(Sale sale) {
        if (sale == null) {
            throw new IllegalArgumentException("Sale cannot be null");
        }
        double total = calculateTotal(sale);
        System.out.println("Sale validated. Total amount: " + total);
    }
}
