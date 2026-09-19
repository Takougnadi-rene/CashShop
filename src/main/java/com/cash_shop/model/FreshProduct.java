package com.cash_shop.model;

public class FreshProduct extends Product {

    private String expirationDate;
    private double storageTemperature;

    public FreshProduct(int reference, String designation, double purchasePrice, double sellingPrice, int stockQuantity, String expirationDate, 
        double storageTemperature) {
        super(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        this.expirationDate = expirationDate;
        this.storageTemperature = storageTemperature;
    }

    // Getter and Setter for expirationDate

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    // Getter and Setter for storageTemperature

    public double getStorageTemperature() {
        return storageTemperature;
    }

    public void setStorageTemperature(double storageTemperature) {
        this.storageTemperature = storageTemperature;
    }

}
