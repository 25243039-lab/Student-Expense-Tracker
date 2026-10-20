package com.studentexpensetracker.ui;

import com.studentexpensetracker.service.BudgetService;
import com.studentexpensetracker.service.ExpenseService;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

/**
 * Root container view managing tabs and global data synchronization.
 */
public class MainView extends BorderPane {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    private final DashboardView dashboardView;
    private final ExpenseManagementView expenseManagementView;
    private final BudgetView budgetView;
    private final ReportsView reportsView;

    private final StackPane contentArea = new StackPane();

    public MainView(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;

        // Instantiate sub-views with bidirectional refresh callbacks
        this.dashboardView = new DashboardView(expenseService);
        this.expenseManagementView = new ExpenseManagementView(expenseService, this::onGlobalDataChanged);
        this.budgetView = new BudgetView(budgetService, this::onGlobalDataChanged);
        this.reportsView = new ReportsView(expenseService);

        initializeUI();
    }

    private void initializeUI() {
        NavigationBar navBar = new NavigationBar(this::switchTab);
        setTop(navBar);

        contentArea.getChildren().addAll(dashboardView, expenseManagementView, budgetView, reportsView);
        setCenter(contentArea);

        // Show Dashboard initially
        switchTab(NavigationBar.Tab.DASHBOARD);
    }

    private void switchTab(NavigationBar.Tab tab) {
        // Toggle visibility
        dashboardView.setVisible(tab == NavigationBar.Tab.DASHBOARD);
        expenseManagementView.setVisible(tab == NavigationBar.Tab.EXPENSES);
        budgetView.setVisible(tab == NavigationBar.Tab.BUDGET);
        reportsView.setVisible(tab == NavigationBar.Tab.REPORTS);

        // Refresh currently viewed tab data
        switch (tab) {
            case DASHBOARD -> dashboardView.refresh();
            case EXPENSES -> expenseManagementView.refresh();
            case BUDGET -> budgetView.refresh();
            case REPORTS -> reportsView.refresh();
        }
    }

    private void onGlobalDataChanged() {
        dashboardView.refresh();
        budgetView.refresh();
        reportsView.refresh();
    }
}
