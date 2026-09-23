package com.cash_shop;

import com.cash_shop.user.UserView;

public class App {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            UserView userView = new UserView();
            userView.setVisible(true);
        });
    }
}
