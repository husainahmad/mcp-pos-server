package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.out.OrganizationPort;
import com.harmoni.pos.mcp.domain.CallerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrganizationClientAdapter implements OrganizationPort {

    @Qualifier("menuRestClient")
    private final RestClient menuRestClient;

    private final JsonRestClient http;

    @Override
    public JsonNode organizationTree(CallerContext caller) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/organization/tree", caller, Map.of()));
    }

    @Override
    public JsonNode listStores(CallerContext caller, int chainId, int page, int size, String search) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("chainId", chainId);
        query.put("page", page);
        query.put("size", size);
        query.put("search", search);
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/store", caller, query));
    }

    @Override
    public JsonNode listTables(CallerContext caller, int storeId) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/table/store/" + storeId, caller, Map.of()));
    }

    @Override
    public JsonNode listTiersByType(CallerContext caller, int brandId, String tierType) {
        return JsonData.unwrap(http.get(menuRestClient,
                "/api/v1/tier/brand/" + brandId + "/type/" + tierType, caller, Map.of()));
    }
}
