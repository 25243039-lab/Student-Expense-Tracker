package com.studentexpensetracker.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class representing an individual expense transaction.
 * Demonstrates clean encapsulation for CO5 TableView data binding and CO10 JDBC persistence.
 */
public class Expense {
    private Integer id;
    private String description;
    private double amount;
    private String category;
    private LocalDate date;
    private String paymentMethod;
    private String notes;
    private String createdAt;

    // Default constructor
    public Expense() {
    }

    // Constructor for creating new expense (before DB auto-increment ID assignment)
    public Expense(String description, double amount, String category, LocalDate date, String paymentMethod, String notes) {
        this(null, description, amount, category, date, paymentMethod, notes, null);
    }

    // Full constructor
    public Expense(Integer id, String description, double amount, String category, LocalDate date, String paymentMethod, String notes, String createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.paymentMethod = paymentMethod;
        this.notes = notes != null ? notes : "";
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Objects.equals(id, expense.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", date=" + date +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }
}
