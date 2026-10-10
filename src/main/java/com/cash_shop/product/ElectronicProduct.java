package com.cash_shop.product;
/** An electronic product with a brand and a warranty duration in months. */
public class ElectronicProduct extends Product {

    String brand;
    int warranty;

    /** Creates an electronic product. */
    public ElectronicProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, String brand, int warranty) {
        super(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        this.brand = brand;
        this.warranty = warranty;
    }

    // getters and setters
    public String getBrand() {return brand;}
    public void setBrand(String brand) {this.brand = brand;}
    public int getWarranty() {return warranty;}
    public void setWarranty(int warranty) {this.warranty = warranty;}

    // Readable representation used for logging and debugging.
    @Override
    public String toString() {
        return super.toString() + "ElectronicProduct{" +
                "brand='" + brand + '\'' +
                ", warranty=" + warranty +
                '}';
    }

}
