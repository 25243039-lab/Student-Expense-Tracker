package com.studentexpensetracker.service;

import com.studentexpensetracker.dao.BudgetDAO;
import com.studentexpensetracker.dao.BudgetDAOImpl;
import com.studentexpensetracker.dao.ExpenseDAO;
import com.studentexpensetracker.dao.ExpenseDAOImpl;
import com.studentexpensetracker.model.Budget;
import com.studentexpensetracker.model.BudgetStatus;
import com.studentexpensetracker.util.DateUtil;
import com.studentexpensetracker.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

/**
 * Service managing college student budget limits, spent vs budget tracking,
 * and overspending alerts.
 */
public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final ExpenseDAO expenseDAO;

    public BudgetService() {
        this(new BudgetDAOImpl(), new ExpenseDAOImpl());
    }

    public BudgetService(BudgetDAO budgetDAO, ExpenseDAO expenseDAO) {
        this.budgetDAO = budgetDAO;
        this.expenseDAO = expenseDAO;
    }

    public void setOrUpdateBudget(String monthYear, double amount) throws IllegalArgumentException, SQLException {
        List<String> errors = ValidationUtil.validateBudget(monthYear, String.valueOf(amount));
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }

        budgetDAO.setOrUpdateBudget(new Budget(monthYear, amount));
    }

    public BudgetStatus getBudgetStatus(String monthYear) throws SQLException {
        if (monthYear == null || monthYear.trim().isEmpty()) {
            monthYear = DateUtil.getCurrentMonthYear();
        }

        Budget budget = budgetDAO.getBudgetByMonthYear(monthYear);
        double budgetAmount = (budget != null) ? budget.getAmount() : 0.0;
        double spentAmount = expenseDAO.getMonthlyExpenses(monthYear);

        return new BudgetStatus(monthYear, budgetAmount, spentAmount);
    }

    public List<Budget> getAllBudgets() throws SQLException {
        return budgetDAO.getAllBudgets();
    }

    public boolean deleteBudget(String monthYear) throws SQLException {
        return budgetDAO.deleteBudget(monthYear);
    }
}
