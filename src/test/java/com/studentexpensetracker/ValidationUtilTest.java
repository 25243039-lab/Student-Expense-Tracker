package com.studentexpensetracker;

import com.studentexpensetracker.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtil Unit Tests")
public class ValidationUtilTest {

    @Test
    @DisplayName("Valid expense data should produce zero errors")
    void testValidExpense() {
        List<String> errors = ValidationUtil.validateExpense(
            "Campus Canteen Lunch",
            "120.50",
            "Food",
            LocalDate.now(),
            "UPI"
        );
        assertTrue(errors.isEmpty(), "Valid expense should have no errors");
    }

    @Test
    @DisplayName("Empty description should fail validation")
    void testEmptyDescription() {
        List<String> errors = ValidationUtil.validateExpense(
            "",
            "100.00",
            "Food",
            LocalDate.now(),
            "Cash"
        );
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("description")));
    }

    @Test
    @DisplayName("Negative or zero amount should fail validation")
    void testNegativeAmount() {
        List<String> errorsZero = ValidationUtil.validateExpense("Snack", "0", "Food", LocalDate.now(), "Cash");
        assertFalse(errorsZero.isEmpty());

        List<String> errorsNegative = ValidationUtil.validateExpense("Snack", "-50.0", "Food", LocalDate.now(), "Cash");
        assertFalse(errorsNegative.isEmpty());
        assertTrue(errorsNegative.stream().anyMatch(e -> e.contains("greater than zero")));
    }

    @Test
    @DisplayName("Non-numeric amount should fail validation")
    void testNonNumericAmount() {
        List<String> errors = ValidationUtil.validateExpense("Snack", "abc", "Food", LocalDate.now(), "Cash");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("valid positive number")));
    }

    @Test
    @DisplayName("Future date should fail validation")
    void testFutureDate() {
        List<String> errors = ValidationUtil.validateExpense(
            "Concert",
            "500",
            "Entertainment",
            LocalDate.now().plusDays(5),
            "UPI"
        );
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("future")));
    }

    @Test
    @DisplayName("Missing category should fail validation")
    void testMissingCategory() {
        List<String> errors = ValidationUtil.validateExpense(
            "Notebook",
            "50",
            "",
            LocalDate.now(),
            "UPI"
        );
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("category")));
    }

    @Test
    @DisplayName("Valid budget input should pass")
    void testValidBudget() {
        List<String> errors = ValidationUtil.validateBudget("2026-10", "15000");
        assertTrue(errors.isEmpty());
    }

    @Test
    @DisplayName("Invalid budget month-year format should fail")
    void testInvalidBudgetMonthFormat() {
        List<String> errors = ValidationUtil.validateBudget("10-2026", "15000");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("YYYY-MM")));
    }

    @Test
    @DisplayName("Negative budget amount should fail")
    void testNegativeBudgetAmount() {
        List<String> errors = ValidationUtil.validateBudget("2026-10", "-500");
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("greater than zero")));
    }
}
