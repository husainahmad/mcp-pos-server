package com.harmoni.pos.mcp.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CatalogCommands;
import com.harmoni.pos.mcp.domain.CustomizationPriceCommand;
import com.harmoni.pos.mcp.domain.ValidateOrderItemsCommand;

import java.util.List;

public interface CatalogUseCase {

    JsonNode getCurrentUser();

    JsonNode listBrands();

    JsonNode listTiers(Integer brandId);

    JsonNode getProductSkus(long productId);

    JsonNode getSkuTierPrices(List<Integer> skuIds, Integer tierId);

    JsonNode createCategory(CatalogCommands.CategoryCommand command);

    JsonNode updateCategory(Integer categoryId, CatalogCommands.CategoryCommand command);

    JsonNode createProduct(CatalogCommands.ProductCommand command);

    JsonNode updateProduct(CatalogCommands.ProductCommand command);

    JsonNode createCustomization(CatalogCommands.CustomizationCommand command);

    JsonNode updateCustomization(Integer customizationId, CatalogCommands.CustomizationCommand command);

    JsonNode configureProductCustomization(Integer productId, Integer linkId,
                                          CatalogCommands.ProductCustomizationConfigCommand command);

    JsonNode validateOrderItems(ValidateOrderItemsCommand command);

    JsonNode calculateCustomizationPrice(CustomizationPriceCommand command);
}
