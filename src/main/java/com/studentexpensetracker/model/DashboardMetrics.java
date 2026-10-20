package com.studentexpensetracker.model;

import java.util.List;

/**
 * Encapsulates aggregated metrics required for the main Dashboard view.
 */
public class DashboardMetrics {
    private double totalExpenses;
    private double todayExpenses;
    private double currentMonthExpenses;
    private int totalRecords;
    private String highestSpendingCategory;
    private List<Expense> recentTransactions;
    private List<CategorySummary> categoryBreakdown;

    public DashboardMetrics(double totalExpenses, double todayExpenses, double currentMonthExpenses,
                            int totalRecords, String highestSpendingCategory,
                            List<Expense> recentTransactions, List<CategorySummary> categoryBreakdown) {
        this.totalExpenses = totalExpenses;
        this.todayExpenses = todayExpenses;
        this.currentMonthExpenses = currentMonthExpenses;
        this.totalRecords = totalRecords;
        this.highestSpendingCategory = highestSpendingCategory;
        this.recentTransactions = recentTransactions;
        this.categoryBreakdown = categoryBreakdown;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public double getTodayExpenses() {
        return todayExpenses;
    }

    public double getCurrentMonthExpenses() {
        return currentMonthExpenses;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public String getHighestSpendingCategory() {
        return highestSpendingCategory;
    }

    public List<Expense> getRecentTransactions() {
        return recentTransactions;
    }

    public List<CategorySummary> getCategoryBreakdown() {
        return categoryBreakdown;
    }
}
