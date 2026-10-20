package com.studentexpensetracker.ui;

import com.studentexpensetracker.model.CategorySummary;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.model.MonthlySummary;
import com.studentexpensetracker.service.ExpenseService;
import com.studentexpensetracker.util.CurrencyUtil;
import com.studentexpensetracker.util.DateUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

/**
 * Analytical Reports View (CO5 charts and TableView).
 * Summarizes category totals, monthly trends, and standout transactions.
 */
public class ReportsView extends ScrollPane {

    private final ExpenseService expenseService;

    // Top Highlight Card Labels
    private final Label lblMostExpensiveDesc = new Label("None");
    private final Label lblMostExpensiveAmt = new Label("₹ 0.00");
    private final Label lblMostExpensiveMeta = new Label("No transactions available");

    // Category Table & Pie Chart
    private final TableView<CategorySummary> categoryTable = new TableView<>();
    private final ObservableList<CategorySummary> categoryData = FXCollections.observableArrayList();
    private final PieChart categoryPieChart = new PieChart();

    // Monthly Table & Bar Chart
    private final TableView<MonthlySummary> monthlyTable = new TableView<>();
    private final ObservableList<MonthlySummary> monthlyData = FXCollections.observableArrayList();
    private final CategoryAxis xAxis = new CategoryAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final BarChart<String, Number> monthlyBarChart = new BarChart<>(xAxis, yAxis);

    public ReportsView(ExpenseService expenseService) {
        this.expenseService = expenseService;

        initializeUI();
        refresh();
    }

    private void initializeUI() {
        setFitToWidth(true);
        getStyleClass().add("scroll-pane");

        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(24));
        contentBox.setMaxWidth(1300);

        // 1. Standout Highlights Card
        VBox highlightCard = new VBox(10);
        highlightCard.getStyleClass().add("card");

        Label highlightTitle = new Label("Highest Single Expense Transaction");
        highlightTitle.getStyleClass().add("card-title");

        HBox highlightRow = new HBox(20);
        highlightRow.setAlignment(Pos.CENTER_LEFT);

        VBox detailsBox = new VBox(4);
        lblMostExpensiveDesc.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2D3436;");
        lblMostExpensiveMeta.setStyle("-fx-font-size: 12px; -fx-text-fill: #636E72;");
        detailsBox.getChildren().addAll(lblMostExpensiveDesc, lblMostExpensiveMeta);

        lblMostExpensiveAmt.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #6C5CE7;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        highlightRow.getChildren().addAll(detailsBox, spacer, lblMostExpensiveAmt);
        highlightCard.getChildren().addAll(highlightTitle, highlightRow);

        // 2. Category-wise Spending Section (Table + Pie Chart)
        VBox catSection = new VBox(12);
        catSection.getStyleClass().add("card");

        Label catTitle = new Label("Category-wise Spending Breakdown");
        catTitle.getStyleClass().add("card-title");

        HBox catContent = new HBox(20);
        catContent.setAlignment(Pos.CENTER);

        configureCategoryTable();
        categoryTable.setPrefHeight(280);
        categoryTable.setMinWidth(380);
        HBox.setHgrow(categoryTable, Priority.ALWAYS);

        categoryPieChart.setPrefHeight(280);
        categoryPieChart.setMinWidth(420);
        HBox.setHgrow(categoryPieChart, Priority.ALWAYS);

        catContent.getChildren().addAll(categoryTable, categoryPieChart);
        catSection.getChildren().addAll(catTitle, catContent);

        // 3. Monthly Trends Section (Table + Bar Chart)
        VBox monthSection = new VBox(12);
        monthSection.getStyleClass().add("card");

        Label monthTitle = new Label("Monthly Expense Trends");
        monthTitle.getStyleClass().add("card-title");

        HBox monthContent = new HBox(20);
        monthContent.setAlignment(Pos.CENTER);

        configureMonthlyTable();
        monthlyTable.setPrefHeight(280);
        monthlyTable.setMinWidth(360);
        HBox.setHgrow(monthlyTable, Priority.ALWAYS);

        xAxis.setLabel("Month");
        yAxis.setLabel("Amount (₹)");
        monthlyBarChart.setLegendVisible(false);
        monthlyBarChart.setPrefHeight(280);
        monthlyBarChart.setMinWidth(440);
        HBox.setHgrow(monthlyBarChart, Priority.ALWAYS);

        monthContent.getChildren().addAll(monthlyTable, monthlyBarChart);
        monthSection.getChildren().addAll(monthTitle, monthContent);

        contentBox.getChildren().addAll(highlightCard, catSection, monthSection);
        setContent(contentBox);
    }

    private void configureCategoryTable() {
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CategorySummary, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));

        TableColumn<CategorySummary, String> colTotal = new TableColumn<>("Total Amount");
        colTotal.setCellValueFactory(cell -> new SimpleStringProperty(CurrencyUtil.format(cell.getValue().getTotalAmount())));

        TableColumn<CategorySummary, Integer> colCount = new TableColumn<>("Transactions");
        colCount.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTransactionCount()));

        TableColumn<CategorySummary, String> colPct = new TableColumn<>("Share (%)");
        colPct.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.1f%%", cell.getValue().getPercentage())));

        categoryTable.getColumns().addAll(colCat, colTotal, colCount, colPct);
        categoryTable.setPlaceholder(new Label("No category data recorded yet."));
        categoryTable.setItems(categoryData);
    }

    private void configureMonthlyTable() {
        monthlyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<MonthlySummary, String> colMonth = new TableColumn<>("Month / Year");
        colMonth.setCellValueFactory(cell -> new SimpleStringProperty(DateUtil.formatMonthDisplay(cell.getValue().getMonthYear())));

        TableColumn<MonthlySummary, String> colTotal = new TableColumn<>("Total Spent");
        colTotal.setCellValueFactory(cell -> new SimpleStringProperty(CurrencyUtil.format(cell.getValue().getTotalAmount())));

        TableColumn<MonthlySummary, Integer> colCount = new TableColumn<>("Transactions");
        colCount.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTransactionCount()));

        monthlyTable.getColumns().addAll(colMonth, colTotal, colCount);
        monthlyTable.setPlaceholder(new Label("No monthly data recorded yet."));
        monthlyTable.setItems(monthlyData);
    }

    public void refresh() {
        try {
            // 1. Most expensive transaction
            Expense highest = expenseService.getMostExpensiveTransaction();
            if (highest != null) {
                lblMostExpensiveDesc.setText(highest.getDescription());
                lblMostExpensiveAmt.setText(CurrencyUtil.format(highest.getAmount()));
                lblMostExpensiveMeta.setText(String.format("Category: %s | Date: %s | Payment: %s",
                    highest.getCategory(),
                    DateUtil.formatDisplay(highest.getDate()),
                    highest.getPaymentMethod()));
            } else {
                lblMostExpensiveDesc.setText("No Records");
                lblMostExpensiveAmt.setText("₹ 0.00");
                lblMostExpensiveMeta.setText("No expense records found in database.");
            }

            // 2. Category summaries
            List<CategorySummary> catList = expenseService.getCategoryBreakdown();
            categoryData.setAll(catList);

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (CategorySummary cs : catList) {
                if (cs.getTotalAmount() > 0) {
                    pieData.add(new PieChart.Data(cs.getCategory() + " (" + CurrencyUtil.format(cs.getTotalAmount()) + ")", cs.getTotalAmount()));
                }
            }
            categoryPieChart.setData(pieData);

            // 3. Monthly summaries
            List<MonthlySummary> monthList = expenseService.getMonthlyBreakdown();
            monthlyData.setAll(monthList);

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Monthly Spending");
            for (MonthlySummary ms : monthList) {
                series.getData().add(new XYChart.Data<>(DateUtil.formatMonthDisplay(ms.getMonthYear()), ms.getTotalAmount()));
            }

            monthlyBarChart.getData().clear();
            if (!series.getData().isEmpty()) {
                monthlyBarChart.getData().add(series);
            }

        } catch (SQLException e) {
            System.err.println("[ReportsView] Error generating analytical reports: " + e.getMessage());
        }
    }
}
