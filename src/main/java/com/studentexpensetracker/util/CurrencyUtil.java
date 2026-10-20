package com.studentexpensetracker.util;

import java.text.DecimalFormat;

/**
 * Utility for uniform monetary formatting across the UI.
 */
public class CurrencyUtil {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    private static final String SYMBOL = "₹";

    public static String format(double amount) {
        return SYMBOL + " " + CURRENCY_FORMAT.format(amount);
    }

    public static String formatPlain(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String getSymbol() {
        return SYMBOL;
    }
}
