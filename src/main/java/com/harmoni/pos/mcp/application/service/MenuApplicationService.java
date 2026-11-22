package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.in.MenuUseCase;
import com.harmoni.pos.mcp.application.port.out.MenuPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuApplicationService implements MenuUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final MenuPort menuPort;

    @Override
    public JsonNode searchProducts(String productName) {
        CallerContext caller = CallerContextHolder.current();
        return menuPort.searchProducts(caller, required(productName, "productName"));
    }

    @Override
    public JsonNode getProduct(long productId) {
        CallerContext caller = CallerContextHolder.current();
        return menuPort.getProduct(caller, positive(productId, "productId"));
    }

    @Override
    public JsonNode searchCategories(String categoryName) {
        CallerContext caller = CallerContextHolder.current();
        return menuPort.searchCategories(caller, required(categoryName, "categoryName"));
    }

    @Override
    public JsonNode listPromotions(String status, String search, Integer brandId, int page, int size) {
        CallerContext caller = CallerContextHolder.current();
        return menuPort.listPromotions(caller, blankToNull(status), blankToNull(search), brandId,
                Math.max(page, 1), boundedSize(size));
    }

    private int boundedSize(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private long positive(long value, String field) {
        if (value < 1) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
