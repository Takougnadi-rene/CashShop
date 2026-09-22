package com.cash_shop.aisle;
import java.util.ArrayList;

import com.cash_shop.product.Product;

public class Aisle {
    private int aisleCode;
    private String aisleName;
    private String aisleChief;
    private ArrayList<Product> productsList;

    public Aisle(int aisleCode, String aisleName, String aisleChief) {
        this.aisleCode = aisleCode;
        this.aisleName = aisleName;
        this.aisleChief = aisleChief;
        this.productsList = new ArrayList<>();
    }

    // Getters and Setters
    public int getAisleCode() {
        return aisleCode;
    }  
    public void setAisleCode(int aisleCode) {
        this.aisleCode = aisleCode;
    }
    public String getAisleName() {
        return aisleName;
    }
    public void setAisleName(String aisleName) {
        this.aisleName = aisleName;
    }
    public String getAisleChief() {
        return aisleChief;
    }
    public void setAisleChief(String aisleChief) {
        this.aisleChief = aisleChief;
    }
    public ArrayList<Product> getProductsList() {
        return productsList;
    }
    public void setProductsList(ArrayList<Product> productsList) {
        this.productsList = productsList;
    }


}
