package com.studentexpensetracker.model;

/**
 * Aggregation model for month-wise expense reporting and chart visualization.
 */
public class MonthlySummary {
    private String monthYear; // e.g. "2026-10"
    private double totalAmount;
    private int transactionCount;

    public MonthlySummary() {
    }

    public MonthlySummary(String monthYear, double totalAmount, int transactionCount) {
        this.monthYear = monthYear;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(int transactionCount) {
        this.transactionCount = transactionCount;
    }

    @Override
    public String toString() {
        return "MonthlySummary{" +
                "monthYear='" + monthYear + '\'' +
                ", totalAmount=" + totalAmount +
                ", transactionCount=" + transactionCount +
                '}';
    }
}
