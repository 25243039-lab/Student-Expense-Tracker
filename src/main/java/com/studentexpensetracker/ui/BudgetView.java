package com.studentexpensetracker.ui;

import com.studentexpensetracker.model.Budget;
import com.studentexpensetracker.model.BudgetStatus;
import com.studentexpensetracker.service.BudgetService;
import com.studentexpensetracker.util.CurrencyUtil;
import com.studentexpensetracker.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Budget Management View (CO5 & CO10 integration).
 * Enables monthly budget planning, monitors spending limits, and issues alerts.
 */
public class BudgetView extends ScrollPane {

    private final BudgetService budgetService;
    private final Runnable onDataChangedCallback;

    // Controls
    private final ComboBox<String> cmbMonthYear = new ComboBox<>();
    private final TextField txtBudgetAmount = new TextField();
    private final Button btnSetBudget = new Button("Set / Update Budget");

    // Display Labels
    private final Label lblBudgetValue = new Label("₹ 0.00");
    private final Label lblSpentValue = new Label("₹ 0.00");
    private final Label lblRemainingValue = new Label("₹ 0.00");
    private final Label lblStatusHeader = new Label("Budget Status");
    private final Label lblStatusDetail = new Label("Configure a monthly budget to monitor spending.");

    // Progress Bar & Banner
    private final ProgressBar progressBar = new ProgressBar(0.0);
    private final Label lblPercentage = new Label("0%");
    private final VBox statusBanner = new VBox(6);

    // History Table
    private final TableView<Budget> budgetHistoryTable = new TableView<>();
    private final ObservableList<Budget> historyData = FXCollections.observableArrayList();

    public BudgetView(BudgetService budgetService, Runnable onDataChangedCallback) {
        this.budgetService = budgetService;
        this.onDataChangedCallback = onDataChangedCallback;

        initializeUI();
        setupEventHandlers();
        refresh();
    }

    private void initializeUI() {
        setFitToWidth(true);
        getStyleClass().add("scroll-pane");

        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(24));
        contentBox.setMaxWidth(1200);

        // Populate Month-Year ComboBox with current, past 6, and next 6 months
        LocalDate now = LocalDate.now();
        for (int i = -6; i <= 6; i++) {
            LocalDate m = now.plusMonths(i);
            cmbMonthYear.getItems().add(m.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        cmbMonthYear.setValue(now.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        cmbMonthYear.setPrefWidth(140);

        // 1. Budget Form Card
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("card");

        Label formTitle = new Label("Set Monthly Budget Target");
        formTitle.getStyleClass().add("card-title");

        HBox formRow = new HBox(14);
        formRow.setAlignment(Pos.CENTER_LEFT);

        VBox boxMonth = new VBox(4, new Label("Target Month (YYYY-MM)"), cmbMonthYear);
        VBox boxAmount = new VBox(4, new Label("Monthly Budget Limit (₹)"), txtBudgetAmount);
        txtBudgetAmount.setPromptText("e.g. 10000");
        txtBudgetAmount.setPrefWidth(200);

        btnSetBudget.getStyleClass().add("btn-primary");
        VBox boxBtn = new VBox(4, new Label(" "), btnSetBudget);

        formRow.getChildren().addAll(boxMonth, boxAmount, boxBtn);
        formCard.getChildren().addAll(formTitle, formRow);

        // 2. Budget Overview & Progress Card
        VBox statusCard = new VBox(16);
        statusCard.getStyleClass().add("card");

        Label overviewTitle = new Label("Monthly Financial Overview");
        overviewTitle.getStyleClass().add("card-title");

        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(16);
        metricsGrid.setVgap(16);

        VBox cardBgt = createStatBox("BUDGET LIMIT", lblBudgetValue, "metric-card-lavender", "metric-value-purple");
        VBox cardSpn = createStatBox("TOTAL SPENT", lblSpentValue, "metric-card-blue", "metric-value-blue");
        VBox cardRem = createStatBox("REMAINING BALANCE", lblRemainingValue, "metric-card-mint", "metric-value-mint");

        GridPane.setHgrow(cardBgt, Priority.ALWAYS);
        GridPane.setHgrow(cardSpn, Priority.ALWAYS);
        GridPane.setHgrow(cardRem, Priority.ALWAYS);

        metricsGrid.add(cardBgt, 0, 0);
        metricsGrid.add(cardSpn, 1, 0);
        metricsGrid.add(cardRem, 2, 0);

        // Progress Bar & Percentage
        VBox progressContainer = new VBox(6);
        HBox progressHeader = new HBox(10);
        progressHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblProgressTitle = new Label("Budget Utilization");
        lblProgressTitle.getStyleClass().add("form-label");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        lblPercentage.getStyleClass().add("form-label");
        progressHeader.getChildren().addAll(lblProgressTitle, spacer, lblPercentage);

        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(18);

        progressContainer.getChildren().addAll(progressHeader, progressBar);

        // Warning / Status Banner
        statusBanner.getStyleClass().add("banner-safe");
        lblStatusHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        lblStatusDetail.setStyle("-fx-font-size: 12px;");
        statusBanner.getChildren().addAll(lblStatusHeader, lblStatusDetail);

        statusCard.getChildren().addAll(overviewTitle, metricsGrid, progressContainer, statusBanner);

        // 3. Budget History Table
        VBox historyCard = new VBox(12);
        historyCard.getStyleClass().add("card");

        Label historyTitle = new Label("Saved Budget History");
        historyTitle.getStyleClass().add("card-title");

        configureHistoryTable();
        historyCard.getChildren().addAll(historyTitle, budgetHistoryTable);

        contentBox.getChildren().addAll(formCard, statusCard, historyCard);
        setContent(contentBox);
    }

    private VBox createStatBox(String title, Label valueLabel, String cardClass, String valueClass) {
        VBox box = new VBox(6);
        box.getStyleClass().addAll("metric-card", cardClass);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("metric-label");

        valueLabel.getStyleClass().addAll("metric-value", valueClass);

        box.getChildren().addAll(lblTitle, valueLabel);
        return box;
    }

    private void configureHistoryTable() {
        budgetHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        budgetHistoryTable.setPrefHeight(200);

        TableColumn<Budget, String> colMonth = new TableColumn<>("Month / Year");
        colMonth.setCellValueFactory(cell -> new SimpleStringProperty(DateUtil.formatMonthDisplay(cell.getValue().getMonthYear())));

        TableColumn<Budget, String> colAmt = new TableColumn<>("Allocated Budget");
        colAmt.setCellValueFactory(cell -> new SimpleStringProperty(CurrencyUtil.format(cell.getValue().getAmount())));

        TableColumn<Budget, String> colUpdated = new TableColumn<>("Last Updated");
        colUpdated.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUpdatedAt()));

        budgetHistoryTable.getColumns().addAll(colMonth, colAmt, colUpdated);
        budgetHistoryTable.setPlaceholder(new Label("No monthly budgets set yet."));
        budgetHistoryTable.setItems(historyData);
    }

    private void setupEventHandlers() {
        // Month selector listener
        cmbMonthYear.valueProperty().addListener((obs, oldVal, newVal) -> refresh());

        // Set budget button
        btnSetBudget.setOnAction(e -> handleSetBudget());
    }

    private void handleSetBudget() {
        String monthYear = cmbMonthYear.getValue();
        String amountText = txtBudgetAmount.getText();

        try {
            double amount = Double.parseDouble(amountText != null ? amountText.trim() : "0");
            budgetService.setOrUpdateBudget(monthYear, amount);

            DialogHelper.showInfo(getScene().getWindow(), "Budget Updated", "Monthly Budget Set",
                "Budget for " + DateUtil.formatMonthDisplay(monthYear) + " set to " + CurrencyUtil.format(amount));

            txtBudgetAmount.clear();
            refresh();
            if (onDataChangedCallback != null) {
                onDataChangedCallback.run();
            }

        } catch (NumberFormatException e) {
            DialogHelper.showError(getScene().getWindow(), "Invalid Input", "Invalid Amount", "Please enter a valid numeric budget amount.");
        } catch (IllegalArgumentException e) {
            DialogHelper.showWarning(getScene().getWindow(), "Validation Error", "Invalid Budget Data", e.getMessage());
        } catch (SQLException e) {
            DialogHelper.showError(getScene().getWindow(), "Database Error", "Failed to save budget", e.getMessage());
        }
    }

    public void refresh() {
        String selectedMonth = cmbMonthYear.getValue();
        if (selectedMonth == null || selectedMonth.trim().isEmpty()) {
            selectedMonth = DateUtil.getCurrentMonthYear();
        }

        try {
            BudgetStatus status = budgetService.getBudgetStatus(selectedMonth);

            lblBudgetValue.setText(CurrencyUtil.format(status.getBudgetAmount()));
            lblSpentValue.setText(CurrencyUtil.format(status.getSpentAmount()));

            // Progress bar and Banner styling
            double progressFraction = status.getBudgetAmount() > 0 ? (status.getSpentAmount() / status.getBudgetAmount()) : 0.0;
            progressBar.setProgress(Math.min(1.0, progressFraction));
            lblPercentage.setText(String.format("%.1f%% utilized", status.getPercentageSpent()));

            progressBar.getStyleClass().removeAll("progress-safe", "progress-warning", "progress-danger");
            statusBanner.getStyleClass().removeAll("banner-safe", "banner-warning", "banner-danger");

            if (!status.isBudgetSet()) {
                lblRemainingValue.setText("Not Set");
                statusBanner.getStyleClass().add("banner-warning");
                lblStatusHeader.setText("No Budget Configured");
                lblStatusDetail.setText("Please set a budget limit for " + DateUtil.formatMonthDisplay(selectedMonth) + " using the form above.");
                progressBar.setProgress(0);
                lblPercentage.setText("No budget set");
            } else if (status.isOverBudget()) {
                // Rule: Do not display a negative remaining balance as if it were money available to spend!
                lblRemainingValue.setText("₹ 0.00 (Exceeded)");
                lblRemainingValue.setStyle("-fx-text-fill: #DC2626;");
                progressBar.getStyleClass().add("progress-danger");
                statusBanner.getStyleClass().add("banner-danger");
                lblStatusHeader.setText("⚠️ Budget Limit Exceeded!");
                lblStatusDetail.setText(String.format("You have exceeded your monthly budget by %s (Spent %s of %s). Restrict non-essential expenses immediately.",
                    CurrencyUtil.format(status.getOverspentAmount()),
                    CurrencyUtil.format(status.getSpentAmount()),
                    CurrencyUtil.format(status.getBudgetAmount())));
            } else if (status.isNearBudget()) {
                lblRemainingValue.setText(CurrencyUtil.format(status.getRemainingAmount()));
                lblRemainingValue.setStyle("-fx-text-fill: #D97706;");
                progressBar.getStyleClass().add("progress-warning");
                statusBanner.getStyleClass().add("banner-warning");
                lblStatusHeader.setText("⚠️ Approaching Budget Limit");
                lblStatusDetail.setText(String.format("You have utilized %.1f%% of your budget. Remaining funds: %s.",
                    status.getPercentageSpent(),
                    CurrencyUtil.format(status.getRemainingAmount())));
            } else {
                lblRemainingValue.setText(CurrencyUtil.format(status.getRemainingAmount()));
                lblRemainingValue.setStyle("-fx-text-fill: #00B894;");
                progressBar.getStyleClass().add("progress-safe");
                statusBanner.getStyleClass().add("banner-safe");
                lblStatusHeader.setText("✅ Budget On Track");
                lblStatusDetail.setText(String.format("Spending is within safe limits (%.1f%% utilized). Remaining available: %s.",
                    status.getPercentageSpent(),
                    CurrencyUtil.format(status.getRemainingAmount())));
            }

            // Refresh history table
            List<Budget> allBudgets = budgetService.getAllBudgets();
            historyData.setAll(allBudgets);

        } catch (SQLException e) {
            System.err.println("[BudgetView] Error loading budget status: " + e.getMessage());
        }
    }
}
