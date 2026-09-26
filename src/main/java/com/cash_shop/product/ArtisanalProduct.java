package com.cash_shop.product;

public class ArtisanalProduct extends Product {

    public enum TypeArtisanal {
        BACERY, FISHMONGER, BUTCHER
    }

    private TypeArtisanal artisanalType;

    public ArtisanalProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity,
            TypeArtisanal type) {
        super(reference, designation, "ARTISANAL", "ARTISANAL", purchasePrice, sellingPrice, stockQuantity);
        this.artisanalType = type;
    }
    // getters and setters

    public String getType() {
        return artisanalType == null ? super.getType() : artisanalType.name();
    }

    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            artisanalType = null;
            super.setType(type);
            return;
        }
        artisanalType = TypeArtisanal.valueOf(type.trim().toUpperCase());
        super.setType(artisanalType.name());
    }

    public TypeArtisanal getArtisanalType() {
        return artisanalType;
    }
}