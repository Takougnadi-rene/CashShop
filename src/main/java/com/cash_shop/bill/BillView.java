package com.cash_shop.bill;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.print.PrinterException;

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
    private final JButton printButton = new JButton("Print Receipt");

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
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        buildUI();
        if (sale == null) {
            receiptArea.setText("No receipt has been generated.");
            printButton.setEnabled(false);
        } else {
            Bill bill = new Bill(billNumber, java.sql.Timestamp.valueOf(sale.getSaleDate()), customer,
                    sale.getCashier(), sale);
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
                            + String.format("CASH RECEIVED: %,.0f $\nCHANGE: %,.0f $\n",
                                    cashReceived, change)
                            + receipt.substring(dueLine);
                }
            }
            receiptArea.setText(receipt);
            receiptArea.setCaretPosition(0);
        }
    }

    private void buildUI() {
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        receiptArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton closeButton = new JButton("Close");
        printButton.addActionListener(event -> printReceipt());
        closeButton.addActionListener(event -> dispose());

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Purchase receipt"), BorderLayout.NORTH);
        page.add(new JScrollPane(receiptArea), BorderLayout.CENTER);
        page.add(StyleManager.createButtonBar(printButton, closeButton), BorderLayout.SOUTH);
        setContentPane(page);
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
        StyleManager.applyLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            BillView billView = new BillView();
            billView.setVisible(true);
        });
    }
}
