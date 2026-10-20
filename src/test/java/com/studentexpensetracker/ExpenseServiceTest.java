package com.studentexpensetracker;

import com.studentexpensetracker.dao.ExpenseDAO;
import com.studentexpensetracker.dao.ExpenseDAOImpl;
import com.studentexpensetracker.database.DatabaseConfig;
import com.studentexpensetracker.database.DatabaseManager;
import com.studentexpensetracker.model.DashboardMetrics;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.service.ExpenseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExpenseService Business Logic Tests")
public class ExpenseServiceTest {

    private File tempDbFile;
    private ExpenseService expenseService;
    private ExpenseDAO expenseDAO;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        tempDbFile = File.createTempFile("test_expense_service_", ".db");
        DatabaseConfig.setCustomDbUrl("jdbc:sqlite:" + tempDbFile.getAbsolutePath());
        DatabaseManager.initializeDatabase();
        try (var conn = DatabaseManager.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM expenses");
            stmt.execute("DELETE FROM budgets");
        }

        expenseDAO = new ExpenseDAOImpl();
        expenseService = new ExpenseService(expenseDAO);
    }

    @AfterEach
    void tearDown() {
        DatabaseConfig.resetCustomDbUrl();
        if (tempDbFile != null && tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }

    @Test
    @DisplayName("Should successfully add valid expense through service")
    void testAddExpenseSuccess() throws SQLException {
        Expense expense = new Expense("Classroom Project Stationery", 150.0, "Education", LocalDate.now(), "UPI", "Chart papers and markers");
        expenseService.addExpense(expense);

        assertNotNull(expense.getId());
        List<Expense> all = expenseService.getAllExpenses();
        assertEquals(1, all.size());
        assertEquals("Classroom Project Stationery", all.get(0).getDescription());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding invalid expense")
    void testAddExpenseValidationFailure() {
        // Empty description and negative amount
        Expense invalid = new Expense("", -200.0, "Food", LocalDate.now(), "UPI", "");
        assertThrows(IllegalArgumentException.class, () -> expenseService.addExpense(invalid));
    }

    @Test
    @DisplayName("Should retrieve correct dashboard metrics")
    void testDashboardMetricsCalculation() throws SQLException {
        LocalDate today = LocalDate.now();
        expenseService.addExpense(new Expense("Morning Breakfast", 80.0, "Food", today, "Cash", ""));
        expenseService.addExpense(new Expense("Metro Train Ticket", 50.0, "Transport", today, "UPI", ""));
        expenseService.addExpense(new Expense("Algorithms Text", 600.0, "Education", today, "Debit Card", ""));

        DashboardMetrics metrics = expenseService.getDashboardMetrics();
        assertNotNull(metrics);
        assertEquals(730.0, metrics.getTotalExpenses(), 0.001);
        assertEquals(730.0, metrics.getTodayExpenses(), 0.001);
        assertEquals(730.0, metrics.getCurrentMonthExpenses(), 0.001);
        assertEquals(3, metrics.getTotalRecords());
        assertEquals("Education", metrics.getHighestSpendingCategory());
        assertEquals(3, metrics.getRecentTransactions().size());
        assertEquals(3, metrics.getCategoryBreakdown().size());
    }

    @Test
    @DisplayName("Should throw exception when updating expense without ID")
    void testUpdateExpenseWithoutId() {
        Expense expense = new Expense("Lunch", 100.0, "Food", LocalDate.now(), "Cash", "");
        assertThrows(IllegalArgumentException.class, () -> expenseService.updateExpense(expense));
    }
}
