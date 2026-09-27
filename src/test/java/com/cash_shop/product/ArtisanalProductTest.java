package com.cash_shop.product;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ArtisanalProductTest {

    @Test
    public void fromAcceptsCurrentAndLegacyBakeryValues() {
        assertEquals(ArtisanalProduct.TypeArtisanal.BAKERY, ArtisanalProduct.TypeArtisanal.from("BAKERY"));
        assertEquals(ArtisanalProduct.TypeArtisanal.BAKERY, ArtisanalProduct.TypeArtisanal.from("BACERY"));
    }
}