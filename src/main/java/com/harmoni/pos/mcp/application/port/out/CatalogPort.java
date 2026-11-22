package com.harmoni.pos.mcp.application.port.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CatalogCommands;
import com.harmoni.pos.mcp.domain.CustomizationPriceCommand;
import com.harmoni.pos.mcp.domain.ValidateOrderItemsCommand;

import java.util.List;

public interface CatalogPort {

    JsonNode getCurrentUser(CallerContext caller);

    JsonNode listBrands(CallerContext caller);

    JsonNode listTiers(CallerContext caller, int brandId);

    JsonNode getProductSkus(CallerContext caller, long productId);

    JsonNode getSkuTierPrices(CallerContext caller, List<Integer> skuIds, int tierId);

    JsonNode createCategory(CallerContext caller, CatalogCommands.CategoryCommand command);

    JsonNode updateCategory(CallerContext caller, int categoryId, CatalogCommands.CategoryCommand command);

    JsonNode createProduct(CallerContext caller, CatalogCommands.ProductCommand command);

    JsonNode updateProduct(CallerContext caller, CatalogCommands.ProductCommand command);

    JsonNode createCustomization(CallerContext caller, CatalogCommands.CustomizationCommand command);

    JsonNode updateCustomization(CallerContext caller, int customizationId,
                                 CatalogCommands.CustomizationCommand command);

    JsonNode configureProductCustomization(CallerContext caller, int productId, int linkId,
                                           CatalogCommands.ProductCustomizationConfigCommand command);

    JsonNode validateOrderItems(CallerContext caller, ValidateOrderItemsCommand command);

    JsonNode calculateCustomizationPrice(CallerContext caller, CustomizationPriceCommand command);
}
