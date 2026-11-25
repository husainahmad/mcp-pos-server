package com.harmoni.pos.mcp.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;

public interface MenuUseCase {

    JsonNode searchProducts(String productName);

    JsonNode getProduct(long productId);

    JsonNode searchCategories(String categoryName);

    JsonNode listPromotions(String status, String search, Integer brandId, int page, int size);

    JsonNode listProductsByCategory(long categoryId, Integer limit, Integer beforeId);

    JsonNode listProductsByBrandTier(int brandId, int tierId, String search,
                                     Integer limit, Integer beforeId);

    JsonNode getProductCustomizations(long productId);

    JsonNode listCustomizations(int page, int size);

    JsonNode findProductsByCustomization(int customizationId, Integer limit);
}
