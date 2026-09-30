package com.cash_shop.employee;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.cash_shop.common.EmailService;
import com.cash_shop.common.StyleManager;
import com.cash_shop.user.AccountCredentials;

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
    private boolean refreshing;
    private final Timer refreshTimer = new Timer(5000, event -> refreshFromDatabase());

    public EmployeeView() {
        this(false);
    }

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
            @Override
            public void windowClosed(WindowEvent event) {
                refreshTimer.stop();
            }
        });
    }

    private void buildUI() {
        // Formulaire à gauche
        JPanel form = StyleManager.createForm("Employee details");
        StyleManager.addRow(form, 0, "Employee ID:", tfEmployeeId);
        StyleManager.addRow(form, 1, "First Name:", tfFirstName);
        StyleManager.addRow(form, 2, "Last Name:", tfLastName);
        StyleManager.addRow(form, 3, "Email:", tfEmail);
        StyleManager.addRow(form, 4, "Salary:", tfSalary);
        StyleManager.addRow(form, 5, "Role:", cbRole);
        JPanel left = new JPanel(new BorderLayout());
        left.add(form, BorderLayout.NORTH);

        // Recherche au-dessus du tableau
        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(event -> searchEmployee());
        tfSearch.addActionListener(event -> searchEmployee());
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.add(new JLabel("Search:"));
        searchBar.add(tfSearch);
        searchBar.add(btnSearch);

        table.getSelectionModel().addListSelectionListener(event -> selectEmployee());
        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.add(searchBar, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);

        // Boutons
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

    private void loadEmployees() {
        String selectedMatricule = selectedEmployee == null ? null : selectedEmployee.getMatricule();
        refreshing = true;
        employees.clear();
        employees.addAll(employeeDAO.getAllEmployees());
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

    private void refreshFromDatabase() {
        try {
            loadEmployees();
        } catch (IllegalStateException exception) {
            LOGGER.log(Level.FINE, "Unable to refresh employees.", exception);
        }
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
        StyleManager.applyLookAndFeel();
        javax.swing.SwingUtilities.invokeLater(() -> new EmployeeView().setVisible(true));
    }
}
