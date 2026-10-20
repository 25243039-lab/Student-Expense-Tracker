package com.studentexpensetracker;

import com.studentexpensetracker.dao.ExpenseDAO;
import com.studentexpensetracker.dao.ExpenseDAOImpl;
import com.studentexpensetracker.database.DatabaseConfig;
import com.studentexpensetracker.database.DatabaseManager;
import com.studentexpensetracker.model.CategorySummary;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.model.MonthlySummary;
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

@DisplayName("ExpenseDAO JDBC Integration Tests (CO10)")
public class ExpenseDAOTest {

    private File tempDbFile;
    private ExpenseDAO expenseDAO;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        tempDbFile = File.createTempFile("test_student_expenses_", ".db");
        DatabaseConfig.setCustomDbUrl("jdbc:sqlite:" + tempDbFile.getAbsolutePath());
        DatabaseManager.initializeDatabase();
        try (var conn = DatabaseManager.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM expenses");
            stmt.execute("DELETE FROM budgets");
        }
        expenseDAO = new ExpenseDAOImpl();
    }

    @AfterEach
    void tearDown() {
        DatabaseConfig.resetCustomDbUrl();
        if (tempDbFile != null && tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }

    @Test
    @DisplayName("Should successfully insert and retrieve an expense")
    void testAddAndGetExpense() throws SQLException {
        Expense expense = new Expense("Reference Book", 450.0, "Education", LocalDate.now(), "UPI", "Semester 5 book");
        boolean added = expenseDAO.addExpense(expense);

        assertTrue(added);
        assertNotNull(expense.getId());
        assertTrue(expense.getId() > 0);

        Expense fetched = expenseDAO.getExpenseById(expense.getId());
        assertNotNull(fetched);
        assertEquals("Reference Book", fetched.getDescription());
        assertEquals(450.0, fetched.getAmount(), 0.001);
        assertEquals("Education", fetched.getCategory());
        assertEquals("UPI", fetched.getPaymentMethod());
    }

    @Test
    @DisplayName("Should update an existing expense record")
    void testUpdateExpense() throws SQLException {
        Expense expense = new Expense("Hostel Snacks", 150.0, "Food", LocalDate.now(), "Cash", "");
        expenseDAO.addExpense(expense);

        expense.setDescription("Hostel Evening Tea & Snacks");
        expense.setAmount(180.0);
        expense.setPaymentMethod("UPI");

        boolean updated = expenseDAO.updateExpense(expense);
        assertTrue(updated);

        Expense fetched = expenseDAO.getExpenseById(expense.getId());
        assertEquals("Hostel Evening Tea & Snacks", fetched.getDescription());
        assertEquals(180.0, fetched.getAmount(), 0.001);
        assertEquals("UPI", fetched.getPaymentMethod());
    }

    @Test
    @DisplayName("Should delete an expense record")
    void testDeleteExpense() throws SQLException {
        Expense expense = new Expense("Movie Ticket", 300.0, "Entertainment", LocalDate.now(), "UPI", "");
        expenseDAO.addExpense(expense);

        int id = expense.getId();
        boolean deleted = expenseDAO.deleteExpense(id);
        assertTrue(deleted);

        Expense fetched = expenseDAO.getExpenseById(id);
        assertNull(fetched);
    }

    @Test
    @DisplayName("Should filter expenses by keyword, category, date range, and payment method")
    void testSearchAndFilter() throws SQLException {
        LocalDate today = LocalDate.now();
        expenseDAO.addExpense(new Expense("Physics Lab Manual", 200.0, "Education", today, "UPI", "Lab practicals"));
        expenseDAO.addExpense(new Expense("Cafeteria Lunch", 120.0, "Food", today, "Cash", "Combo meal"));
        expenseDAO.addExpense(new Expense("City Bus Ticket", 30.0, "Transport", today.minusDays(2), "Cash", "Daily commute"));

        // Filter by keyword
        List<Expense> byKeyword = expenseDAO.searchAndFilter("physics", "All Categories", null, null, "All Methods");
        assertEquals(1, byKeyword.size());
        assertEquals("Physics Lab Manual", byKeyword.get(0).getDescription());

        // Filter by category
        List<Expense> byCategory = expenseDAO.searchAndFilter(null, "Food", null, null, "All Methods");
        assertEquals(1, byCategory.size());
        assertEquals("Cafeteria Lunch", byCategory.get(0).getDescription());

        // Filter by payment method
        List<Expense> byPayment = expenseDAO.searchAndFilter(null, "All Categories", null, null, "Cash");
        assertEquals(2, byPayment.size());
    }

    @Test
    @DisplayName("Should correctly aggregate totals and highest spending category")
    void testAggregations() throws SQLException {
        LocalDate today = LocalDate.now();
        String currentMonth = today.toString().substring(0, 7);

        expenseDAO.addExpense(new Expense("Tuition Fee Installment", 5000.0, "Education", today, "Net Banking", "Semester fee"));
        expenseDAO.addExpense(new Expense("Canteen Lunch", 200.0, "Food", today, "UPI", "Lunch"));

        double total = expenseDAO.getTotalExpenses();
        assertTrue(total >= 5200.0);

        double todayTotal = expenseDAO.getTodayExpenses(today);
        assertTrue(todayTotal >= 5200.0);

        double monthTotal = expenseDAO.getMonthlyExpenses(currentMonth);
        assertTrue(monthTotal >= 5200.0);

        String topCat = expenseDAO.getHighestSpendingCategory();
        assertEquals("Education", topCat);

        Expense mostExpensive = expenseDAO.getMostExpensiveExpense();
        assertNotNull(mostExpensive);
        assertEquals("Tuition Fee Installment", mostExpensive.getDescription());
    }
}
