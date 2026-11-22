package com.harmoni.pos.mcp.domain;

import java.util.Locale;

public enum ReportType {
    SETTLEMENT("settlement"),
    TOP_PRODUCTS("top-products"),
    DAILY("daily"),
    SALES("sales"),
    ORDER_VOLUME("order-volume");

    private final String path;

    ReportType(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public static ReportType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("reportType is required");
        }
        return ReportType.valueOf(value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    }
}
