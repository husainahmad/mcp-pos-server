package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.in.CustomerUseCase;
import com.harmoni.pos.mcp.application.port.out.CustomerPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerApplicationService implements CustomerUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final CustomerPort customerPort;

    @Override
    public JsonNode searchCustomers(String search, Integer brandId, int page, int size) {
        CallerContext caller = CallerContextHolder.current();
        return customerPort.searchCustomers(caller, blankToNull(search), brandId,
                Math.max(page, 0), boundedSize(size));
    }

    @Override
    public JsonNode getCustomer(long customerId) {
        return customerPort.getCustomer(CallerContextHolder.current(), positive(customerId));
    }

    @Override
    public JsonNode getMetrics(long customerId) {
        return customerPort.getMetrics(CallerContextHolder.current(), positive(customerId));
    }

    @Override
    public JsonNode getLoyalty(long customerId) {
        return customerPort.getLoyalty(CallerContextHolder.current(), positive(customerId));
    }

    private int boundedSize(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private long positive(long value) {
        if (value < 1) {
            throw new IllegalArgumentException("customerId must be positive");
        }
        return value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
