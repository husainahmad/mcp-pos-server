package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.out.CustomerPort;
import com.harmoni.pos.mcp.domain.CallerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomerClientAdapter implements CustomerPort {

    @Qualifier("customerRestClient")
    private final RestClient customerRestClient;

    private final JsonRestClient http;

    @Override
    public JsonNode searchCustomers(CallerContext caller, String search, Integer brandId, int page, int size) {
        Map<String, Object> query = new LinkedHashMap<>();
        if (search != null && !search.isBlank()) {
            query.put("search", search);
        }
        if (brandId != null) {
            query.put("brandId", brandId);
        }
        query.put("page", page);
        query.put("size", size);
        return JsonData.unwrap(http.get(customerRestClient, "/api/v1/customers", caller, query));
    }

    @Override
    public JsonNode getCustomer(CallerContext caller, long customerId) {
        return JsonData.unwrap(http.get(customerRestClient, "/api/v1/customers/" + customerId, caller, Map.of()));
    }

    @Override
    public JsonNode getMetrics(CallerContext caller, long customerId) {
        return JsonData.unwrap(http.get(customerRestClient,
                "/api/v1/growth/customers/" + customerId + "/metrics", caller, Map.of()));
    }

    @Override
    public JsonNode getLoyalty(CallerContext caller, long customerId) {
        return JsonData.unwrap(http.get(customerRestClient,
                "/api/v1/growth/loyalty/" + customerId, caller, Map.of()));
    }
}
