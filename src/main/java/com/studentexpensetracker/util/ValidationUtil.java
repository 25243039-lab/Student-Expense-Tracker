package com.studentexpensetracker.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for validating user input across forms (CO5 and quality requirement).
 * Guarantees that only valid, sanitized data reaches the database layer.
 */
public class ValidationUtil {

    /**
     * Validates all fields of an expense entry.
     * Returns an empty list if valid, or a list of specific error descriptions.
     */
    public static List<String> validateExpense(String description, String amountText, String category, LocalDate date, String paymentMethod) {
        List<String> errors = new ArrayList<>();

        // Description validation
        if (description == null || description.trim().isEmpty()) {
            errors.add("Expense description cannot be empty.");
        } else if (description.trim().length() > 100) {
            errors.add("Description cannot exceed 100 characters.");
        }

        // Amount validation
        if (amountText == null || amountText.trim().isEmpty()) {
            errors.add("Amount is required.");
        } else {
            try {
                double amount = Double.parseDouble(amountText.trim());
                if (amount <= 0) {
                    errors.add("Amount must be greater than zero.");
                } else if (amount > 10_000_000) {
                    errors.add("Amount exceeds reasonable limit (10,000,000).");
                }
            } catch (NumberFormatException e) {
                errors.add("Amount must be a valid positive number.");
            }
        }

        // Category validation
        if (category == null || category.trim().isEmpty() || category.equalsIgnoreCase("Select Category")) {
            errors.add("Please select an expense category.");
        }

        // Date validation
        if (date == null) {
            errors.add("Please pick a valid date.");
        } else if (date.isAfter(LocalDate.now())) {
            errors.add("Expense date cannot be in the future.");
        }

        // Payment Method validation
        if (paymentMethod == null || paymentMethod.trim().isEmpty() || paymentMethod.equalsIgnoreCase("Select Payment Method")) {
            errors.add("Please select a payment method.");
        }

        return errors;
    }

    /**
     * Validates monthly budget form input.
     */
    public static List<String> validateBudget(String monthYear, String amountText) {
        List<String> errors = new ArrayList<>();

        if (monthYear == null || !monthYear.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            errors.add("Month/Year must follow the 'YYYY-MM' format (e.g., 2026-10).");
        }

        if (amountText == null || amountText.trim().isEmpty()) {
            errors.add("Budget amount is required.");
        } else {
            try {
                double amount = Double.parseDouble(amountText.trim());
                if (amount <= 0) {
                    errors.add("Budget amount must be strictly greater than zero.");
                }
            } catch (NumberFormatException e) {
                errors.add("Budget amount must be a valid number.");
            }
        }

        return errors;
    }
}
