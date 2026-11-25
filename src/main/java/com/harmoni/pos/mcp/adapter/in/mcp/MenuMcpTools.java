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

    @McpTool(name = "pos_list_products_by_category", description = """
            List priced products in one category with cursor pagination.
            Use categoryId -1 for all categories. Returns at most 30 results and
            inspects at most 5 customization options per product.
            """)
    public String listProductsByCategory(
            @McpToolParam(description = "Category id from pos_search_categories, or -1 for all.") long categoryId,
            @McpToolParam(description = "Maximum products, capped at 30. Defaults to 20.") Integer limit,
            @McpToolParam(description = "Cursor: return products older than this id.") Integer beforeId) {
        return ToolExecutor.call(() -> menu.listProductsByCategory(categoryId, limit, beforeId));
    }

    @McpTool(name = "pos_list_products_by_brand_tier", description = """
            List products for a brand and tier with optional search and cursor pagination.
            Returns at most 30 results and inspects at most 5 customization options per product.
            """)
    public String listProductsByBrandTier(
            @McpToolParam(description = "Brand id from pos_list_brands.") int brandId,
            @McpToolParam(description = "Tier id from pos_list_tiers or pos_list_tiers_by_type.") int tierId,
            @McpToolParam(description = "Optional product name search.") String search,
            @McpToolParam(description = "Maximum products, capped at 30. Defaults to 20.") Integer limit,
            @McpToolParam(description = "Cursor: return products older than this id.") Integer beforeId) {
        return ToolExecutor.call(() -> menu.listProductsByBrandTier(
                brandId, tierId, search, limit, beforeId));
    }

    @McpTool(name = "pos_get_product_customizations", description = """
            List customization groups linked to one product.
            Result scope: inspect at most 5 entities, including options and prices.
            """)
    public String getProductCustomizations(
            @McpToolParam(description = "Product id from pos_search_products.") long productId) {
        return ToolExecutor.call(() -> menu.getProductCustomizations(productId));
    }

    @McpTool(name = "pos_list_customizations", description = """
            List customization groups. Both page and size are required.
            Returns at most 30 results and inspects at most 5 options per customization.
            """)
    public String listCustomizations(
            @McpToolParam(description = "Page number, 1-based.") int page,
            @McpToolParam(description = "Page size, capped at 30.") int size) {
        return ToolExecutor.call(() -> menu.listCustomizations(page, size));
    }

    @McpTool(name = "pos_find_products_by_customization", description = """
            Find products that use one customization, with a cursor for more pages.
            Returns at most 30 results and inspects at most 5 options per product.
            """)
    public String findProductsByCustomization(
            @McpToolParam(description = "Customization id from pos_list_customizations.") int customizationId,
            @McpToolParam(description = "Maximum products, capped at 30. Defaults to 20.") Integer limit) {
        return ToolExecutor.call(() -> menu.findProductsByCustomization(customizationId, limit));
    }
}
