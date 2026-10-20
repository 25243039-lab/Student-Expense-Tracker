package com.studentexpensetracker.dao;

import com.studentexpensetracker.database.DatabaseManager;
import com.studentexpensetracker.model.CategorySummary;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.model.MonthlySummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete JDBC implementation of ExpenseDAO (CO10 requirement).
 * Demonstrates PreparedStatement, try-with-resources, parameterized queries,
 * and robust SQL exception handling.
 */
public class ExpenseDAOImpl implements ExpenseDAO {

    @Override
    public boolean addExpense(Expense expense) throws SQLException {
        String sql = """
            INSERT INTO expenses (description, amount, category, expense_date, payment_method, notes)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, expense.getDescription().trim());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setString(3, expense.getCategory());
            pstmt.setString(4, expense.getDate().toString());
            pstmt.setString(5, expense.getPaymentMethod());
            pstmt.setString(6, expense.getNotes() != null ? expense.getNotes().trim() : "");

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        expense.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean updateExpense(Expense expense) throws SQLException {
        String sql = """
            UPDATE expenses
            SET description = ?, amount = ?, category = ?, expense_date = ?, payment_method = ?, notes = ?
            WHERE id = ?
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expense.getDescription().trim());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setString(3, expense.getCategory());
            pstmt.setString(4, expense.getDate().toString());
            pstmt.setString(5, expense.getPaymentMethod());
            pstmt.setString(6, expense.getNotes() != null ? expense.getNotes().trim() : "");
            pstmt.setInt(7, expense.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean deleteExpense(int id) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public Expense getExpenseById(int id) throws SQLException {
        String sql = "SELECT id, description, amount, category, expense_date, payment_method, notes, created_at FROM expenses WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToExpense(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Expense> getAllExpenses() throws SQLException {
        String sql = "SELECT id, description, amount, category, expense_date, payment_method, notes, created_at FROM expenses ORDER BY expense_date DESC, id DESC";
        List<Expense> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToExpense(rs));
            }
        }
        return list;
    }

    @Override
    public List<Expense> searchAndFilter(String keyword, String category, LocalDate fromDate, LocalDate toDate, String paymentMethod) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, description, amount, category, expense_date, payment_method, notes, created_at FROM expenses WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(description) LIKE ? OR LOWER(notes) LIKE ?)");
            String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (category != null && !category.trim().isEmpty() && !category.equalsIgnoreCase("All Categories") && !category.equalsIgnoreCase("All")) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }

        if (paymentMethod != null && !paymentMethod.trim().isEmpty() && !paymentMethod.equalsIgnoreCase("All Methods") && !paymentMethod.equalsIgnoreCase("All")) {
            sql.append(" AND payment_method = ?");
            params.add(paymentMethod.trim());
        }

        if (fromDate != null) {
            sql.append(" AND expense_date >= ?");
            params.add(fromDate.toString());
        }

        if (toDate != null) {
            sql.append(" AND expense_date <= ?");
            params.add(toDate.toString());
        }

        sql.append(" ORDER BY expense_date DESC, id DESC");

        List<Expense> results = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToExpense(rs));
                }
            }
        }
        return results;
    }

    @Override
    public double getTotalExpenses() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    @Override
    public double getTodayExpenses(LocalDate today) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE expense_date = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today.toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    @Override
    public double getMonthlyExpenses(String monthYear) throws SQLException {
        // monthYear is expected as "YYYY-MM", e.g. "2026-10"
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE substr(expense_date, 1, 7) = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, monthYear);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    @Override
    public int getTotalExpenseCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM expenses";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public String getHighestSpendingCategory() throws SQLException {
        String sql = "SELECT category, SUM(amount) AS cat_total FROM expenses GROUP BY category ORDER BY cat_total DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("category");
            }
        }
        return "None";
    }

    @Override
    public List<Expense> getRecentExpenses(int limit) throws SQLException {
        String sql = "SELECT id, description, amount, category, expense_date, payment_method, notes, created_at FROM expenses ORDER BY expense_date DESC, id DESC LIMIT ?";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExpense(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<CategorySummary> getCategorySummaries() throws SQLException {
        String sql = """
            SELECT category, SUM(amount) AS total, COUNT(*) AS count
            FROM expenses
            GROUP BY category
            ORDER BY total DESC
        """;
        List<CategorySummary> list = new ArrayList<>();
        double grandTotal = getTotalExpenses();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String cat = rs.getString("category");
                double tot = rs.getDouble("total");
                int cnt = rs.getInt("count");
                CategorySummary summary = new CategorySummary(cat, tot, cnt);
                if (grandTotal > 0) {
                    summary.setPercentage((tot / grandTotal) * 100.0);
                }
                list.add(summary);
            }
        }
        return list;
    }

    @Override
    public List<MonthlySummary> getMonthlySummaries() throws SQLException {
        String sql = """
            SELECT substr(expense_date, 1, 7) AS month_year, SUM(amount) AS total, COUNT(*) AS count
            FROM expenses
            GROUP BY month_year
            ORDER BY month_year ASC
        """;
        List<MonthlySummary> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String my = rs.getString("month_year");
                double tot = rs.getDouble("total");
                int cnt = rs.getInt("count");
                list.add(new MonthlySummary(my, tot, cnt));
            }
        }
        return list;
    }

    @Override
    public Expense getMostExpensiveExpense() throws SQLException {
        String sql = "SELECT id, description, amount, category, expense_date, payment_method, notes, created_at FROM expenses ORDER BY amount DESC LIMIT 1";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return mapRowToExpense(rs);
            }
        }
        return null;
    }

    private Expense mapRowToExpense(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String desc = rs.getString("description");
        double amt = rs.getDouble("amount");
        String cat = rs.getString("category");
        String dateStr = rs.getString("expense_date");
        String pay = rs.getString("payment_method");
        String notes = rs.getString("notes");
        String createdAt = rs.getString("created_at");

        LocalDate date = LocalDate.parse(dateStr);
        return new Expense(id, desc, amt, cat, date, pay, notes, createdAt);
    }
}
