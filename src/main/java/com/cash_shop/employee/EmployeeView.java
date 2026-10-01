package com.cash_shop.employee;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.EmailService;
import com.cash_shop.common.StyleManager;
import com.cash_shop.user.AccountCredentials;

/**
 * Swing window to manage employees (add, modify, delete, search). It can be opened in read-only mode, and
 * refreshes itself every 5 seconds.
 */
public class EmployeeView extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(EmployeeView.class.getName());
    private final JTextField tfEmployeeId = StyleManager.createField();
    private final JTextField tfFirstName = StyleManager.createField();
    private final JTextField tfLastName = StyleManager.createField();
    private final JTextField tfEmail = StyleManager.createField();
    private final JTextField tfSalary = StyleManager.createField();
    private final JTextField tfSearch = new JTextField(20);
    private final JComboBox<Employee.Role> cbRole = new JComboBox<>(Employee.Role.values());
    private final DefaultTableModel tableModel = StyleManager.createReadOnlyModel(
            "Employee ID", "First Name", "Last Name", "Email", "Salary", "Role");
    private final JTable table = StyleManager.createTable(tableModel);
    private Employee selectedEmployee;
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final EmployeeService employeeService = new EmployeeService();
    private final EmailService emailService = new EmailService();
    private final List<Employee> employees = new ArrayList<>();
    private final List<Employee> visibleEmployees = new ArrayList<>();
    private final boolean readOnly;
    // True while the table is being rebuilt, so that selection events are ignored.
    private boolean refreshing;
    private final Timer refreshTimer = new Timer(5000, event -> refreshFromDatabase());

    /** Editable window. */
    public EmployeeView() {
        this(false);
    }

    /** Window that can be opened read-only. */
    public EmployeeView(boolean readOnly) {
        this.readOnly = readOnly;
        setTitle("Employee Management");
        setSize(Toolkit.getDefaultToolkit().getScreenSize());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        tfEmployeeId.setEditable(!readOnly);
        tfFirstName.setEditable(!readOnly);
        tfLastName.setEditable(!readOnly);
        tfEmail.setEditable(!readOnly);
        tfSalary.setEditable(!readOnly);
        cbRole.setEnabled(!readOnly);
        loadEmployees();
        refreshTimer.start();
        addWindowListener(new WindowAdapter() {
            // Stop the periodic refresh once the window is closed.
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    /** Builds the form, the search bar, the table and the buttons. */
    private void buildUI() {
        // Form on the left
        JPanel form = StyleManager.createForm("Employee Details");
        StyleManager.addRow(form, 0, "Employee ID:", tfEmployeeId);
        StyleManager.addRow(form, 1, "First Name:", tfFirstName);
        StyleManager.addRow(form, 2, "Last Name:", tfLastName);
        StyleManager.addRow(form, 3, "Email:", tfEmail);
        StyleManager.addRow(form, 4, "Salary:", tfSalary);
        StyleManager.addRow(form, 5, "Role:", cbRole);
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(form, BorderLayout.NORTH);

        // Styled search bar
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(event -> searchEmployee());
        tfSearch.addActionListener(event -> searchEmployee());
        tfSearch.setBackground(StyleManager.BG_INPUT);
        tfSearch.setForeground(StyleManager.TEXT_PRIMARY);
        tfSearch.setCaretColor(StyleManager.ACCENT_PRIMARY);
        tfSearch.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(StyleManager.BORDER_COLOR, 1),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JLabel searchLabel = StyleManager.createLabel("Search:");
        JPanel searchBar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        searchBar.add(searchLabel);
        searchBar.add(tfSearch);
        searchBar.add(btnSearch);
        StyleManager.styleButton(btnSearch);

        table.getSelectionModel().addListSelectionListener(event -> selectEmployee());
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(searchBar, BorderLayout.NORTH);
        center.add(StyleManager.createScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JButton btnAdd = new JButton("Add");
        JButton btnModify = new JButton("Modify");
        JButton btnDelete = new JButton("Delete");
        JButton btnClose = new JButton("Close");
        btnAdd.setEnabled(!readOnly);
        btnModify.setEnabled(!readOnly);
        btnDelete.setEnabled(!readOnly);
        btnAdd.addActionListener(event -> addEmployee());
        btnModify.addActionListener(event -> updateEmployee());
        btnDelete.addActionListener(event -> deleteEmployee());
        btnClose.addActionListener(event -> dispose());

        JPanel page = StyleManager.createPage();
        page.add(StyleManager.createTitle("Employees"), BorderLayout.NORTH);
        page.add(left, BorderLayout.WEST);
        page.add(center, BorderLayout.CENTER);
        page.add(StyleManager.createButtonBar(btnAdd, btnModify, btnDelete, btnClose), BorderLayout.SOUTH);
        setContentPane(page);
    }

    /** Reloads employees from the database and restores the previous selection. */
    private void loadEmployees() {
        String selectedMatricule = selectedEmployee == null ? null : selectedEmployee.getMatricule();
        List<Employee> loadedEmployees = employeeDAO.getAllEmployees();
        refreshing = true;
        employees.clear();
        employees.addAll(loadedEmployees);
        searchEmployee();
        if (selectedMatricule != null) {
            boolean selectionRestored = false;
            for (int index = 0; index < visibleEmployees.size(); index++) {
                if (selectedMatricule.equals(visibleEmployees.get(index).getMatricule())) {
                    selectedEmployee = visibleEmployees.get(index);
                    table.setRowSelectionInterval(index, index);
                    selectionRestored = true;
                    break;
                }
            }
            if (!selectionRestored) {
                selectedEmployee = null;
                table.clearSelection();
            }
        }
        refreshing = false;
    }

    /** Shows the given employees in the table. */
    private void displayEmployees(List<Employee> employeeList) {
        visibleEmployees.clear();
        visibleEmployees.addAll(employeeList);
        tableModel.setRowCount(0);
        for (Employee employee : employeeList) {
            tableModel.addRow(new Object[] { employee.getMatricule(), employee.getFirstName(),
                    employee.getLastName(), employee.getEmail(), String.format("%.2f", employee.getSalary()),
                    employee.getRole() });
        }
    }

    /** Copies the selected row into the form. */
    private void selectEmployee() {
        if (refreshing) {
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0 || row >= visibleEmployees.size()) {
            return;
        }
        selectedEmployee = visibleEmployees.get(row);
        tfEmployeeId.setText(selectedEmployee.getMatricule());
        tfFirstName.setText(selectedEmployee.getFirstName());
        tfLastName.setText(selectedEmployee.getLastName());
        tfEmail.setText(selectedEmployee.getEmail());
        tfSalary.setText(String.valueOf(selectedEmployee.getSalary()));
        cbRole.setSelectedItem(selectedEmployee.getRole());
    }

    /**
     * Creates the employee and its account, e-mails the credentials when SMTP is configured, and shows them to
     * the user.
     */
    private void addEmployee() {
        try {
            Employee employee = readEmployee();
            AccountCredentials credentials = employeeService.addEmployee(
                    employee.getMatricule(),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getEmail(),
                    employee.getSalary(),
                    employee.getRole());
            employees.add(employee);
            loadEmployees();
            clearForm();
            boolean emailSent = emailService.sendEmployeeCredentials(employee.getEmail(), credentials.login(),
                    credentials.password());
            String message = "Employee and application account created.\n\nLogin: " + credentials.login()
                    + "\nTemporary password: " + credentials.password()
                    + (emailSent ? "\n\nCredentials sent to " + employee.getEmail() + "."
                            : "\n\nEmail was not sent. Configure SMTP to enable email delivery.");
            showSuccess(message);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Saves the modified employee. */
    private void updateEmployee() {
        if (selectedEmployee == null) {
            showError("Select an employee.");
            return;
        }
        try {
            Employee employee = readEmployee();
            employeeDAO.updateEmployee(employee);
            loadEmployees();
            clearForm();
            showSuccess("Employee updated.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            showError(exception.getMessage());
        }
    }

    /** Deletes the selected employee after confirmation. */
    private void deleteEmployee() {
        if (selectedEmployee == null) {
            showError("Select an employee.");
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Delete " + selectedEmployee.getFirstName() + " " + selectedEmployee.getLastName() + " ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                employeeDAO.deleteEmployee(selectedEmployee.getMatricule());
                employees.remove(selectedEmployee);
                loadEmployees();
                clearForm();
            } catch (IllegalStateException exception) {
                showError(exception.getMessage());
            }
        }
    }

    /** Builds an Employee from the form, validating the required fields and the salary. */
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

    /** Filters the table on matricule, first name, last name or e-mail. */
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

    /** Periodic refresh; failures are only logged. */
    private void refreshFromDatabase() {
        try {
            loadEmployees();
        } catch (IllegalStateException exception) {
            LOGGER.log(Level.FINE, "Unable to refresh employees.", exception);
        }
    }

    /** Empties the form and the selection. */
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

    /** Displays an error dialog. */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Displays an information dialog. */
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
