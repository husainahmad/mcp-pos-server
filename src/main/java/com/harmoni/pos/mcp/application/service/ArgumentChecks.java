package com.harmoni.pos.mcp.application.service;

import java.util.List;

final class ArgumentChecks {

    private ArgumentChecks() {
    }

    static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    static <T> T required(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    static <T> List<T> requiredList(List<T> value, String field) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be empty");
        }
        return value;
    }

    static int positive(int value, String field) {
        if (value < 1) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    static Integer positive(Integer value, String field) {
        if (value == null || value < 1) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    static long positive(long value, String field) {
        if (value < 1) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }
}
