package com.harmoni.pos.mcp.application.port.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CallerContext;

public interface CustomerPort {

    JsonNode searchCustomers(CallerContext caller, String search, Integer brandId, int page, int size);

    JsonNode getCustomer(CallerContext caller, long customerId);

    JsonNode getMetrics(CallerContext caller, long customerId);

    JsonNode getLoyalty(CallerContext caller, long customerId);
}
