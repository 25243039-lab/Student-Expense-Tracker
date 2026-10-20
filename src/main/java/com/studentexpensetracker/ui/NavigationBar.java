package com.studentexpensetracker.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Top Navigation Bar (CO5 layout and event handling).
 * Coordinates tab selection with active styling.
 */
public class NavigationBar extends HBox {

    public enum Tab {
        DASHBOARD("Dashboard"),
        EXPENSES("Expenses"),
        BUDGET("Monthly Budget"),
        REPORTS("Reports & Analytics");

        private final String label;

        Tab(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    private final List<Button> navButtons = new ArrayList<>();
    private final Consumer<Tab> onTabSelected;

    public NavigationBar(Consumer<Tab> onTabSelected) {
        this.onTabSelected = onTabSelected;

        initializeUI();
    }

    private void initializeUI() {
        getStyleClass().add("top-nav");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(16);

        // App Branding
        VBox brandingBox = new VBox(2);
        Label titleLabel = new Label("Student Expense Tracker");
        titleLabel.getStyleClass().add("app-title");

        Label subtitleLabel = new Label("College Mini Project • CO5 JavaFX & CO10 JDBC");
        subtitleLabel.getStyleClass().add("app-subtitle");

        brandingBox.getChildren().addAll(titleLabel, subtitleLabel);

        // Spacer to push tabs to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Navigation Tab Buttons
        HBox tabBox = new HBox(8);
        tabBox.setAlignment(Pos.CENTER_RIGHT);

        for (Tab tab : Tab.values()) {
            Button btn = new Button(tab.getLabel());
            btn.getStyleClass().add("nav-button");

            btn.setOnAction(e -> {
                setActiveTab(btn);
                onTabSelected.accept(tab);
            });

            navButtons.add(btn);
            tabBox.getChildren().add(btn);
        }

        // Default active tab: DASHBOARD
        if (!navButtons.isEmpty()) {
            navButtons.get(0).getStyleClass().add("nav-button-active");
        }

        getChildren().addAll(brandingBox, spacer, tabBox);
    }

    private void setActiveTab(Button selectedBtn) {
        for (Button btn : navButtons) {
            btn.getStyleClass().remove("nav-button-active");
        }
        selectedBtn.getStyleClass().add("nav-button-active");
    }
}
