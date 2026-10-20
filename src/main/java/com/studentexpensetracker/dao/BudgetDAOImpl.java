package com.studentexpensetracker.dao;

import com.studentexpensetracker.database.DatabaseManager;
import com.studentexpensetracker.model.Budget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete JDBC implementation of BudgetDAO (CO10 requirement).
 * Manages persistence for student monthly budget targets.
 */
public class BudgetDAOImpl implements BudgetDAO {

    @Override
    public boolean setOrUpdateBudget(Budget budget) throws SQLException {
        // SQLite upsert syntax (ON CONFLICT DO UPDATE)
        String sql = """
            INSERT INTO budgets (month_year, amount, updated_at)
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT(month_year) DO UPDATE SET
                amount = excluded.amount,
                updated_at = CURRENT_TIMESTAMP
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, budget.getMonthYear().trim());
            pstmt.setDouble(2, budget.getAmount());

            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public Budget getBudgetByMonthYear(String monthYear) throws SQLException {
        String sql = "SELECT id, month_year, amount, updated_at FROM budgets WHERE month_year = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, monthYear.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String my = rs.getString("month_year");
                    double amt = rs.getDouble("amount");
                    String updated = rs.getString("updated_at");
                    return new Budget(id, my, amt, updated);
                }
            }
        }
        return null;
    }

    @Override
    public List<Budget> getAllBudgets() throws SQLException {
        String sql = "SELECT id, month_year, amount, updated_at FROM budgets ORDER BY month_year DESC";
        List<Budget> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Budget(
                    rs.getInt("id"),
                    rs.getString("month_year"),
                    rs.getDouble("amount"),
                    rs.getString("updated_at")
                ));
            }
        }
        return list;
    }

    @Override
    public boolean deleteBudget(String monthYear) throws SQLException {
        String sql = "DELETE FROM budgets WHERE month_year = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, monthYear.trim());
            return pstmt.executeUpdate() > 0;
        }
    }
}
