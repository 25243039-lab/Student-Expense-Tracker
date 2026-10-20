package com.studentexpensetracker;

/**
 * Standard JVM entrypoint to prevent module-path runtime restrictions
 * when executing fat JARs or non-modular JavaFX configurations.
 */
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
