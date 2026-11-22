package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.CustomerUseCase;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerMcpTools {

    private final CustomerUseCase customers;

    @McpTool(name = "pos_search_customers", description = """
            Search customers by name, phone or email.
            Use the returned customer id for order creation; do not invent customer ids.
            """)
    public String searchCustomers(
            @McpToolParam(description = "Optional name, phone or email search.") String search,
            @McpToolParam(description = "Optional brand id filter.") Integer brandId,
            @McpToolParam(description = "Zero-based page number.") int page,
            @McpToolParam(description = "Page size, 1-100.") int size) {
        return ToolExecutor.call(() -> customers.searchCustomers(search, brandId, page, size));
    }

    @McpTool(name = "pos_get_customer", description = "Get a customer by id.")
    public String getCustomer(@McpToolParam(description = "Customer id.") long customerId) {
        return ToolExecutor.call(() -> customers.getCustomer(customerId));
    }

    @McpTool(name = "pos_get_customer_metrics", description = """
            Get customer spend, visit count, average order value and segment.
            """)
    public String getCustomerMetrics(@McpToolParam(description = "Customer id.") long customerId) {
        return ToolExecutor.call(() -> customers.getMetrics(customerId));
    }

    @McpTool(name = "pos_get_customer_loyalty", description = "Get a customer's loyalty account and points.")
    public String getCustomerLoyalty(@McpToolParam(description = "Customer id.") long customerId) {
        return ToolExecutor.call(() -> customers.getLoyalty(customerId));
    }
}
