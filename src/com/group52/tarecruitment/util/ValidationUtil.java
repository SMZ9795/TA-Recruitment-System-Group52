package com.group52.tarecruitment.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidationUtil() {
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    public static String requireEmail(String value, String fieldName) {
        String normalizedValue = requireText(value, fieldName);
        if (!EMAIL_PATTERN.matcher(normalizedValue).matches()) {
            throw new IllegalArgumentException(fieldName + " must be a valid email address.");
        }
        return normalizedValue;
    }

    public static String requirePassword(String password) {
        String normalizedPassword = requireText(password, "Password");
        if (normalizedPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        return normalizedPassword;
    }

    public static String requireStudentId(String studentId) {
        String normalizedStudentId = requireText(studentId, "Student ID");
        if (!normalizedStudentId.matches("\\d{9,12}")) {
            throw new IllegalArgumentException("Student ID must be 9-12 digits.");
        }
        return normalizedStudentId;
    }

    public static int parsePositiveInt(String value, String fieldName) {
        return parseIntInRange(value, fieldName, 1, Integer.MAX_VALUE);
    }

    public static int parseIntInRange(String value, String fieldName, int minimum, int maximum) {
        String normalizedValue = requireText(value, fieldName);
        final int parsedValue;
        try {
            parsedValue = Integer.parseInt(normalizedValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }

        if (parsedValue < minimum || parsedValue > maximum) {
            if (minimum == maximum) {
                throw new IllegalArgumentException(fieldName + " must be " + minimum + ".");
            }
            if (maximum == Integer.MAX_VALUE) {
                throw new IllegalArgumentException(fieldName + " must be at least " + minimum + ".");
            }
            throw new IllegalArgumentException(
                    fieldName + " must be between " + minimum + " and " + maximum + ".");
        }

        return parsedValue;
    }

    public static String requireTodayOrFutureDate(String value, String fieldName) {
        String normalizedValue = requireText(value, fieldName);
        final LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(normalizedValue);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(fieldName + " must use YYYY-MM-DD format.");
        }

        if (parsedDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(fieldName + " must be today or later.");
        }
        return parsedDate.toString();
    }
}
