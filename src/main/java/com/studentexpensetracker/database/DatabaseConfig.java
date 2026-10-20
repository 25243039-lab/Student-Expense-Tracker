package com.studentexpensetracker.database;

import java.io.File;

/**
 * Manages database configuration and file location.
 * Stores SQLite database in a persistent, OS-standard user data directory.
 */
public class DatabaseConfig {

    private static final String APP_DIR_NAME = ".student_expense_tracker";
    private static final String DB_FILE_NAME = "student_expenses.db";

    private static String customDbUrl = null;

    /**
     * Resolves the persistent SQLite database file path.
     * Guaranteed not to be wiped across runs or relative working directory shifts.
     */
    public static String getDatabasePath() {
        String userHome = System.getProperty("user.home");
        File appDir = new File(userHome, APP_DIR_NAME);
        if (!appDir.exists()) {
            boolean created = appDir.mkdirs();
            if (created) {
                System.out.println("[DatabaseConfig] Created application data directory: " + appDir.getAbsolutePath());
            }
        }
        return new File(appDir, DB_FILE_NAME).getAbsolutePath();
    }

    /**
     * Returns JDBC connection URL.
     */
    public static String getDbUrl() {
        if (customDbUrl != null && !customDbUrl.trim().isEmpty()) {
            return customDbUrl;
        }
        return "jdbc:sqlite:" + getDatabasePath();
    }

    /**
     * Allows test suites to supply an in-memory or temporary database URL.
     * Example: "jdbc:sqlite::memory:"
     */
    public static void setCustomDbUrl(String url) {
        customDbUrl = url;
    }

    /**
     * Resets database URL back to default persistent storage.
     */
    public static void resetCustomDbUrl() {
        customDbUrl = null;
    }
}
