package com.cash_shop.user;

import java.text.Normalizer;
import java.util.Locale;
import java.security.SecureRandom;

/** Generates the login and the temporary password of a new employee account. */
public final class AccountCredentialGenerator {
    // Characters allowed in generated passwords (look-alike characters such as 0/O and 1/l/I are left out).
    private static final String PASSWORD_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%&*?:";
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Utility class: not instantiable. */
    private AccountCredentialGenerator() {
    }

    /**
     * Builds a login from the first letter of the first name followed by the last name (lower case, no accents
     * or spaces).
     */
    public static String createLogin(String firstName, String lastName) {
        String first = normalize(firstName);
        String last = normalize(lastName);
        if (first.isEmpty() || last.isEmpty()) {
            throw new IllegalArgumentException("First name and last name are required to generate a login.");
        }
        return first.charAt(0) + last;
    }

    /**
     * Generates a random 8-character password with a secure random generator (ambiguous characters such as 0/O
     * and 1/l are excluded).
     */
    public static String createPassword() {
        StringBuilder password = new StringBuilder(8);
        for (int index = 0; index < 8; index++) {
            password.append(PASSWORD_CHARACTERS.charAt(RANDOM.nextInt(PASSWORD_CHARACTERS.length())));
        }
        return password.toString();
    }

    /** Lower-cases the text and removes accents and any character that is not a letter or a digit. */
    private static String normalize(String value) {
        if (value == null) return "";
        String decomposed = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase(Locale.ROOT);
    }

}