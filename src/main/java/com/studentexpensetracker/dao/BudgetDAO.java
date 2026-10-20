package com.studentexpensetracker.dao;

import com.studentexpensetracker.model.Budget;

import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object (DAO) interface for Budget operations (CO10 requirement).
 */
public interface BudgetDAO {

    boolean setOrUpdateBudget(Budget budget) throws SQLException;

    Budget getBudgetByMonthYear(String monthYear) throws SQLException;

    List<Budget> getAllBudgets() throws SQLException;

    boolean deleteBudget(String monthYear) throws SQLException;
}
