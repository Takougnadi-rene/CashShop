package com.cash_shop.product;
/** A perishable product with an expiration date (ISO yyyy-MM-dd) and a storage temperature. */
public class FreshProduct extends Product {

    private String expirationDate;
    private double storageTemperature;

    /** Creates a fresh product. */
    public FreshProduct(int reference, String designation, double purchasePrice, double sellingPrice, int stockQuantity, String expirationDate, 
        double storageTemperature) {
        super(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        this.expirationDate = expirationDate;
        this.storageTemperature = storageTemperature;
    }

    // getters and setters
    public String getExpirationDate() {return expirationDate;}
    public void setExpirationDate(String expirationDate) {this.expirationDate = expirationDate;}
    public double getStorageTemperature() {return storageTemperature;}
    public void setStorageTemperature(double storageTemperature) {this.storageTemperature = storageTemperature;}

}
