package com.studentexpensetracker;

import com.studentexpensetracker.dao.BudgetDAO;
import com.studentexpensetracker.dao.BudgetDAOImpl;
import com.studentexpensetracker.database.DatabaseConfig;
import com.studentexpensetracker.database.DatabaseManager;
import com.studentexpensetracker.model.Budget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BudgetDAO JDBC Integration Tests (CO10)")
public class BudgetDAOTest {

    private File tempDbFile;
    private BudgetDAO budgetDAO;

    @BeforeEach
    void setUp() throws IOException, SQLException {
        tempDbFile = File.createTempFile("test_student_budgets_", ".db");
        DatabaseConfig.setCustomDbUrl("jdbc:sqlite:" + tempDbFile.getAbsolutePath());
        DatabaseManager.initializeDatabase();
        try (var conn = DatabaseManager.getConnection(); var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM expenses");
            stmt.execute("DELETE FROM budgets");
        }
        budgetDAO = new BudgetDAOImpl();
    }

    @AfterEach
    void tearDown() {
        DatabaseConfig.resetCustomDbUrl();
        if (tempDbFile != null && tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }

    @Test
    @DisplayName("Should insert a monthly budget and retrieve it")
    void testSetAndGetBudget() throws SQLException {
        Budget budget = new Budget("2026-11", 12000.0);
        boolean saved = budgetDAO.setOrUpdateBudget(budget);

        assertTrue(saved);

        Budget fetched = budgetDAO.getBudgetByMonthYear("2026-11");
        assertNotNull(fetched);
        assertEquals("2026-11", fetched.getMonthYear());
        assertEquals(12000.0, fetched.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Should upsert / update budget when same month is submitted")
    void testUpsertBudget() throws SQLException {
        budgetDAO.setOrUpdateBudget(new Budget("2026-12", 8000.0));

        // Update amount for the same month
        budgetDAO.setOrUpdateBudget(new Budget("2026-12", 9500.0));

        Budget updated = budgetDAO.getBudgetByMonthYear("2026-12");
        assertNotNull(updated);
        assertEquals(9500.0, updated.getAmount(), 0.001);
    }

    @Test
    @DisplayName("Should retrieve list of all budgets and delete by month")
    void testListAndDeleteBudgets() throws SQLException {
        budgetDAO.setOrUpdateBudget(new Budget("2026-08", 7000.0));
        budgetDAO.setOrUpdateBudget(new Budget("2026-09", 8000.0));

        List<Budget> list = budgetDAO.getAllBudgets();
        assertTrue(list.size() >= 2);

        boolean deleted = budgetDAO.deleteBudget("2026-08");
        assertTrue(deleted);

        Budget afterDelete = budgetDAO.getBudgetByMonthYear("2026-08");
        assertNull(afterDelete);
    }
}
