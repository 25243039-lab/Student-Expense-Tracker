package com.studentexpensetracker.dao;

import com.studentexpensetracker.model.CategorySummary;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.model.MonthlySummary;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Expense operations (CO10 requirement).
 * Declares standard CRUD, search/filtering, and analytical aggregation queries.
 */
public interface ExpenseDAO {

    boolean addExpense(Expense expense) throws SQLException;

    boolean updateExpense(Expense expense) throws SQLException;

    boolean deleteExpense(int id) throws SQLException;

    Expense getExpenseById(int id) throws SQLException;

    List<Expense> getAllExpenses() throws SQLException;

    List<Expense> searchAndFilter(String keyword, String category, LocalDate fromDate, LocalDate toDate, String paymentMethod) throws SQLException;

    double getTotalExpenses() throws SQLException;

    double getTodayExpenses(LocalDate today) throws SQLException;

    double getMonthlyExpenses(String monthYear) throws SQLException;

    int getTotalExpenseCount() throws SQLException;

    String getHighestSpendingCategory() throws SQLException;

    List<Expense> getRecentExpenses(int limit) throws SQLException;

    List<CategorySummary> getCategorySummaries() throws SQLException;

    List<MonthlySummary> getMonthlySummaries() throws SQLException;

    Expense getMostExpensiveExpense() throws SQLException;
}
