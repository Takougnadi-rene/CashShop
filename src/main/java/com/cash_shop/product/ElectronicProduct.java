package com.cash_shop.product;

public class ElectronicProduct extends Product {

    String brand;
    int warranty;

    public ElectronicProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, String brand, int warranty) {
        super(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        this.brand = brand;
        this.warranty = warranty;
    }

    // getters and setters
    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getWarranty() {
        return warranty;
    }

    public void setWarranty(int warranty) {
        this.warranty = warranty;
    }

    @Override
    public String toString() {
        return super.toString() + "ElectronicProduct{" +
                "brand='" + brand + '\'' +
                ", warranty=" + warranty +
                '}';
    }

}
