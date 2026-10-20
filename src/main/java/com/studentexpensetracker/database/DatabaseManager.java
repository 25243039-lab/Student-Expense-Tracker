package com.studentexpensetracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Core JDBC Database Manager (CO10 requirement).
 * Manages database connections, executes table DDL upon startup,
 * and handles transactions and initial sample seeding.
 */
public class DatabaseManager {

    static {
        try {
            // Explicitly load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseManager] SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Establishes and returns a new JDBC Connection.
     * Uses try-with-resources at call sites for robust resource management.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DatabaseConfig.getDbUrl());
    }

    /**
     * Automatically creates necessary tables and indexes if they do not exist.
     * Called on application startup.
     */
    public static void initializeDatabase() {
        String createExpensesTableSQL = """
            CREATE TABLE IF NOT EXISTS expenses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                description TEXT NOT NULL,
                amount REAL NOT NULL CHECK(amount > 0),
                category TEXT NOT NULL,
                expense_date TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createBudgetsTableSQL = """
            CREATE TABLE IF NOT EXISTS budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                month_year TEXT NOT NULL UNIQUE,
                amount REAL NOT NULL CHECK(amount >= 0),
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        String createDateIndexSQL = "CREATE INDEX IF NOT EXISTS idx_expense_date ON expenses(expense_date);";
        String createCategoryIndexSQL = "CREATE INDEX IF NOT EXISTS idx_expense_category ON expenses(category);";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createExpensesTableSQL);
            stmt.execute(createBudgetsTableSQL);
            stmt.execute(createDateIndexSQL);
            stmt.execute(createCategoryIndexSQL);

            System.out.println("[DatabaseManager] Database initialized successfully at: " + DatabaseConfig.getDbUrl());

            // If empty, seed initial sample data for demonstration
            seedInitialDataIfEmpty(conn);

        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Seeds initial sample college student expenses if the database has zero records.
     * Ensures college demonstration is immediately ready with realistic data.
     */
    private static void seedInitialDataIfEmpty(Connection conn) {
        String checkCountSQL = "SELECT COUNT(*) FROM expenses";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkCountSQL)) {

            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("[DatabaseManager] Seeding initial student expenses for demonstration...");

                LocalDate today = LocalDate.now();
                String currentMonth = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));

                // Insert a default budget for the current month (e.g. 15,000 INR / currency units)
                String insertBudgetSQL = "INSERT OR IGNORE INTO budgets (month_year, amount) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertBudgetSQL)) {
                    pstmt.setString(1, currentMonth);
                    pstmt.setDouble(2, 12000.0);
                    pstmt.executeUpdate();
                }

                // Sample records across varied categories, dates, and payment methods
                String insertExpenseSQL = """
                    INSERT INTO expenses (description, amount, category, expense_date, payment_method, notes)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

                try (PreparedStatement pstmt = conn.prepareStatement(insertExpenseSQL)) {
                    // Turn auto-commit off for batch transaction (CO10 demonstration)
                    conn.setAutoCommit(false);

                    Object[][] sampleData = {
                        {"Campus Canteen Lunch", 180.0, "Food", today.toString(), "UPI", "Meal with project teammates"},
                        {"Bus Monthly Pass", 450.0, "Transport", today.minusDays(1).toString(), "Cash", "Route 23 campus bus"},
                        {"Database Systems Textbook", 1250.0, "Education", today.minusDays(3).toString(), "Debit Card", "Reference book for semester exams"},
                        {"Notebooks & Stationery", 220.0, "Education", today.minusDays(4).toString(), "UPI", "Lab record notebooks and pens"},
                        {"Weekend Movie with Friends", 350.0, "Entertainment", today.minusDays(6).toString(), "UPI", "Inox cinema student discount"},
                        {"College Sports T-Shirt", 599.0, "Shopping", today.minusDays(8).toString(), "Credit Card", "Annual sports day jersey"},
                        {"Pharmacy - Cold Medicine", 140.0, "Health", today.minusDays(10).toString(), "Cash", "Paracetamol & vitamin C tablets"},
                        {"Hostel Snacks & Groceries", 650.0, "Food", today.minusDays(12).toString(), "UPI", "Biscuits, milk, and tea bags"},
                        {"Mobile Recharge & 5G Data", 299.0, "Others", today.minusDays(14).toString(), "UPI", "Monthly academic research connectivity"},
                        {"Cafeteria Evening Coffee", 60.0, "Food", today.toString(), "UPI", "Espresso during group study"}
                    };

                    for (Object[] row : sampleData) {
                        pstmt.setString(1, (String) row[0]);
                        pstmt.setDouble(2, (Double) row[1]);
                        pstmt.setString(3, (String) row[2]);
                        pstmt.setString(4, (String) row[3]);
                        pstmt.setString(5, (String) row[4]);
                        pstmt.setString(6, (String) row[5]);
                        pstmt.executeUpdate();
                    }

                    conn.commit();
                    conn.setAutoCommit(true);
                    System.out.println("[DatabaseManager] Sample seed data inserted successfully.");
                } catch (SQLException e) {
                    conn.rollback();
                    conn.setAutoCommit(true);
                    System.err.println("[DatabaseManager] Rolling back sample seed transaction: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error checking expense count: " + e.getMessage());
        }
    }

    /**
     * Standalone verification runner to initialize and check the database without starting JavaFX UI.
     */
    public static void main(String[] args) {
        System.out.println("[DatabaseManager] Running standalone database verification...");
        initializeDatabase();
        System.out.println("[DatabaseManager] Verification complete. Database ready.");
    }
}
