package com.cash_shop.sale;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.StyleManager;
import com.cash_shop.customer.Customer;
import com.cash_shop.customer.CustomerDAO;
import com.cash_shop.employee.Employee;
import com.cash_shop.employee.EmployeeDAO;
import com.cash_shop.employee.Employee.Role;
import com.cash_shop.user.AccessService;
import com.cash_shop.user.AccessService.Module;

public class SaleHistoryView extends JFrame {
    private final SaleDAO saleDAO = new SaleDAO();
    private final DefaultTableModel tableModel = StyleManager.createReadOnlyModel(
            "Sale #", "Customer", "Cashier", "Amount (USD)");
    private final JCheckBox dateEnabled = new JCheckBox("Date");
    private final JSpinner dateFilter = new JSpinner(new SpinnerDateModel());
    private final JComboBox<FilterOption> customerFilter = new JComboBox<>();
    private final JComboBox<FilterOption> cashierFilter = new JComboBox<>();
    private boolean loadingFilters;
    private final Timer refreshTimer = new Timer(5000, event -> refreshSharedData());

    public SaleHistoryView(Role role) {
        if (!new AccessService().canAccess(role, Module.SALES_HISTORY)) {
            throw new SecurityException("Only administrators and accountants can view sales history.");
        }
        setTitle("Sales History");
        setSize(900, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        loadFilters();
        JButton refresh = new JButton("Refresh");
        JButton reset = new JButton("Clear filters");
        JButton close = new JButton("Close");
        refresh.addActionListener(event -> refreshSales());
        reset.addActionListener(event -> clearFilters());
        close.addActionListener(event -> dispose());

        dateFilter.setEditor(new JSpinner.DateEditor(dateFilter, "yyyy-MM-dd"));
        dateFilter.setEnabled(false);
        dateEnabled.addActionListener(event -> {
            dateFilter.setEnabled(dateEnabled.isSelected());
            refreshSales();
        });
        dateFilter.addChangeListener(event -> {
            if (dateEnabled.isSelected()) {
                refreshSales();
            }
        });
        customerFilter.addActionListener(event -> {
            if (!loadingFilters) {
                refreshSales();
            }
        });
        cashierFilter.addActionListener(event -> {
            if (!loadingFilters) {
                refreshSales();
            }
        });

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(dateEnabled);
        filters.add(dateFilter);
        filters.add(new javax.swing.JLabel("Customer:"));
        filters.add(customerFilter);
        filters.add(new javax.swing.JLabel("Cashier:"));
        filters.add(cashierFilter);
        filters.add(refresh);
        filters.add(reset);
        filters.add(close);

        JPanel page = StyleManager.createPage();
        JPanel heading = new JPanel(new BorderLayout(0, 8));
        heading.add(StyleManager.createTitle("Sales History"), BorderLayout.NORTH);
        heading.add(filters, BorderLayout.CENTER);
        page.add(heading, BorderLayout.NORTH);
        page.add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);
        setContentPane(page);
        refreshSales();
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    private void refreshSales() {
        try {
            tableModel.setRowCount(0);
            LocalDate selectedDate = dateEnabled.isSelected()
                ? ((Date) dateFilter.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;
            FilterOption selectedCustomer = (FilterOption) customerFilter.getSelectedItem();
            FilterOption selectedCashier = (FilterOption) cashierFilter.getSelectedItem();
            Integer customerId = selectedCustomer == null || selectedCustomer.value() == null
                ? null : Integer.valueOf(selectedCustomer.value());
            String cashierMatricule = selectedCashier == null ? null : selectedCashier.value();
            for (SaleSummary sale : saleDAO.getSaleSummaries(selectedDate, customerId, cashierMatricule)) {
                BigDecimal amount = sale.totalAmount() == null ? BigDecimal.ZERO : sale.totalAmount();
                tableModel.addRow(new Object[] { sale.saleId(), sale.customerName(), sale.cashierName(),
                        String.format("%.2f", amount) });
            }
        } catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Sales History", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadFilters() {
        loadFilters(true);
    }

    private void loadFilters(boolean showError) {
        FilterOption previousCustomer = (FilterOption) customerFilter.getSelectedItem();
        FilterOption previousCashier = (FilterOption) cashierFilter.getSelectedItem();
        loadingFilters = true;
        customerFilter.removeAllItems();
        cashierFilter.removeAllItems();
        customerFilter.addItem(new FilterOption(null, "All customers"));
        cashierFilter.addItem(new FilterOption(null, "All cashiers"));
        try {
            for (Customer customer : new CustomerDAO().getAllCustomers()) {
                customerFilter.addItem(new FilterOption(Integer.toString(customer.getCustomerId()), customer.getName()));
            }
            for (Employee employee : new EmployeeDAO().getAllEmployees()) {
                if (employee.getRole() == Role.CASHIER) {
                    cashierFilter.addItem(new FilterOption(employee.getMatricule(),
                            employee.getFirstName() + " " + employee.getLastName()));
                }
            }
        } catch (IllegalStateException exception) {
            if (showError) {
                JOptionPane.showMessageDialog(this, exception.getMessage(), "Sales filters",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        customerFilter.setRenderer(renderer);
        cashierFilter.setRenderer(renderer);
        restoreSelection(customerFilter, previousCustomer);
        restoreSelection(cashierFilter, previousCashier);
        loadingFilters = false;
    }

    private void restoreSelection(JComboBox<FilterOption> filter, FilterOption previous) {
        if (previous == null) {
            return;
        }
        for (int index = 0; index < filter.getItemCount(); index++) {
            FilterOption option = filter.getItemAt(index);
            if (java.util.Objects.equals(previous.value(), option.value())) {
                filter.setSelectedIndex(index);
                return;
            }
        }
    }

    private void refreshSharedData() {
        loadFilters(false);
        refreshSales();
    }

    private void clearFilters() {
        dateEnabled.setSelected(false);
        dateFilter.setEnabled(false);
        customerFilter.setSelectedIndex(0);
        cashierFilter.setSelectedIndex(0);
        refreshSales();
    }

    private record FilterOption(String value, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}