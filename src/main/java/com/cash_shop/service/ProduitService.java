package com.cash_shop.service;
import com.cash_shop.model.Product;

public class ProduitService {
    private Product product;

    public void addStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() + quantity);
    }

    public void removeStock(Product product, int quantity) {
        product.setStockQuantity(product.getStockQuantity() - quantity);
    }
    public void modifySellingPrice(Product product, double newPrice) {
        product.setSellingPrice(newPrice);
    }
    public void calculateMargin(Product product) {
        double margin = product.getSellingPrice() - product.getPurchasePrice();
        System.out.println("Margin for product " + product.getDesignation() + ": " + margin);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProduitService{");
        sb.append("product=").append(product);
        sb.append('}');
        return sb.toString();
    }


    
}

