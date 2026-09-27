package com.cash_shop.product;

public class ArtisanalProduct extends Product {

    public enum TypeArtisanal {
        BACERY, FISHMONGER, BUTCHER
    }

    private TypeArtisanal artisanalType;

    public ArtisanalProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity,
            TypeArtisanal type) {
        super(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        this.artisanalType = type;
    }
    // getters and setters

    public String getType() {
        return artisanalType.name();
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            artisanalType = null;
            return;
        }
        artisanalType = TypeArtisanal.valueOf(type.trim().toUpperCase());
    }

    public TypeArtisanal getArtisanalType() {
        return artisanalType;
    }

    @Override
    public String toString() {
        return "ArtisanalProduct [reference=" + getReference() + ", designation=" + getDesignation()
                + ", purchasePrice=" + getPurchasePrice() + ", sellingPrice=" + getSellingPrice()
                + ", stockQuantity=" + getStockQuantity() + ", artisanalType=" + artisanalType + "]";
    }
}