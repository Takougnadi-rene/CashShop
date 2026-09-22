package com.cash_shop.supplier;

public class Supplier {
    private int code;
    private String name;
    private String telephone;
    private String address;
    
    public Supplier(int code, String name, String telephone, String address) {
        this.code = code;
        this.name = name;
        this.telephone = telephone;
        this.address = address;
    }

    // Getters and Setters
    
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTelephone() {
        return telephone;
    }
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
