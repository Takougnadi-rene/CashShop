package com.cash_shop.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Provides JDBC connections to the Cash Shop MySQL database. */
public class DBConnection {

    /*Alternative connection URLs kept for local or remote environment testing.*/
    
    //private static final String URL = "jdbc:mysql://vmnar-102-22-134-50.run.pinggy-free.link:37509/cash_shop";
    //private static final String URL = "jdbc:mysql://localhost:3306/cash_shop";

    // The application currently connects to the local network database.
    // Each value can be overridden with an environment variable so that credentials
    // do not have to live in the source code (the defaults keep the current behavior).
    private static final String URL = env("CASH_SHOP_DB_URL", "jdbc:mysql://192.168.1.102:3306/cash_shop");
    private static final String USER = env("CASH_SHOP_DB_USER", "linux");
    private static final String PASSWORD = env("CASH_SHOP_DB_PASSWORD", "kingbrain@2.0");

    /** Returns the environment variable value, or the default when it is missing or blank. */
    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * Opens and returns a live JDBC connection to the configured database.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}