package com.cash_shop.product;

import java.util.Locale;

/** A product made by a craftsman (bakery, fishmonger or butcher), identified by its artisanal type. */

public class ArtisanalProduct extends Product {
/** Kinds of artisanal shops. */
public enum TypeArtisanal {
        BAKERY, FISHMONGER, BUTCHER;

        /** Parses a type name */
        public static TypeArtisanal from(String type) {
            String normalized = type.trim().toUpperCase(Locale.ROOT);
            return valueOf(normalized);
        }
    }

    private TypeArtisanal artisanalType;

    /** Creates an artisanal product. */
    public ArtisanalProduct(int reference, String designation, double purchasePrice, double sellingPrice,
            int stockQuantity, TypeArtisanal type) {
        super(reference, designation, purchasePrice, sellingPrice, stockQuantity);
        this.artisanalType = type;
    }

    // getters and setters
    public String getType() {return artisanalType.name();}
    public void setType(String type) {
        if (type == null || type.trim().isEmpty()) {
            artisanalType = null;
            return;
        }
        artisanalType = TypeArtisanal.from(type);
    }
    public TypeArtisanal getArtisanalType() {return artisanalType;}
    
    @Override
    public String toString() {
        return "ArtisanalProduct [reference=" + getReference() + ", designation=" + getDesignation()
                + ", purchasePrice=" + getPurchasePrice() + ", sellingPrice=" + getSellingPrice()
                + ", stockQuantity=" + getStockQuantity() + ", artisanalType=" + artisanalType + "]";
    }
}