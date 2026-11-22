package com.harmoni.pos.mcp.application.port.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CallerContext;

public interface MenuPort {

    JsonNode searchProducts(CallerContext caller, String productName);

    JsonNode getProduct(CallerContext caller, long productId);

    JsonNode searchCategories(CallerContext caller, String categoryName);

    JsonNode listPromotions(CallerContext caller, String status, String search, Integer brandId,
                            int page, int size);
}
