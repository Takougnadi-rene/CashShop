package com.cash_shop.user;

import java.text.Normalizer;
import java.util.Locale;
import java.security.SecureRandom;

public final class AccountCredentialGenerator {
    private static final String PASSWORD_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private AccountCredentialGenerator() {
    }

    public static String createLogin(String firstName, String lastName) {
        String first = normalize(firstName);
        String last = normalize(lastName);
        if (first.isEmpty() || last.isEmpty()) {
            throw new IllegalArgumentException("First name and last name are required to generate a login.");
        }
        return prefix(first, 4) + prefix(last, 4);
    }

    public static String createPassword() {
        StringBuilder password = new StringBuilder(8);
        for (int index = 0; index < 8; index++) {
            password.append(PASSWORD_CHARACTERS.charAt(RANDOM.nextInt(PASSWORD_CHARACTERS.length())));
        }
        return password.toString();
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

    private static String prefix(String value, int length) {
        return value.substring(0, Math.min(value.length(), length));
    }
}