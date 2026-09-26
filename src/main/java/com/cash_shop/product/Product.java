package com.cash_shop.product;

public class Product {

    private int reference;
    private String designation;
    private String type;
    private String category;
    private double purchasePrice;
    private double sellingPrice;
    private int stockQuantity;

    public Product(int reference, String designation, double purchasePrice, double sellingPrice, int stockQuantity) {
        this(reference, designation, "", "", purchasePrice, sellingPrice, stockQuantity);
    }

    public Product(int reference, String designation, String type, String category, double purchasePrice,
            double sellingPrice, int stockQuantity) {
        this.reference = reference;
        this.designation = designation;
        this.type = type;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
    }

    // getters and setters
    public int getReference() {
        return reference;
    }

    public String getDesignation() {
        return designation;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
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

    public Product getProductById(int reference) {
        return this;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
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
        this.type = product.getType();
        this.category = product.getCategory();
        this.purchasePrice = product.getPurchasePrice();
        this.sellingPrice = product.getSellingPrice();
        this.stockQuantity = product.getStockQuantity();
    }

    @Override
    public String toString() {
        return "Product [reference=" + reference + ", designation=" + designation + ", type=" + type
                + ", category=" + category + ", purchasePrice=" + purchasePrice + ", sellingPrice=" + sellingPrice
                + ", stockQuantity=" + stockQuantity + "]";
    }
}
