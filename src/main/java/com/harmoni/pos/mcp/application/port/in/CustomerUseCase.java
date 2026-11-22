package com.harmoni.pos.mcp.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;

public interface CustomerUseCase {

    JsonNode searchCustomers(String search, Integer brandId, int page, int size);

    JsonNode getCustomer(long customerId);

    JsonNode getMetrics(long customerId);

    JsonNode getLoyalty(long customerId);
}
