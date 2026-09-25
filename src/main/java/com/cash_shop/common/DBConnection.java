package com.cash_shop.common;   
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://192.168.1.102:3306/cash_shop";
    private static final String USER = "root";
    private static final String PASSWORD = "toto";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}