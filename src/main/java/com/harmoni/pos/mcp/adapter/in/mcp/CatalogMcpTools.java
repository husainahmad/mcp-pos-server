package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.CatalogUseCase;
import com.harmoni.pos.mcp.domain.CatalogCommands;
import com.harmoni.pos.mcp.domain.CustomizationPriceCommand;
import com.harmoni.pos.mcp.domain.ValidateOrderItemsCommand;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CatalogMcpTools {

    private final CatalogUseCase catalog;

    @McpTool(name = "pos_get_current_user", description = """
            Get the signed-in POS user, including store id. Use this to resolve the caller's store.
            """)
    public String getCurrentUser() {
        return ToolExecutor.call(catalog::getCurrentUser);
    }

    @McpTool(name = "pos_list_brands", description = """
            List brands visible to the caller. Use these brand ids instead of inventing one.
            """)
    public String listBrands() {
        return ToolExecutor.call(catalog::listBrands);
    }

    @McpTool(name = "pos_list_tiers", description = """
            List price tiers for a brand. Use a real tier id when creating SKU or option prices.
            """)
    public String listTiers(
            @McpToolParam(description = "Brand id from pos_list_brands or pos_get_current_user.") Integer brandId) {
        return ToolExecutor.call(() -> catalog.listTiers(brandId));
    }

    @McpTool(name = "pos_get_product_skus", description = """
            List a product's SKUs. Use the returned SKU ids for ordering or price reads.
            """)
    public String getProductSkus(
            @McpToolParam(description = "Product id from pos_search_products.") long productId) {
        return ToolExecutor.call(() -> catalog.getProductSkus(productId));
    }

    @McpTool(name = "pos_get_sku_tier_prices", description = """
            Read existing SKU prices for one price tier. Use this before updating SKU prices.
            """)
    public String getSkuTierPrices(
            @McpToolParam(description = "SKU ids from pos_get_product_skus.") List<Integer> skuIds,
            @McpToolParam(description = "Tier id from pos_list_tiers.") Integer tierId) {
        return ToolExecutor.call(() -> catalog.getSkuTierPrices(skuIds, tierId));
    }

    @McpTool(name = "pos_create_category", description = """
            Create a category. Search existing categories first to avoid duplicates.
            brandId and description are required.
            """)
    public String createCategory(
            @McpToolParam(description = "Category name.") String name,
            @McpToolParam(description = "Category description.") String description,
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Optional active flag.") Boolean active) {
        return ToolExecutor.call(() -> catalog.createCategory(
                new CatalogCommands.CategoryCommand(name, description, brandId, active)));
    }

    @McpTool(name = "pos_update_category", description = """
            Update an existing category. Use ids returned by pos_search_categories.
            """)
    public String updateCategory(
            @McpToolParam(description = "Category id.") Integer categoryId,
            @McpToolParam(description = "Category name.") String name,
            @McpToolParam(description = "Category description.") String description,
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Optional active flag.") Boolean active) {
        return ToolExecutor.call(() -> catalog.updateCategory(categoryId,
                new CatalogCommands.CategoryCommand(name, description, brandId, active)));
    }

    @McpTool(name = "pos_create_product", description = """
            Create a product with its SKUs, tier prices and optional customizations in one call.
            Search the product and category first. Use real category and tier ids.
            Every SKU needs at least one tier price.
            """)
    public String createProduct(
            @McpToolParam(description = "Product name.") String name,
            @McpToolParam(description = "Category id from pos_search_categories.") Integer categoryId,
            @McpToolParam(description = "Optional product description.") String description,
            @McpToolParam(description = "Optional product image id.") Integer productImageId,
            @McpToolParam(description = "SKUs. Each SKU has name, optional productId, and tierPrices.") List<CatalogCommands.SkuCommand> skus,
            @McpToolParam(description = "Optional customization ids to assign to the product.") List<Integer> customizationIds) {
        return ToolExecutor.call(() -> catalog.createProduct(new CatalogCommands.ProductCommand(
                null, name, categoryId, description, image(productImageId), skus, customizationIds)));
    }

    @McpTool(name = "pos_update_product", description = """
            Update a product and its SKUs. Read the product first and send the complete SKU list.
            Every SKU must have an id and at least one tier price.
            """)
    public String updateProduct(
            @McpToolParam(description = "Product id.") Integer productId,
            @McpToolParam(description = "Product name.") String name,
            @McpToolParam(description = "Category id from pos_search_categories.") Integer categoryId,
            @McpToolParam(description = "Optional product description.") String description,
            @McpToolParam(description = "Optional product image id.") Integer productImageId,
            @McpToolParam(description = "Complete SKU list. Each SKU has id, name, and tierPrices.") List<CatalogCommands.SkuCommand> skus,
            @McpToolParam(description = "Optional customization ids. Omit to keep existing links.") List<Integer> customizationIds) {
        return ToolExecutor.call(() -> catalog.updateProduct(new CatalogCommands.ProductCommand(
                productId, name, categoryId, description, image(productImageId), skus, customizationIds)));
    }

    @McpTool(name = "pos_create_customization", description = """
            Create a customization group with its options and tier prices.
            Use real tier ids from pos_list_tiers.
            """)
    public String createCustomization(
            @McpToolParam(description = "Customization name.") String name,
            @McpToolParam(description = "Optional description.") String description,
            @McpToolParam(description = "Minimum selections.") Integer minimumSelection,
            @McpToolParam(description = "Maximum selections.") Integer maximumSelection,
            @McpToolParam(description = "Optional active flag.") Boolean active,
            @McpToolParam(description = "Whether a selection is required.") Boolean required,
            @McpToolParam(description = "Whether an option may be selected with quantity greater than one.") Boolean allowQuantity,
            @McpToolParam(description = "Selection type: SINGLE or MULTIPLE.") String selectionType,
            @McpToolParam(description = "Options. Each option has name and optional tierPrices.") List<CatalogCommands.CustomizationOptionCommand> customizationOptions) {
        return ToolExecutor.call(() -> catalog.createCustomization(customization(
                null, name, description, minimumSelection, maximumSelection, active, required,
                allowQuantity, selectionType, customizationOptions)));
    }

    @McpTool(name = "pos_update_customization", description = """
            Update a customization group, its options and tier prices.
            Read the current customization first and send the complete option list.
            """)
    public String updateCustomization(
            @McpToolParam(description = "Customization id.") Integer customizationId,
            @McpToolParam(description = "Customization name.") String name,
            @McpToolParam(description = "Optional description.") String description,
            @McpToolParam(description = "Minimum selections.") Integer minimumSelection,
            @McpToolParam(description = "Maximum selections.") Integer maximumSelection,
            @McpToolParam(description = "Optional active flag.") Boolean active,
            @McpToolParam(description = "Whether a selection is required.") Boolean required,
            @McpToolParam(description = "Whether an option may be selected with quantity greater than one.") Boolean allowQuantity,
            @McpToolParam(description = "Selection type: SINGLE or MULTIPLE.") String selectionType,
            @McpToolParam(description = "Complete option list. Each option has name and optional tierPrices.") List<CatalogCommands.CustomizationOptionCommand> customizationOptions) {
        return ToolExecutor.call(() -> catalog.updateCustomization(customizationId, customization(
                customizationId, name, description, minimumSelection, maximumSelection, active, required,
                allowQuantity, selectionType, customizationOptions)));
    }

    @McpTool(name = "pos_configure_product_customization", description = """
            Configure per-product overrides for an existing customization link.
            Use link ids returned by pos_get_product.
            """)
    public String configureProductCustomization(
            @McpToolParam(description = "Product id.") Integer productId,
            @McpToolParam(description = "Product-customization link id.") Integer linkId,
            @McpToolParam(description = "Optional required override.") Boolean requiredOverride,
            @McpToolParam(description = "Optional minimum-selection override.") Integer minSelectionOverride,
            @McpToolParam(description = "Optional maximum-selection override.") Integer maxSelectionOverride,
            @McpToolParam(description = "Optional sort order.") Integer sortOrder) {
        return ToolExecutor.call(() -> catalog.configureProductCustomization(productId, linkId,
                new CatalogCommands.ProductCustomizationConfigCommand(
                        requiredOverride, minSelectionOverride, maxSelectionOverride, sortOrder)));
    }

    @McpTool(name = "pos_validate_order_items", description = """
            Validate product, SKU, customization rules and official prices for a store.
            Use this before confirming an order. This does not create an order.
            """)
    public String validateOrderItems(
            @McpToolParam(description = "Store id from pos_get_current_user.") Integer storeId,
            @McpToolParam(description = "Order lines. Each line has productId, skuId, quantity and optional customizations.") List<ValidateOrderItemsCommand.Item> items) {
        return ToolExecutor.call(() -> catalog.validateOrderItems(new ValidateOrderItemsCommand(storeId, items)));
    }

    @McpTool(name = "pos_calculate_customization_price", description = """
            Calculate the official customization surcharge for a product/SKU line.
            Only options linked to the SKU are accepted. Use pos_validate_order_items
            for full line validation before ordering. This does not create an order.
            """)
    public String calculateCustomizationPrice(
            @McpToolParam(description = "Product id.") Integer productId,
            @McpToolParam(description = "SKU id.") Integer skuId,
            @McpToolParam(description = "Quantity, defaults to 1.") Integer quantity,
            @McpToolParam(description = "Selected customization option ids.") List<Integer> customizationOptionIds) {
        return ToolExecutor.call(() -> catalog.calculateCustomizationPrice(
                new CustomizationPriceCommand(productId, skuId, quantity, customizationOptionIds)));
    }

    private CatalogCommands.ProductImageCommand image(Integer productImageId) {
        return productImageId == null ? null : new CatalogCommands.ProductImageCommand(productImageId);
    }

    private CatalogCommands.CustomizationCommand customization(
            Integer id, String name, String description, Integer minimumSelection, Integer maximumSelection,
            Boolean active, Boolean required, Boolean allowQuantity, String selectionType,
            List<CatalogCommands.CustomizationOptionCommand> customizationOptions) {
        return new CatalogCommands.CustomizationCommand(id, name, description, minimumSelection,
                maximumSelection, active, required, allowQuantity, selectionType, customizationOptions);
    }
}
