package com.studentexpensetracker.model;

import java.util.Objects;

/**
 * Model class representing a student's monthly budget.
 * Connects directly to the SQLite 'budgets' table for tracking financial limits.
 */
public class Budget {
    private Integer id;
    private String monthYear; // Format: "YYYY-MM", e.g. "2026-10"
    private double amount;
    private String updatedAt;

    public Budget() {
    }

    public Budget(String monthYear, double amount) {
        this(null, monthYear, amount, null);
    }

    public Budget(Integer id, String monthYear, double amount, String updatedAt) {
        this.id = id;
        this.monthYear = monthYear;
        this.amount = amount;
        this.updatedAt = updatedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMonthYear() {
        return monthYear;
    }

    public void setMonthYear(String monthYear) {
        this.monthYear = monthYear;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return Objects.equals(id, budget.id) || Objects.equals(monthYear, budget.monthYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, monthYear);
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", monthYear='" + monthYear + '\'' +
                ", amount=" + amount +
                '}';
    }
}
