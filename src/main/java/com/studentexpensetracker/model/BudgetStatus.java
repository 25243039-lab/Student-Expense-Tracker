package com.studentexpensetracker.model;

/**
 * Financial status helper for budget tracking.
 * Strictly guarantees that negative remaining balances are NOT displayed as available funds.
 */
public class BudgetStatus {
    private String monthYear;
    private double budgetAmount;
    private double spentAmount;
    private double remainingAmount;
    private double overspentAmount;
    private double percentageSpent;
    private boolean overBudget;
    private boolean nearBudget; // >= 80% and <= 100%

    public BudgetStatus(String monthYear, double budgetAmount, double spentAmount) {
        this.monthYear = monthYear;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;

        if (budgetAmount > 0) {
            this.percentageSpent = (spentAmount / budgetAmount) * 100.0;
        } else {
            this.percentageSpent = spentAmount > 0 ? 100.0 : 0.0;
        }

        if (spentAmount > budgetAmount) {
            this.overBudget = true;
            this.nearBudget = false;
            this.remainingAmount = 0.0; // Never show negative balance as available money
            this.overspentAmount = spentAmount - budgetAmount;
        } else {
            this.overBudget = false;
            this.remainingAmount = budgetAmount - spentAmount;
            this.overspentAmount = 0.0;
            this.nearBudget = this.percentageSpent >= 80.0;
        }
    }

    public String getMonthYear() {
        return monthYear;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public double getOverspentAmount() {
        return overspentAmount;
    }

    public double getPercentageSpent() {
        return percentageSpent;
    }

    public boolean isOverBudget() {
        return overBudget;
    }

    public boolean isNearBudget() {
        return nearBudget;
    }

    public boolean isBudgetSet() {
        return budgetAmount > 0;
    }
}
