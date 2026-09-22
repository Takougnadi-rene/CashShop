package com.cash_shop.product;

public class Product {

    private int reference;
    private String designation;
    private double purchasePrice;
    private double sellingPrice;
    private int stockQuantity;

    public Product(int reference, String designation, double purchasePrice, double sellingPrice, int stockQuantity) {
        this.reference = reference;
        this.designation = designation;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
    }
    // Getters

    public int getReference() {
        return reference;
    }
    public String getDesignation() {
        return designation;
    }
    public double getPurchasePrice() {
        return purchasePrice;
    }
    public double getSellingPrice() {
        return sellingPrice;
    }
    public int getStockQuantity() {
        return stockQuantity;
    }
    public Product getProduct() {
        return this;
    }

    //` Setters

    public void setReference(int reference) {
        this.reference = reference;
    }
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    public void setProduct(Product product) {
        this.reference = product.getReference();
        this.designation = product.getDesignation();
        this.purchasePrice = product.getPurchasePrice();
        this.sellingPrice = product.getSellingPrice();
        this.stockQuantity = product.getStockQuantity();
        //this.Product = Product;
    }

    @Override
    public String toString() {
        return "Product [reference=" + reference + ", designation=" + designation + ", purchasePrice=" + purchasePrice
                + ", sellingPrice=" + sellingPrice + ", stockQuantity=" + stockQuantity + "]";
    }
}
