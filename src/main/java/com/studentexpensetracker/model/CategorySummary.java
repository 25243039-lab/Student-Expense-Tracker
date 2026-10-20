package com.studentexpensetracker.model;

/**
 * Aggregation model for category-wise expense reporting and chart visualization.
 */
public class CategorySummary {
    private String category;
    private double totalAmount;
    private int transactionCount;
    private double percentage;

    public CategorySummary() {
    }

    public CategorySummary(String category, double totalAmount, int transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.percentage = 0.0;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public String toString() {
        return "CategorySummary{" +
                "category='" + category + '\'' +
                ", totalAmount=" + totalAmount +
                ", transactionCount=" + transactionCount +
                ", percentage=" + percentage +
                '}';
    }
}
