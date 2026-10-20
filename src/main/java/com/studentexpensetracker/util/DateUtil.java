package com.studentexpensetracker.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility for date parsing and consistent formatting across reports and tables.
 */
public class DateUtil {

    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    public static final DateTimeFormatter MONTH_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    public static String formatDisplay(LocalDate date) {
        if (date == null) return "";
        return date.format(DISPLAY_FORMATTER);
    }

    public static String getCurrentMonthYear() {
        return LocalDate.now().format(MONTH_YEAR_FORMATTER);
    }

    public static String formatMonthDisplay(String monthYear) {
        if (monthYear == null || monthYear.trim().isEmpty()) return "";
        try {
            LocalDate date = LocalDate.parse(monthYear + "-01");
            return date.format(MONTH_DISPLAY_FORMATTER);
        } catch (Exception e) {
            return monthYear;
        }
    }
}
