package com.cash_shop.product;

public class ProduitService {
    private Product product;

    public void addProduct(Product product) {
        this.product = product;
    }
    public void removeProduct() {
        this.product = null;
    }
    public void modifyProduct(Product product) {
        this.product.setProduct(product);

    }
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

