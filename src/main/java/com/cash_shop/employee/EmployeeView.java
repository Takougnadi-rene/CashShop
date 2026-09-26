package com.cash_shop.employee;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import static com.cash_shop.common.StyleManager.ACCENT_BLUE;
import static com.cash_shop.common.StyleManager.ACCENT_GOLD;
import static com.cash_shop.common.StyleManager.ACCENT_GREEN;
import static com.cash_shop.common.StyleManager.ACCENT_RED;
import static com.cash_shop.common.StyleManager.BG_DARK;
import static com.cash_shop.common.StyleManager.BG_PANEL;
import static com.cash_shop.common.StyleManager.FONT_BODY;
import static com.cash_shop.common.StyleManager.FONT_HEADER;
import static com.cash_shop.common.StyleManager.FONT_SMALL;
import static com.cash_shop.common.StyleManager.FONT_TITLE;
import static com.cash_shop.common.StyleManager.TEXT_MUTED;
import static com.cash_shop.common.StyleManager.TEXT_PRIMARY;
import static com.cash_shop.common.StyleManager.createButton;
import static com.cash_shop.common.StyleManager.createCard;
import static com.cash_shop.common.StyleManager.createField;
import static com.cash_shop.common.StyleManager.createLabel;
import static com.cash_shop.common.StyleManager.createTable;

public class EmployeeView extends JFrame {
    private JTextField tfEmployeeId, tfFirstName, tfLastName, tfEmail, tfSalary, tfSearch;
    private JComboBox<Employee.Role> cbRole;
    private DefaultTableModel tableModel;
    private JTable table;
    private Employee selectedEmployee;
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final List<Employee> employees = new ArrayList<>();

    public EmployeeView() {
        setTitle("Employee Management");
        setSize(900, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        loadEmployees();
    }

    private void buildUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);

        JPanel titrePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        titrePanel.setBackground(BG_PANEL);
        titrePanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        titrePanel.add(createLabel("⚙ EMPLOYEE MANAGEMENT", ACCENT_GOLD, FONT_TITLE));

        JPanel corps = new JPanel(new GridLayout(1, 2, 10, 0));
        corps.setBackground(BG_DARK);
        corps.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        corps.setPreferredSize(new java.awt.Dimension(900, 450));

        JPanel formPanel = createCard();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setPreferredSize(new java.awt.Dimension(300, 0));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        formPanel.add(createLabel("[ Employee Form ]", ACCENT_BLUE, FONT_HEADER), g);
        g.gridwidth = 1;

        addField(formPanel, g, 1, "Employee ID:", tfEmployeeId = createField());
        addField(formPanel, g, 2, "First Name:", tfFirstName = createField());
        addField(formPanel, g, 3, "Last Name:", tfLastName = createField());
        addField(formPanel, g, 4, "Email:", tfEmail = createField());
        addField(formPanel, g, 5, "Salary:", tfSalary = createField());

        g.gridy = 6;
        g.gridx = 0;
        formPanel.add(createLabel("Role:", TEXT_MUTED, FONT_SMALL), g);
        g.gridx = 1;
        cbRole = new JComboBox<>(Employee.Role.values());
        cbRole.setBackground(BG_DARK);
        cbRole.setForeground(TEXT_PRIMARY);
        cbRole.setFont(FONT_BODY);
        formPanel.add(cbRole, g);

        g.gridy = 7;
        g.gridx = 0;
        formPanel.add(createLabel("Search:", TEXT_MUTED, FONT_SMALL), g);
        g.gridx = 1;
        tfSearch = createField();
        formPanel.add(tfSearch, g);

        JPanel listePanel = createCard();
        listePanel.setLayout(new BorderLayout());
        listePanel.setPreferredSize(new java.awt.Dimension(520, 0));
        listePanel.add(createLabel("[ Employee List ]", ACCENT_BLUE, FONT_HEADER), BorderLayout.NORTH);
        tableModel = new DefaultTableModel(
                new String[] { "Employee ID", "First Name", "Last Name", "Email", "Salary", "Role" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = createTable(tableModel);
        table.getSelectionModel().addListSelectionListener(event -> selectEmployee());
        listePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        corps.add(formPanel);
        corps.add(listePanel);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        actionsPanel.setBackground(BG_PANEL);
        actionsPanel.add(createLabel("[ Actions ]", TEXT_MUTED, FONT_SMALL));
        javax.swing.JButton btnAdd = createButton("+ Add", ACCENT_GREEN);
        javax.swing.JButton btnModify = createButton("✎ Modify", ACCENT_BLUE);
        javax.swing.JButton btnDelete = createButton("✕ Delete", ACCENT_RED);
        javax.swing.JButton btnSearch = createButton("⚲ Search", ACCENT_GOLD);
        javax.swing.JButton btnClose = createButton("Close", new Color(80, 80, 100));
        btnAdd.addActionListener(event -> addEmployee());
        btnModify.addActionListener(event -> updateEmployee());
        btnDelete.addActionListener(event -> deleteEmployee());
        btnSearch.addActionListener(event -> searchEmployee());
        btnClose.addActionListener(event -> dispose());
        actionsPanel.add(btnAdd);
        actionsPanel.add(btnModify);
        actionsPanel.add(btnDelete);
        actionsPanel.add(btnSearch);
        actionsPanel.add(btnClose);

        main.add(titrePanel, BorderLayout.NORTH);
        main.add(corps, BorderLayout.CENTER);
        main.add(actionsPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    private void addField(JPanel panel, GridBagConstraints constraints, int row, String label,
            JTextField field) {
        constraints.gridy = row;
        constraints.gridx = 0;
        panel.add(createLabel(label, TEXT_MUTED, FONT_SMALL), constraints);
        constraints.gridx = 1;
        panel.add(field, constraints);
    }

    private void loadEmployees() {
        employees.clear();
        employees.addAll(employeeDAO.getAllEmployees());
        displayEmployees(employees);
    }

    private void displayEmployees(List<Employee> employeeList) {
        tableModel.setRowCount(0);
        for (Employee employee : employeeList) {
            tableModel.addRow(new Object[] { employee.getMatricule(), employee.getFirstName(),
                    employee.getLastName(), employee.getEmail(), String.format("%.2f", employee.getSalary()),
                    employee.getRole() });
        }
    }

    private void selectEmployee() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= employees.size()) {
            return;
        }
        selectedEmployee = employees.get(row);
        tfEmployeeId.setText(selectedEmployee.getMatricule());
        tfFirstName.setText(selectedEmployee.getFirstName());
        tfLastName.setText(selectedEmployee.getLastName());
        tfEmail.setText(selectedEmployee.getEmail());
        tfSalary.setText(String.valueOf(selectedEmployee.getSalary()));
        cbRole.setSelectedItem(selectedEmployee.getRole());
    }

    private void addEmployee() {
        try {
            Employee employee = readEmployee();
            employeeDAO.insertEmployee(
                    employee.getMatricule(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getEmail(),
                    employee.getSalary(),
                    employee.getRole());
            employees.add(employee);
            loadEmployees();
            clearForm();
            showSuccess("Employee added successfully.");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void updateEmployee() {
        if (selectedEmployee == null) {
            showError("Select an employee.");
            return;
        }
        try {
            Employee employee = readEmployee();
            int index = employees.indexOf(selectedEmployee);
            employeeDAO.updateEmployee(employee);
            employees.set(index, employee);
            loadEmployees();
            clearForm();
            showSuccess("Employee updated.");
        } catch (IllegalArgumentException exception) {
            showError(exception.getMessage());
        }
    }

    private void deleteEmployee() {
        if (selectedEmployee == null) {
            showError("Select an employee.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            employeeDAO.deleteEmployee(selectedEmployee.getMatricule());
            employees.remove(selectedEmployee);
            loadEmployees();
            clearForm();
        }
    }

    private Employee readEmployee() {
        String employeeId = tfEmployeeId.getText().trim();
        String firstName = tfFirstName.getText().trim();
        String lastName = tfLastName.getText().trim();
        String email = tfEmail.getText().trim();
        if (employeeId.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Please fill in all fields.");
        }
        try {
            double salary = Double.parseDouble(tfSalary.getText().trim());
            return new Employee(employeeId, firstName, lastName, email, salary,
                    (Employee.Role) cbRole.getSelectedItem());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Please check the salary value.");
        }
    }

    private void searchEmployee() {
        String term = tfSearch.getText().trim().toLowerCase();
        List<Employee> results = new ArrayList<>();
        for (Employee employee : employees) {
            if (employee.getMatricule().toLowerCase().contains(term)
                    || employee.getFirstName().toLowerCase().contains(term)
                    || employee.getLastName().toLowerCase().contains(term)
                    || employee.getEmail().toLowerCase().contains(term)) {
                results.add(employee);
            }
        }
        displayEmployees(results);
    }

    private void clearForm() {
        tfEmployeeId.setText("");
        tfFirstName.setText("");
        tfLastName.setText("");
        tfEmail.setText("");
        tfSalary.setText("");
        tfSearch.setText("");
        cbRole.setSelectedIndex(0);
        selectedEmployee = null;
        table.clearSelection();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new EmployeeView().setVisible(true));
    }
}