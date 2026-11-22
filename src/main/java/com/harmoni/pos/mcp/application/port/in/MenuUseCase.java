package com.harmoni.pos.mcp.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;

public interface MenuUseCase {

    JsonNode searchProducts(String productName);

    JsonNode getProduct(long productId);

    JsonNode searchCategories(String categoryName);

    JsonNode listPromotions(String status, String search, Integer brandId, int page, int size);
}
