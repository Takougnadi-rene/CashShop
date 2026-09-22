package com.cash_shop.product;

public class ArtisanalProduct extends Product {

    public enum TypeArtisanal {
        BACERY, FISHMONGER, BUTCHER
    }
    private TypeArtisanal type;

    public ArtisanalProduct(int reference, String designation, double purchasePrice, double sellingPrice, int stockQuantity, 
        TypeArtisanal type) {
        super(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        this.type = type;
    }
    // Getter and Setter for type
    public TypeArtisanal getType() {
        return type;
    }

    public void setType(TypeArtisanal type) {
        this.type = type;
    }
}