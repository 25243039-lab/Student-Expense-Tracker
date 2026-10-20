package com.studentexpensetracker.service;

import com.studentexpensetracker.dao.ExpenseDAO;
import com.studentexpensetracker.dao.ExpenseDAOImpl;
import com.studentexpensetracker.model.CategorySummary;
import com.studentexpensetracker.model.DashboardMetrics;
import com.studentexpensetracker.model.Expense;
import com.studentexpensetracker.model.MonthlySummary;
import com.studentexpensetracker.util.DateUtil;
import com.studentexpensetracker.util.ValidationUtil;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer orchestrating Expense business rules, validation, and DAO operations.
 * Decouples user interface (CO5) from raw database calls (CO10).
 */
public class ExpenseService {

    private final ExpenseDAO expenseDAO;

    public ExpenseService() {
        this(new ExpenseDAOImpl());
    }

    public ExpenseService(ExpenseDAO expenseDAO) {
        this.expenseDAO = expenseDAO;
    }

    public void addExpense(Expense expense) throws IllegalArgumentException, SQLException {
        List<String> errors = ValidationUtil.validateExpense(
            expense.getDescription(),
            String.valueOf(expense.getAmount()),
            expense.getCategory(),
            expense.getDate(),
            expense.getPaymentMethod()
        );

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }

        expenseDAO.addExpense(expense);
    }

    public void updateExpense(Expense expense) throws IllegalArgumentException, SQLException {
        if (expense.getId() == null || expense.getId() <= 0) {
            throw new IllegalArgumentException("Cannot update an expense without a valid ID.");
        }

        List<String> errors = ValidationUtil.validateExpense(
            expense.getDescription(),
            String.valueOf(expense.getAmount()),
            expense.getCategory(),
            expense.getDate(),
            expense.getPaymentMethod()
        );

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }

        expenseDAO.updateExpense(expense);
    }

    public boolean deleteExpense(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid expense ID for deletion.");
        }
        return expenseDAO.deleteExpense(id);
    }

    public List<Expense> getAllExpenses() throws SQLException {
        return expenseDAO.getAllExpenses();
    }

    public Expense getExpenseById(int id) throws SQLException {
        return expenseDAO.getExpenseById(id);
    }

    public List<Expense> searchAndFilter(String keyword, String category, LocalDate fromDate, LocalDate toDate, String paymentMethod) throws SQLException {
        return expenseDAO.searchAndFilter(keyword, category, fromDate, toDate, paymentMethod);
    }

    public DashboardMetrics getDashboardMetrics() throws SQLException {
        double total = expenseDAO.getTotalExpenses();
        double today = expenseDAO.getTodayExpenses(LocalDate.now());
        double month = expenseDAO.getMonthlyExpenses(DateUtil.getCurrentMonthYear());
        int count = expenseDAO.getTotalExpenseCount();
        String topCat = expenseDAO.getHighestSpendingCategory();
        List<Expense> recent = expenseDAO.getRecentExpenses(5);
        List<CategorySummary> catList = expenseDAO.getCategorySummaries();

        return new DashboardMetrics(total, today, month, count, topCat, recent, catList);
    }

    public List<CategorySummary> getCategoryBreakdown() throws SQLException {
        return expenseDAO.getCategorySummaries();
    }

    public List<MonthlySummary> getMonthlyBreakdown() throws SQLException {
        return expenseDAO.getMonthlySummaries();
    }

    public Expense getMostExpensiveTransaction() throws SQLException {
        return expenseDAO.getMostExpensiveExpense();
    }
}
