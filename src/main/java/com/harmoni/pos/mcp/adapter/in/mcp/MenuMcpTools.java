package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.MenuUseCase;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuMcpTools {

    private final MenuUseCase menu;

    @McpTool(name = "pos_search_products", description = """
            Search products by name. Use the returned productId and skuId for ordering.
            Never invent product ids, SKU ids or prices.
            """)
    public String searchProducts(
            @McpToolParam(description = "Product name as the customer typed it.") String productName) {
        return ToolExecutor.call(() -> menu.searchProducts(productName));
    }

    @McpTool(name = "pos_get_product", description = """
            Get a product with its customizations, effective selection rules and tier prices.
            Use pos_get_product_skus when SKU ids or SKU names are needed.
            """)
    public String getProduct(@McpToolParam(description = "Product id from pos_search_products.") long productId) {
        return ToolExecutor.call(() -> menu.getProduct(productId));
    }

    @McpTool(name = "pos_search_categories", description = "Search menu categories by name.")
    public String searchCategories(
            @McpToolParam(description = "Category name as the customer typed it.") String categoryName) {
        return ToolExecutor.call(() -> menu.searchCategories(categoryName));
    }

    @McpTool(name = "pos_list_promotions", description = """
            List promotions. Optionally filter by status, search text and brand.
            Page is 1-based.
            """)
    public String listPromotions(
            @McpToolParam(description = "Optional promotion status, e.g. ACTIVE.") String status,
            @McpToolParam(description = "Optional code or name search.") String search,
            @McpToolParam(description = "Optional brand id.") Integer brandId,
            @McpToolParam(description = "Page number, 1-based.") int page,
            @McpToolParam(description = "Page size, 1-100.") int size) {
        return ToolExecutor.call(() -> menu.listPromotions(status, search, brandId, page, size));
    }
}
