package com.studentexpensetracker;

import com.studentexpensetracker.database.DatabaseManager;
import com.studentexpensetracker.service.BudgetService;
import com.studentexpensetracker.service.ExpenseService;
import com.studentexpensetracker.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main JavaFX Application class (CO5 Entrypoint).
 * Initializes SQLite database connection on startup and constructs the primary UI scene.
 */
public class App extends Application {

    @Override
    public void init() throws Exception {
        super.init();
        // Initialize SQLite database and create tables if not present (CO10)
        DatabaseManager.initializeDatabase();
    }

    @Override
    public void start(Stage primaryStage) {
        ExpenseService expenseService = new ExpenseService();
        BudgetService budgetService = new BudgetService();

        MainView mainView = new MainView(expenseService, budgetService);

        Scene scene = new Scene(mainView, 1140, 740);

        // Apply custom modern JavaFX stylesheet
        try {
            String cssPath = getClass().getResource("/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("[App] Warning: Failed to load stylesheet: " + e.getMessage());
        }

        primaryStage.setTitle("Student Expense Tracker");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(680);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
