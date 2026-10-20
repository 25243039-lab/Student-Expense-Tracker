package com.studentexpensetracker.ui;

import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.service.ExpenseService;
import com.studentexpensetracker.util.CurrencyUtil;
import com.studentexpensetracker.util.DateUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Expense Management View (CO5 & CO10 integration).
 * Houses the TableView, Add/Update form, Delete mechanism, and Search/Filter toolset.
 */
public class ExpenseManagementView extends VBox {

    private final ExpenseService expenseService;
    private final Runnable onDataChangedCallback;

    // Table
    private final TableView<Expense> expenseTable = new TableView<>();
    private final ObservableList<Expense> tableData = FXCollections.observableArrayList();

    // Search and Filter controls
    private final TextField txtSearch = new TextField();
    private final ComboBox<String> cmbFilterCategory = new ComboBox<>();
    private final ComboBox<String> cmbFilterPayment = new ComboBox<>();
    private final DatePicker dpFromDate = new DatePicker();
    private final DatePicker dpToDate = new DatePicker();
    private final Button btnResetFilters = new Button("Reset Filters");

    // Form inputs (Add / Edit)
    private final Label lblFormTitle = new Label("Add New Expense");
    private final TextField txtDescription = new TextField();
    private final TextField txtAmount = new TextField();
    private final ComboBox<String> cmbCategory = new ComboBox<>();
    private final DatePicker dpDate = new DatePicker();
    private final ComboBox<String> cmbPaymentMethod = new ComboBox<>();
    private final TextArea txtNotes = new TextArea();
    private final Button btnSave = new Button("Save Expense");
    private final Button btnClear = new Button("Clear Form");
    private final Button btnDelete = new Button("Delete Selected");

    // Tracks currently selected expense for update operations
    private Expense selectedExpense = null;

    private static final String[] CATEGORIES = {
        "Food", "Transport", "Education", "Shopping", "Entertainment", "Health", "Others"
    };

    private static final String[] PAYMENT_METHODS = {
        "UPI", "Cash", "Debit Card", "Credit Card", "Net Banking"
    };

    public ExpenseManagementView(ExpenseService expenseService, Runnable onDataChangedCallback) {
        super(16);
        this.expenseService = expenseService;
        this.onDataChangedCallback = onDataChangedCallback;

        setPadding(new Insets(20, 24, 20, 24));
        initializeUI();
        setupEventHandlers();
        refresh();
    }

    private void initializeUI() {
        // 1. Filter & Search Bar
        VBox filterCard = createFilterBar();

        // 2. Main Content Split: Table on Left/Center, Form on Right
        HBox contentSplit = new HBox(18);
        HBox.setHgrow(contentSplit, Priority.ALWAYS);
        VBox.setVgrow(contentSplit, Priority.ALWAYS);

        // Left Container: TableView & Action Toolbar
        VBox tableContainer = new VBox(12);
        HBox.setHgrow(tableContainer, Priority.ALWAYS);
        tableContainer.getStyleClass().add("card");

        configureTableView();
        VBox.setVgrow(expenseTable, Priority.ALWAYS);

        HBox tableToolbar = new HBox(12);
        tableToolbar.setAlignment(Pos.CENTER_RIGHT);

        btnDelete.getStyleClass().add("btn-danger");
        btnDelete.setDisable(true); // Enabled only when row selected

        tableToolbar.getChildren().addAll(btnDelete);
        tableContainer.getChildren().addAll(expenseTable, tableToolbar);

        // Right Container: Add / Edit Form Card
        VBox formContainer = createFormPanel();
        formContainer.setPrefWidth(350);
        formContainer.setMinWidth(320);

        contentSplit.getChildren().addAll(tableContainer, formContainer);

        getChildren().addAll(filterCard, contentSplit);
    }

    private VBox createFilterBar() {
        VBox container = new VBox(10);
        container.getStyleClass().add("card");

        Label lblTitle = new Label("Search & Filter Expenses");
        lblTitle.getStyleClass().add("card-title");

        HBox controlsRow = new HBox(12);
        controlsRow.setAlignment(Pos.CENTER_LEFT);

        txtSearch.setPromptText("Search description or notes...");
        txtSearch.setPrefWidth(220);

        cmbFilterCategory.getItems().add("All Categories");
        cmbFilterCategory.getItems().addAll(CATEGORIES);
        cmbFilterCategory.setValue("All Categories");
        cmbFilterCategory.setPrefWidth(140);

        cmbFilterPayment.getItems().add("All Methods");
        cmbFilterPayment.getItems().addAll(PAYMENT_METHODS);
        cmbFilterPayment.setValue("All Methods");
        cmbFilterPayment.setPrefWidth(130);

        dpFromDate.setPromptText("From Date");
        dpFromDate.setPrefWidth(130);

        dpToDate.setPromptText("To Date");
        dpToDate.setPrefWidth(130);

        btnResetFilters.getStyleClass().add("btn-secondary");

        controlsRow.getChildren().addAll(
            txtSearch,
            cmbFilterCategory,
            cmbFilterPayment,
            dpFromDate,
            dpToDate,
            btnResetFilters
        );

        container.getChildren().addAll(lblTitle, controlsRow);
        return container;
    }

    private void configureTableView() {
        expenseTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Expense, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colId.setMaxWidth(50);
        colId.setMinWidth(40);

        TableColumn<Expense, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(DateUtil.formatDisplay(cell.getValue().getDate())));
        colDate.setPrefWidth(95);

        TableColumn<Expense, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        colDesc.setPrefWidth(180);

        TableColumn<Expense, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(cell -> new SimpleStringProperty(CurrencyUtil.format(cell.getValue().getAmount())));
        colAmount.setPrefWidth(95);

        TableColumn<Expense, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        colCat.setCellFactory(column -> new TableCell<Expense, String>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(category);
                    badge.getStyleClass().add("category-badge");
                    String lower = category.toLowerCase();
                    badge.getStyleClass().add("badge-" + lower);
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colCat.setPrefWidth(110);

        TableColumn<Expense, String> colPay = new TableColumn<>("Payment");
        colPay.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPaymentMethod()));
        colPay.setPrefWidth(90);

        TableColumn<Expense, String> colNotes = new TableColumn<>("Notes");
        colNotes.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNotes()));
        colNotes.setPrefWidth(140);

        expenseTable.getColumns().addAll(colId, colDate, colDesc, colAmount, colCat, colPay, colNotes);
        expenseTable.setPlaceholder(new Label("No expense records found. Try adjusting filters or add an expense."));
        expenseTable.setItems(tableData);
    }

    private VBox createFormPanel() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        lblFormTitle.getStyleClass().add("card-title");

        // Form Fields
        VBox fieldDesc = createFormField("Description *", txtDescription);
        txtDescription.setPromptText("e.g. Campus Lunch, Physics Notes");

        VBox fieldAmount = createFormField("Amount (₹) *", txtAmount);
        txtAmount.setPromptText("e.g. 250.00");

        cmbCategory.getItems().addAll(CATEGORIES);
        cmbCategory.setPromptText("Select Category");
        cmbCategory.setMaxWidth(Double.MAX_VALUE);
        VBox fieldCat = createFormField("Category *", cmbCategory);

        dpDate.setValue(LocalDate.now());
        dpDate.setMaxWidth(Double.MAX_VALUE);
        VBox fieldDate = createFormField("Date *", dpDate);

        cmbPaymentMethod.getItems().addAll(PAYMENT_METHODS);
        cmbPaymentMethod.setValue("UPI");
        cmbPaymentMethod.setMaxWidth(Double.MAX_VALUE);
        VBox fieldPay = createFormField("Payment Method *", cmbPaymentMethod);

        txtNotes.setPromptText("Optional remarks or tags...");
        txtNotes.setPrefRowCount(3);
        txtNotes.setWrapText(true);
        VBox fieldNotes = createFormField("Notes", txtNotes);

        // Action Buttons
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));

        btnClear.getStyleClass().add("btn-secondary");
        btnSave.getStyleClass().add("btn-primary");
        HBox.setHgrow(btnSave, Priority.ALWAYS);
        btnSave.setMaxWidth(Double.MAX_VALUE);

        buttonBar.getChildren().addAll(btnClear, btnSave);

        card.getChildren().addAll(
            lblFormTitle,
            fieldDesc,
            fieldAmount,
            fieldCat,
            fieldDate,
            fieldPay,
            fieldNotes,
            buttonBar
        );

        return card;
    }

    private VBox createFormField(String labelText, Control inputControl) {
        VBox box = new VBox(4);
        Label label = new Label(labelText);
        label.getStyleClass().add("form-label");
        box.getChildren().addAll(label, inputControl);
        return box;
    }

    private void setupEventHandlers() {
        // Table row selection event handler (CO5 event handling)
        expenseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                populateFormForEdit(newVal);
                btnDelete.setDisable(false);
            } else {
                btnDelete.setDisable(true);
            }
        });

        // Save Button (Add or Update based on selectedExpense)
        btnSave.setOnAction(e -> handleSaveExpense());

        // Clear Form Button
        btnClear.setOnAction(e -> clearForm());

        // Delete Button
        btnDelete.setOnAction(e -> handleDeleteExpense());

        // Search and Filters change listeners (Real-time reactive search)
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cmbFilterCategory.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cmbFilterPayment.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dpFromDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dpToDate.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        btnResetFilters.setOnAction(e -> resetFilters());
    }

    private void populateFormForEdit(Expense expense) {
        this.selectedExpense = expense;
        lblFormTitle.setText("Edit Expense #" + expense.getId());
        btnSave.setText("Update Expense");

        txtDescription.setText(expense.getDescription());
        txtAmount.setText(String.valueOf(expense.getAmount()));
        cmbCategory.setValue(expense.getCategory());
        dpDate.setValue(expense.getDate());
        cmbPaymentMethod.setValue(expense.getPaymentMethod());
        txtNotes.setText(expense.getNotes() != null ? expense.getNotes() : "");
    }

    private void clearForm() {
        selectedExpense = null;
        expenseTable.getSelectionModel().clearSelection();
        lblFormTitle.setText("Add New Expense");
        btnSave.setText("Save Expense");

        txtDescription.clear();
        txtAmount.clear();
        cmbCategory.setValue(null);
        dpDate.setValue(LocalDate.now());
        cmbPaymentMethod.setValue("UPI");
        txtNotes.clear();
        btnDelete.setDisable(true);
    }

    private void handleSaveExpense() {
        String desc = txtDescription.getText();
        String amtText = txtAmount.getText();
        String cat = cmbCategory.getValue();
        LocalDate date = dpDate.getValue();
        String pay = cmbPaymentMethod.getValue();
        String notes = txtNotes.getText();

        try {
            double amount = Double.parseDouble(amtText != null ? amtText.trim() : "0");

            if (selectedExpense == null) {
                // Add new expense
                Expense newExp = new Expense(desc, amount, cat, date, pay, notes);
                expenseService.addExpense(newExp);
                DialogHelper.showInfo(getScene().getWindow(), "Success", "Expense Added", "The expense record was saved successfully to SQLite.");
            } else {
                // Update existing expense
                selectedExpense.setDescription(desc);
                selectedExpense.setAmount(amount);
                selectedExpense.setCategory(cat);
                selectedExpense.setDate(date);
                selectedExpense.setPaymentMethod(pay);
                selectedExpense.setNotes(notes);

                expenseService.updateExpense(selectedExpense);
                DialogHelper.showInfo(getScene().getWindow(), "Success", "Expense Updated", "Expense #" + selectedExpense.getId() + " has been updated.");
            }

            clearForm();
            refresh();
            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            }

        } catch (NumberFormatException e) {
            DialogHelper.showError(getScene().getWindow(), "Input Error", "Invalid Amount", "Please enter a valid numeric amount (e.g. 150.50).");
        } catch (IllegalArgumentException e) {
            DialogHelper.showWarning(getScene().getWindow(), "Validation Warning", "Please check your inputs", e.getMessage());
        } catch (SQLException e) {
            DialogHelper.showError(getScene().getWindow(), "Database Error", "Failed to persist expense", e.getMessage());
        }
    }

    private void handleDeleteExpense() {
        Expense toDelete = expenseTable.getSelectionModel().getSelectedItem();
        if (toDelete == null) {
            DialogHelper.showWarning(getScene().getWindow(), "Selection Required", "No expense selected", "Please select an expense row from the table to delete.");
            return;
        }

        boolean confirm = DialogHelper.showConfirmation(
            getScene().getWindow(),
            "Confirm Delete",
            "Delete Expense #" + toDelete.getId() + "?",
            "Are you sure you want to delete '" + toDelete.getDescription() + "' (Amount: " + CurrencyUtil.format(toDelete.getAmount()) + ")?\nThis action cannot be undone."
        );

        if (confirm) {
            try {
                boolean deleted = expenseService.deleteExpense(toDelete.getId());
                if (deleted) {
                    DialogHelper.showInfo(getScene().getWindow(), "Success", "Expense Deleted", "Expense #" + toDelete.getId() + " was deleted from the database.");
                    clearForm();
                    refresh();
                    if (onDataChangedCallback != null) {
                        onDataChangedCallback.run();
                    }
                }
            } catch (SQLException e) {
                DialogHelper.showError(getScene().getWindow(), "Database Error", "Failed to delete expense", e.getMessage());
            }
        }
    }

    private void applyFilters() {
        String keyword = txtSearch.getText();
        String cat = cmbFilterCategory.getValue();
        String pay = cmbFilterPayment.getValue();
        LocalDate from = dpFromDate.getValue();
        LocalDate to = dpToDate.getValue();

        try {
            List<Expense> filtered = expenseService.searchAndFilter(keyword, cat, from, to, pay);
            tableData.setAll(filtered);
        } catch (SQLException e) {
            System.err.println("[ExpenseManagementView] Filter query failed: " + e.getMessage());
        }
    }

    private void resetFilters() {
        txtSearch.clear();
        cmbFilterCategory.setValue("All Categories");
        cmbFilterPayment.setValue("All Methods");
        dpFromDate.setValue(null);
        dpToDate.setValue(null);
        refresh();
    }

    public void refresh() {
        applyFilters();
    }
}
