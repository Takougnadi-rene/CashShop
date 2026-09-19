package com.cash_shop.model;

public class ElectronicProduct extends Product {

    int warranty;
    public ElectronicProduct(int reference, String designation, double purchasePrice, double sellingPrice, int stockQuantity, int warranty) {
        super(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        this.warranty = warranty;
    }
    //getters and setters
    public int getWarranty() {
        return warranty;
    }

    public void setWarranty(int warranty) {
        this.warranty = warranty;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArtisanalProduct{");
        sb.append("warranty=").append(warranty);
        sb.append('}');
        return sb.toString();
    }

}
