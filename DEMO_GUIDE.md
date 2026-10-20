# 🎓 College Project Demonstration & Viva Guide
## Project Title: Student Expense Tracker
**Course Outcomes**: CO5 (JavaFX Desktop Application & Event Handling) and CO10 (JDBC Database Connectivity)

---

## 1. Quick Project Introduction & Purpose

### What is this project?
The **Student Expense Tracker** is a modular desktop application engineered using **Java 17**, **JavaFX**, and **SQLite JDBC**. It enables college students to record, categorize, search, filter, and analyze daily spending habits while maintaining strict monthly budget limits.

### Why was it built?
College students often face financial budgeting challenges with multiple disparate expenses (canteen, textbooks, transport, mobile recharge, entertainment). This application provides a single local dashboard to visualize spending patterns, prevent overspending through automatic warning thresholds, and maintain financial discipline.

---

## 2. How to Run the Application Before the Faculty

Open PowerShell or Command Prompt in the project directory:

```powershell
# Option A: Run via Maven JavaFX Plugin (Recommended)
mvn javafx:run

# Option B: Run Standalone Packaged Fat JAR
java -jar target/student-expense-tracker-1.0.0.jar
```

---

## 3. Recommended Live Demonstration Sequence

Follow this step-by-step presentation order when presenting to faculty:

### Step 1: Show the Dashboard (First Impression)
1. Point to the top metric cards:
   - **Total Spent**: Grand total across all recorded expenses.
   - **This Month**: Spending in the current calendar month.
   - **Today**: Real-time spending recorded for today.
   - **Records & Top Category**: Total transaction count and highest spending category (e.g. "Food" or "Education").
2. Highlight the **Category-wise Spending Breakdown PieChart**: Show that it calculates real-time percentage shares directly from the database.
3. Show the **Recent Transactions** table displaying the latest student expenditures.

### Step 2: Add a New Expense (Validation & Insert)
1. Click the **Expenses** tab in the top navigation.
2. Demonstrate **Input Validation** first:
   - Click **Save Expense** with empty fields. Point out the validation dialog warning: *"Expense description cannot be empty."*
   - Enter amount as `-150` or `abc`. Point out the error dialog: *"Amount must be greater than zero."*
   - Try selecting a future date. Point out: *"Expense date cannot be in the future."*
3. Now enter valid data:
   - **Description**: `Discrete Mathematics Reference Guide`
   - **Amount**: `350`
   - **Category**: `Education`
   - **Date**: Today's date
   - **Payment Method**: `UPI`
   - **Notes**: `Exam preparation study material`
4. Click **Save Expense**.
5. Show the success dialog, and show that the new item immediately appears at the top of the `TableView`.
6. Switch to the **Dashboard** tab to prove that the **Total Spent**, **Today's Expenses**, and **PieChart** automatically updated!

### Step 3: Search and Filter Expenses
1. Return to the **Expenses** tab.
2. In the **Search description or notes** field, type `Mathematics`. Notice the table immediately filters to show only matching records.
3. Select **Category**: `Education`. Show that only educational expenses remain.
4. Select **Payment Method**: `UPI`.
5. Click **Reset Filters**. Show that all filters clear and the full dataset returns.

### Step 4: Update an Existing Expense
1. Click on the row you created (`Discrete Mathematics Reference Guide`).
2. Show that its details instantly populate into the right-hand form:
   - The form title changes to **Edit Expense #[ID]**.
   - The button text changes to **Update Expense**.
3. Change the amount from `350` to `400` and append to the notes: `Includes practice question bank`.
4. Click **Update Expense**.
5. Point out the success dialog, and show the updated amount reflected in the table.

### Step 5: Delete an Expense with Confirmation
1. Select the expense row you just updated.
2. Click the red **Delete Selected** button.
3. Show the **Confirmation Alert dialog**: *"Are you sure you want to delete...? This action cannot be undone."*
4. Click **Cancel** first — show that the record is preserved.
5. Click **Delete Selected** again and click **OK**.
6. Show that the record disappears from the table, and total counts decrease.

### Step 6: Demonstrate Monthly Budget Management
1. Click the **Monthly Budget** tab.
2. Select current month (e.g., `2026-10`).
3. Set a budget limit of `5000` and click **Set / Update Budget**.
4. Point out the three dynamic visual states:
   - **Safe State (< 80%)**: Green progress bar, showing remaining balance available.
   - **Warning State (80% - 100%)**: Orange warning banner alerting that budget capacity is nearly exhausted.
   - **Exceeded State (> 100%)**: Set budget to `2000` (less than total spent). Show the **Red Danger Banner**:
     - Remaining balance displays **₹ 0.00 (Exceeded)**.
     - Explain to faculty: *"The application strictly follows financial accounting standards — it never displays a negative balance as if it were money available to spend. Instead, it highlights the exact Overspent Deficit."*

### Step 7: Reports & Analytics
1. Click the **Reports & Analytics** tab.
2. Show the **Highest Single Expense Transaction** card.
3. Show the **Category Summary Table** with transaction counts and percentage share.
4. Show the **Monthly Expense Trends BarChart** comparing total spending across months.

---

## 4. Explaining JavaFX Event Handling (CO5)

When faculty ask: *"How did you implement event handling in JavaFX?"*, explain:

1. **Button Action Events (`ActionEvent`)**:
   ```java
   // In ExpenseManagementView.java
   btnSave.setOnAction(e -> handleSaveExpense());
   btnDelete.setOnAction(e -> handleDeleteExpense());
   btnClear.setOnAction(e -> clearForm());
   ```
2. **Table Selection Model Listener (`ChangeListener`)**:
   ```java
   // Triggers when a user clicks any row in the TableView
   expenseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
       if (newVal != null) {
           populateFormForEdit(newVal);
           btnDelete.setDisable(false);
       }
   });
   ```
3. **Reactive Text & Filter Listeners**:
   ```java
   // Real-time filtering as the user types
   txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
   cmbFilterCategory.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
   ```
4. **Dialogs & User Response Handling (`Optional<ButtonType>`)**:
   ```java
   // Modal confirmation dialog
   Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
   Optional<ButtonType> result = alert.showAndWait();
   if (result.isPresent() && result.get() == ButtonType.OK) {
       // execute deletion
   }
   ```

---

## 5. Explaining JDBC & Database Operations (CO10)

When faculty ask: *"How did you implement JDBC connectivity?"*, explain:

1. **Connection Establishment (`DriverManager`)**:
   ```java
   // In DatabaseManager.java
   public static Connection getConnection() throws SQLException {
       return DriverManager.getConnection(DatabaseConfig.getDbUrl());
   }
   ```
2. **Automatic Table DDL on Startup**:
   `DatabaseManager.initializeDatabase()` executes `CREATE TABLE IF NOT EXISTS` for both `expenses` and `budgets` with check constraints and index creation.
3. **Preventing SQL Injection via `PreparedStatement`**:
   User input is **never concatenated** into SQL strings. It always uses parameterized placeholders (`?`):
   ```java
   String sql = "INSERT INTO expenses (description, amount, category, expense_date, payment_method, notes) VALUES (?, ?, ?, ?, ?, ?)";
   try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
       pstmt.setString(1, expense.getDescription().trim());
       pstmt.setDouble(2, expense.getAmount());
       // ...
       pstmt.executeUpdate();
   }
   ```
4. **Transactions with Commit & Rollback**:
   Demonstrated in `seedInitialDataIfEmpty`:
   ```java
   conn.setAutoCommit(false);
   // execute multiple batch inserts...
   conn.commit();
   // in catch block:
   conn.rollback();
   ```
5. **Safe Resource Cleanup (`try-with-resources`)**:
   Every `Connection`, `PreparedStatement`, `Statement`, and `ResultSet` is automatically closed at the end of the block, avoiding memory or socket leaks.
6. **SQLite Upsert Syntax**:
   In `BudgetDAOImpl.java`, updating an existing month's budget uses SQLite's native `ON CONFLICT(month_year) DO UPDATE SET amount=excluded.amount`.

---

## 6. Common Viva Questions & Model Answers

### Q1: What design pattern did you use to structure your project?
> **Answer**: *"I implemented the **MVC (Model-View-Controller) / Multi-Tier Architecture** alongside the **DAO (Data Access Object)** pattern. The `model` package holds data transfer objects, the `dao` package encapsulates raw JDBC database operations, the `service` layer manages business rules and validation, and the `ui` package handles JavaFX presentation. This decouples database code from UI components."*

### Q2: Why did you use `PreparedStatement` instead of `Statement`?
> **Answer**: *"Two main reasons: First, **Security**: `PreparedStatement` uses parameterized placeholders (`?`) which prevents SQL injection attacks by escaping all user input. Second, **Performance**: The SQL statement is pre-compiled by the database engine, resulting in faster execution on repeated queries."*

### Q3: Where is the SQLite database stored? Will data be lost when restarting?
> **Answer**: *"No, data is permanently persisted. The database is stored in the user's home directory under `.student_expense_tracker/student_expenses.db`. Because SQLite writes directly to a persistent disk file rather than in-memory storage, all additions, modifications, and deletions remain intact across restarts."*

### Q4: How did you implement real-time search without lag?
> **Answer**: *"In `ExpenseManagementView`, I added a change listener to the `textProperty()` of the search field and `valueProperty()` of the category and payment filter ComboBoxes. Each change invokes `applyFilters()`, which issues a parameterized SQL query with `LIKE` and exact matches to update the observable list bound to the `TableView`."*

### Q5: How did you handle budget deficits when spending exceeds the budget?
> **Answer**: *"In `BudgetStatus.java`, when spending exceeds the budget, the remaining balance is mathematically capped to `0.00` rather than allowing a negative number. An explicit `overspentAmount` field tracks the deficit, which triggers a red danger banner and high-priority alert in the UI."*

### Q6: How do you verify the project without launching the graphical UI?
> **Answer**: *"The project has 24 automated unit and integration tests covering data validation, JDBC CRUD, SQL filters, and budget business logic. Running `mvn test` executes all test suites against a temporary, isolated SQLite database."*

---

## 7. Known Limitations & Academic Scope
- **Single Currency**: Formatted by default in Indian Rupees (`₹`), but easily configurable to other currencies in `CurrencyUtil`.
- **Single User Desktop Application**: Designed as a standalone student desktop utility rather than a multi-tenant client-server system.
