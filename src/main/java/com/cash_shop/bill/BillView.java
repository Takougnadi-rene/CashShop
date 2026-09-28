package com.cash_shop.bill;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.print.PrinterException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.cash_shop.common.StyleManager;
import com.cash_shop.customer.Customer;
import com.cash_shop.payment.Payment;
import com.cash_shop.sale.Sale;

public class BillView extends JFrame {
    private final JTextArea receiptArea = new JTextArea();
    private final JButton printButton = StyleManager.createButton("Print Receipt", StyleManager.ACCENT_BLUE);

    public BillView() {
        this(null);
    }

    public BillView(Sale sale) {
        this(sale, null, null);
    }

    public BillView(Sale sale, Customer customer, Payment.PaymentMode paymentMode) {
        this(sale, customer, paymentMode, null);
    }

    public BillView(Sale sale, Customer customer, Payment.PaymentMode paymentMode, Double cashReceived) {
        this(sale, customer, paymentMode, cashReceived, sale == null ? 0 : sale.getSaleId());
        }

        public BillView(Sale sale, Customer customer, Payment.PaymentMode paymentMode,
            Double cashReceived, int billNumber) {
        setTitle("Cash Shop - Receipt");
        setSize(560, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        if (sale == null) {
            receiptArea.setText("No receipt has been generated.");
            printButton.setEnabled(false);
        } else {
            Bill bill = new Bill(billNumber, new Date(), customer, sale.getCashier(), sale);
            String receipt = bill.genererFacture();
            if (paymentMode != null) {
                String paymentLabel = switch (paymentMode) {
                    case CASH -> "Cash";
                    case CREDIT_CARD -> "Bank card";
                    case MOBILE_PAYMENT -> "Mobile Money";
                };
                int totalLine = receipt.indexOf("TOTAL DUE:");
                receipt = receipt.substring(0, totalLine)
                        + "PAYMENT METHOD: " + paymentLabel + "\n"
                        + receipt.substring(totalLine);
                if (paymentMode == Payment.PaymentMode.CASH && cashReceived != null) {
                    double change = cashReceived - new com.cash_shop.sale.SaleService().calculateTotal(sale);
                    int dueLine = receipt.indexOf("TOTAL DUE:");
                    receipt = receipt.substring(0, dueLine)
                        + String.format("CASH RECEIVED: %,.0f FCFA\nCHANGE: %,.0f FCFA\n",
                            cashReceived, change)
                        + receipt.substring(dueLine);
                }
            }
            receiptArea.setText(receipt);
            receiptArea.setCaretPosition(0);
        }
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(StyleManager.BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel receiptPanel = StyleManager.createCard();
        receiptPanel.setLayout(new BorderLayout());
        receiptArea.setEditable(false);
        receiptArea.setBackground(StyleManager.BG_PANEL);
        receiptArea.setForeground(StyleManager.TEXT_PRIMARY);
        receiptArea.setCaretColor(StyleManager.TEXT_PRIMARY);
        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        receiptArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        receiptPanel.add(new JScrollPane(receiptArea), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton closeButton = StyleManager.createButton("Close", StyleManager.ACCENT_RED);
        printButton.addActionListener(event -> printReceipt());
        closeButton.addActionListener(event -> dispose());
        actions.add(printButton);
        actions.add(closeButton);

        mainPanel.add(StyleManager.createLabel("PURCHASE RECEIPT", StyleManager.ACCENT_GOLD,
                StyleManager.FONT_TITLE), BorderLayout.NORTH);
        mainPanel.add(receiptPanel, BorderLayout.CENTER);
        mainPanel.add(actions, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void printReceipt() {
        try {
            receiptArea.print();
        } catch (PrinterException exception) {
            JOptionPane.showMessageDialog(this, "Unable to print the receipt: " + exception.getMessage(),
                    "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BillView billView = new BillView();
            billView.setVisible(true);
        });
    }
}
