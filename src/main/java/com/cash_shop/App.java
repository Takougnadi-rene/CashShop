package com.cash_shop;

import com.cash_shop.common.StyleManager;
import com.cash_shop.user.UserView;

public class App {
    public static void main(String[] args) {
        StyleManager.applyLookAndFeel();
        javax.swing.SwingUtilities.invokeLater(() -> {
            UserView userView = new UserView();
            userView.setVisible(true);
        });
    }
}
