package com.studentexpensetracker.ui;

import com.studentexpensetracker.model.CategorySummary;
import com.studentexpensetracker.model.DashboardMetrics;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.service.ExpenseService;
import com.studentexpensetracker.util.CurrencyUtil;
import com.studentexpensetracker.util.DateUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

/**
 * Dashboard View (CO5 JavaFX component).
 * Displays key financial metrics, category-wise spending chart, and recent transactions.
 */
public class DashboardView extends ScrollPane {

    private final ExpenseService expenseService;

    // Metric Labels
    private final Label lblTotalValue = new Label("₹ 0.00");
    private final Label lblTodayValue = new Label("₹ 0.00");
    private final Label lblMonthValue = new Label("₹ 0.00");
    private final Label lblRecordCount = new Label("0 Records");
    private final Label lblTopCategory = new Label("None");

    // Chart & Table
    private final PieChart pieChart = new PieChart();
    private final TableView<Expense> recentTable = new TableView<>();
    private final StackPane chartContainer = new StackPane();
    private final Label lblEmptyChart = new Label("No expense data recorded yet.");

    public DashboardView(ExpenseService expenseService) {
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

        // 1. Metric Summary Cards Grid
        GridPane metricsGrid = new GridPane();
        metricsGrid.setHgap(16);
        metricsGrid.setVgap(16);

        VBox cardTotal = createMetricCard("TOTAL SPENT", lblTotalValue, "All-time expenses", "metric-card-lavender", "metric-value-purple");
        VBox cardMonth = createMetricCard("THIS MONTH", lblMonthValue, "Current calendar month", "metric-card-blue", "metric-value-blue");
        VBox cardToday = createMetricCard("TODAY", lblTodayValue, "Today's transactions", "metric-card-mint", "metric-value-mint");
        VBox cardStats = createMetricCard("RECORDS & TOP SPEND", lblRecordCount, "Top: " + lblTopCategory.getText(), "metric-card", "metric-value");

        // Make metric cards expand evenly across the grid
        GridPane.setHgrow(cardTotal, Priority.ALWAYS);
        GridPane.setHgrow(cardMonth, Priority.ALWAYS);
        GridPane.setHgrow(cardToday, Priority.ALWAYS);
        GridPane.setHgrow(cardStats, Priority.ALWAYS);

        metricsGrid.add(cardTotal, 0, 0);
        metricsGrid.add(cardMonth, 1, 0);
        metricsGrid.add(cardToday, 2, 0);
        metricsGrid.add(cardStats, 3, 0);

        // 2. Middle Row: Spending Breakdown Chart (Left) & Recent Transactions (Right)
        HBox middleRow = new HBox(20);
        HBox.setHgrow(middleRow, Priority.ALWAYS);

        // Left Card: Pie Chart
        VBox chartCard = new VBox(12);
        chartCard.getStyleClass().add("card");
        HBox.setHgrow(chartCard, Priority.ALWAYS);
        chartCard.setMinWidth(420);

        Label chartTitle = new Label("Category-wise Spending Breakdown");
        chartTitle.getStyleClass().add("card-title");

        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);
        pieChart.setPrefHeight(320);

        lblEmptyChart.getStyleClass().add("empty-state-desc");
        chartContainer.getChildren().addAll(pieChart, lblEmptyChart);

        chartCard.getChildren().addAll(chartTitle, chartContainer);

        // Right Card: Recent Transactions Table
        VBox recentCard = new VBox(12);
        recentCard.getStyleClass().add("card");
        HBox.setHgrow(recentCard, Priority.ALWAYS);
        recentCard.setMinWidth(460);

        Label recentTitle = new Label("Recent Transactions");
        recentTitle.getStyleClass().add("card-title");

        configureRecentTable();
        recentTable.setPrefHeight(320);

        recentCard.getChildren().addAll(recentTitle, recentTable);

        middleRow.getChildren().addAll(chartCard, recentCard);

        contentBox.getChildren().addAll(metricsGrid, middleRow);
        setContent(contentBox);
    }

    private VBox createMetricCard(String title, Label valueLabel, String subtitle, String cardClass, String valueClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("metric-card", cardClass);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("metric-label");

        valueLabel.getStyleClass().addAll("metric-value", valueClass);

        Label lblSubtitle = new Label(subtitle);
        lblSubtitle.getStyleClass().add("empty-state-desc");

        card.getChildren().addAll(lblTitle, valueLabel, lblSubtitle);
        return card;
    }

    private void configureRecentTable() {
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Expense, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(DateUtil.formatDisplay(cell.getValue().getDate())));
        colDate.setPrefWidth(90);

        TableColumn<Expense, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDescription()));
        colDesc.setPrefWidth(160);

        TableColumn<Expense, String> colCat = new TableColumn<>("Category");
        colCat.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCategory()));
        colCat.setPrefWidth(100);

        TableColumn<Expense, String> colAmount = new TableColumn<>("Amount");
        colAmount.setCellValueFactory(cell -> new SimpleStringProperty(CurrencyUtil.format(cell.getValue().getAmount())));
        colAmount.setPrefWidth(95);

        recentTable.getColumns().addAll(colDate, colDesc, colCat, colAmount);
        recentTable.setPlaceholder(new Label("No recent transactions."));
    }

    /**
     * Refreshes metrics, chart, and recent transaction list from SQLite database.
     */
    public void refresh() {
        try {
            DashboardMetrics metrics = expenseService.getDashboardMetrics();

            lblTotalValue.setText(CurrencyUtil.format(metrics.getTotalExpenses()));
            lblTodayValue.setText(CurrencyUtil.format(metrics.getTodayExpenses()));
            lblMonthValue.setText(CurrencyUtil.format(metrics.getCurrentMonthExpenses()));
            lblRecordCount.setText(metrics.getTotalRecords() + " Records");
            lblTopCategory.setText(metrics.getHighestSpendingCategory());

            // Update Chart
            List<CategorySummary> summaries = metrics.getCategoryBreakdown();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (CategorySummary cs : summaries) {
                if (cs.getTotalAmount() > 0) {
                    pieData.add(new PieChart.Data(cs.getCategory() + " (" + CurrencyUtil.format(cs.getTotalAmount()) + ")", cs.getTotalAmount()));
                }
            }

            pieChart.setData(pieData);
            boolean hasData = !pieData.isEmpty();
            pieChart.setVisible(hasData);
            lblEmptyChart.setVisible(!hasData);

            // Update Recent Table
            recentTable.setItems(FXCollections.observableArrayList(metrics.getRecentTransactions()));

        } catch (SQLException e) {
            System.err.println("[DashboardView] Error refreshing metrics: " + e.getMessage());
        }
    }
}
