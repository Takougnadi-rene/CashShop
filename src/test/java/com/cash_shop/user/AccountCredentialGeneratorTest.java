package com.cash_shop.user;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AccountCredentialGeneratorTest {
    @Test
    public void createsLoginFromFirstInitialAndFullSurname() {
        assertEquals("jsmith", AccountCredentialGenerator.createLogin("John", "Smith"));
    }

    @Test
    public void normalizesAccentedNames() {
        assertEquals("emartin", AccountCredentialGenerator.createLogin("Élodie", "Martin"));
    }
}