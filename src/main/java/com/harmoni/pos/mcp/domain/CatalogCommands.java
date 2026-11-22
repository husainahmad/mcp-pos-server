package com.harmoni.pos.mcp.domain;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogCommands {

    private CatalogCommands() {
    }

    public record CategoryCommand(String name, String description, Integer brandId, Boolean active) {
    }

    public record ProductCommand(Integer id, String name, Integer categoryId, String description,
                                 ProductImageCommand productImage, List<SkuCommand> skus,
                                 List<Integer> customizationIds) {
    }

    public record ProductImageCommand(Integer id) {
    }

    public record SkuCommand(Integer id, String name, Integer productId, List<TierPriceCommand> tierPrices) {
    }

    public record TierPriceCommand(Integer skuId, Integer tierId, BigDecimal price) {
    }

    public record CustomizationCommand(Integer id, String name, String description, Integer minimumSelection,
                                       Integer maximumSelection, Boolean active, Boolean required,
                                       Boolean allowQuantity, String selectionType,
                                       List<CustomizationOptionCommand> customizationOptions) {
    }

    public record CustomizationOptionCommand(Integer id, String name, Integer customizationId,
                                             List<CustomizationOptionTierPriceCommand> tierPrices) {
    }

    public record CustomizationOptionTierPriceCommand(Integer tierId, BigDecimal price) {
    }

    public record ProductCustomizationConfigCommand(Boolean requiredOverride, Integer minSelectionOverride,
                                                    Integer maxSelectionOverride, Integer sortOrder) {
    }
}
