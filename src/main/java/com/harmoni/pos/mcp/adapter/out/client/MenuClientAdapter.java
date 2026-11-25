package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.out.MenuPort;
import com.harmoni.pos.mcp.domain.CallerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MenuClientAdapter implements MenuPort {

    @Qualifier("menuRestClient")
    private final RestClient menuRestClient;

    private final JsonRestClient http;

    @Override
    public JsonNode searchProducts(CallerContext caller, String productName) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/product/search", caller,
                Map.of("productName", productName)));
    }

    @Override
    public JsonNode getProduct(CallerContext caller, long productId) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/product/" + productId, caller, Map.of()));
    }

    @Override
    public JsonNode searchCategories(CallerContext caller, String categoryName) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/category/search", caller,
                Map.of("categoryName", categoryName)));
    }

    @Override
    public JsonNode listPromotions(CallerContext caller, String status, String search, Integer brandId,
                                   int page, int size) {
        Map<String, Object> query = new LinkedHashMap<>();
        putIfPresent(query, "status", status);
        putIfPresent(query, "search", search);
        if (brandId != null) {
            query.put("brandId", brandId);
        }
        query.put("page", page);
        query.put("size", size);
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/promotion", caller, query));
    }

    @Override
    public JsonNode listProductsByCategory(CallerContext caller, long categoryId, int limit, Integer beforeId) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("limit", limit);
        if (beforeId != null) {
            query.put("beforeId", beforeId);
        }
        return JsonData.unwrap(http.get(menuRestClient,
                "/api/v1/product/category/" + categoryId + "/price/page", caller, query));
    }

    @Override
    public JsonNode listProductsByBrandTier(CallerContext caller, int brandId, int tierId, String search,
                                            int limit, Integer beforeId) {
        Map<String, Object> query = new LinkedHashMap<>();
        if (search != null) {
            query.put("search", search);
        }
        query.put("limit", limit);
        if (beforeId != null) {
            query.put("beforeId", beforeId);
        }
        return JsonData.unwrap(http.get(menuRestClient,
                "/api/v1/product/brand/" + brandId + "/tier/" + tierId + "/cursor", caller, query));
    }

    @Override
    public JsonNode getProductCustomizations(CallerContext caller, long productId) {
        return JsonData.unwrap(http.get(menuRestClient,
                "/api/v1/product/" + productId + "/customization", caller, Map.of()));
    }

    @Override
    public JsonNode listCustomizations(CallerContext caller, int page, int size) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("page", page);
        query.put("size", size);
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/customization", caller, query));
    }

    @Override
    public JsonNode findProductsByCustomization(CallerContext caller, int customizationId, int limit) {
        return JsonData.unwrap(http.get(menuRestClient,
                "/api/v1/product/by-customization/" + customizationId, caller, Map.of("limit", limit)));
    }

    private void putIfPresent(Map<String, Object> query, String key, String value) {
        if (value != null && !value.isBlank()) {
            query.put(key, value);
        }
    }
}
