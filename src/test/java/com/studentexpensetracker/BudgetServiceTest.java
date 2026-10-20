package com.studentexpensetracker;

import com.studentexpensetracker.dao.BudgetDAO;
import com.studentexpensetracker.dao.BudgetDAOImpl;
import com.studentexpensetracker.dao.ExpenseDAO;
import com.studentexpensetracker.dao.ExpenseDAOImpl;
import com.studentexpensetracker.database.DatabaseConfig;
import com.studentexpensetracker.database.DatabaseManager;
import com.studentexpensetracker.model.Budget;
import com.studentexpensetracker.model.BudgetStatus;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.service.BudgetService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BudgetService Business Logic Tests")
public class BudgetServiceTest {

    private File tempDbFile;
    private BudgetService budgetService;
    private ExpenseDAO expenseDAO;
    private BudgetDAO budgetDAO;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        tempDbFile = File.createTempFile("test_budget_service_", ".db");
        DatabaseConfig.setCustomDbUrl("jdbc:sqlite:" + tempDbFile.getAbsolutePath());
        DatabaseManager.initializeDatabase();
        try (var conn = DatabaseManager.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM expenses");
            stmt.execute("DELETE FROM budgets");
        }

        expenseDAO = new ExpenseDAOImpl();
        budgetDAO = new BudgetDAOImpl();
        budgetService = new BudgetService(budgetDAO, expenseDAO);
    }

    @AfterEach
    void tearDown() {
        DatabaseConfig.resetCustomDbUrl();
        if (tempDbFile != null && tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }

    @Test
    @DisplayName("Under budget: remaining balance calculated correctly and overspent is zero")
    void testUnderBudget() throws SQLException {
        String testMonth = "2026-05";
        budgetDAO.setOrUpdateBudget(new Budget(testMonth, 10000.0));

        expenseDAO.addExpense(new Expense("Course Books", 3000.0, "Education", LocalDate.parse("2026-05-10"), "UPI", ""));

        BudgetStatus status = budgetService.getBudgetStatus(testMonth);
        assertEquals(10000.0, status.getBudgetAmount(), 0.001);
        assertEquals(3000.0, status.getSpentAmount(), 0.001);
        assertEquals(7000.0, status.getRemainingAmount(), 0.001);
        assertEquals(0.0, status.getOverspentAmount(), 0.001);
        assertFalse(status.isOverBudget());
        assertFalse(status.isNearBudget());
    }

    @Test
    @DisplayName("Near budget: flags near budget when spending reaches or exceeds 80%")
    void testNearBudget() throws SQLException {
        String testMonth = "2026-06";
        budgetDAO.setOrUpdateBudget(new Budget(testMonth, 10000.0));

        expenseDAO.addExpense(new Expense("Hostel Rent Share", 8500.0, "Others", LocalDate.parse("2026-06-05"), "Net Banking", ""));

        BudgetStatus status = budgetService.getBudgetStatus(testMonth);
        assertEquals(1500.0, status.getRemainingAmount(), 0.001);
        assertFalse(status.isOverBudget());
        assertTrue(status.isNearBudget());
        assertEquals(85.0, status.getPercentageSpent(), 0.1);
    }

    @Test
    @DisplayName("Over budget: remaining balance must be zero (NEVER negative available funds) and overspent deficit recorded")
    void testOverBudgetNoNegativeRemainingBalance() throws SQLException {
        String testMonth = "2026-07";
        budgetDAO.setOrUpdateBudget(new Budget(testMonth, 5000.0));

        expenseDAO.addExpense(new Expense("Laptop Repair", 7500.0, "Education", LocalDate.parse("2026-07-15"), "Debit Card", "Motherboard repair"));

        BudgetStatus status = budgetService.getBudgetStatus(testMonth);
        assertTrue(status.isOverBudget());
        // CRITICAL REQUIREMENT: Do not display a negative remaining balance as if it were money available to spend!
        assertEquals(0.0, status.getRemainingAmount(), 0.001, "Remaining balance must be capped at 0.0 when over budget");
        assertEquals(2500.0, status.getOverspentAmount(), 0.001, "Overspent deficit must be correctly recorded");
        assertEquals(150.0, status.getPercentageSpent(), 0.1);
    }
}
