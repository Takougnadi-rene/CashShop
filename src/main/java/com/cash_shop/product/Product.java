package com.cash_shop.product;
/**
 * A product sold in the supermarket: reference, designation, purchase/selling price and quantity in stock.
 * Specialised by FreshProduct, ElectronicProduct and ArtisanalProduct.
 */
public class Product {

    private int reference;
    private String designation;
    private double purchasePrice;
    private double sellingPrice;
    private int stockQuantity;

    /**
     * Creates a product with its reference, label and pricing data.
     */

    public Product(int reference, String designation, double purchasePrice, double sellingPrice, int stockQuantity) {
        this.reference = reference;
        this.designation = designation;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.stockQuantity = stockQuantity;
    }

    // getters and setters
    public int getReference() {return reference;}
    public String getDesignation() {return designation;}
    public double getPurchasePrice() {return purchasePrice;}
    public double getSellingPrice() {return sellingPrice;}
    public int getStockQuantity() {return stockQuantity;}
    public Product getProductById(int reference) {return this;}
    public void setReference(int reference) {this.reference = reference;}
    public void setDesignation(String designation) {this.designation = designation;}
    public void setPurchasePrice(double purchasePrice) {this.purchasePrice = purchasePrice;}
    public void setSellingPrice(double sellingPrice) {this.sellingPrice = sellingPrice;}
    public void setStockQuantity(int stockQuantity) {this.stockQuantity = stockQuantity;}
    

    /**
     * Copies another product's values into this instance.
     */
    public void setProduct(Product product) {
        this.reference = product.getReference();
        this.designation = product.getDesignation();
        this.purchasePrice = product.getPurchasePrice();
        this.sellingPrice = product.getSellingPrice();
        this.stockQuantity = product.getStockQuantity();
    }

    // Readable representation used for logging and debugging.
    @Override
    public String toString() {
        return "Product [reference=" + reference + ", designation=" + designation + ", purchasePrice=" + purchasePrice
                + ", sellingPrice=" + sellingPrice
                + ", stockQuantity=" + stockQuantity + "]";
    }
}
