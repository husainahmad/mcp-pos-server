package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.in.CatalogUseCase;
import com.harmoni.pos.mcp.application.port.out.CatalogPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CatalogCommands;
import com.harmoni.pos.mcp.domain.CustomizationPriceCommand;
import com.harmoni.pos.mcp.domain.ValidateOrderItemsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogApplicationService implements CatalogUseCase {

    private final CatalogPort catalogPort;

    @Override
    public JsonNode getCurrentUser() {
        return catalogPort.getCurrentUser(CallerContextHolder.current());
    }

    @Override
    public JsonNode listBrands() {
        return catalogPort.listBrands(CallerContextHolder.current());
    }

    @Override
    public JsonNode listTiers(Integer brandId) {
        return catalogPort.listTiers(CallerContextHolder.current(),
                ArgumentChecks.positive(brandId, "brandId"));
    }

    @Override
    public JsonNode getProductSkus(long productId) {
        return catalogPort.getProductSkus(CallerContextHolder.current(),
                ArgumentChecks.positive(productId, "productId"));
    }

    @Override
    public JsonNode getSkuTierPrices(List<Integer> skuIds, Integer tierId) {
        ArgumentChecks.requiredList(skuIds, "skuIds");
        return catalogPort.getSkuTierPrices(CallerContextHolder.current(), skuIds,
                ArgumentChecks.positive(tierId, "tierId"));
    }

    @Override
    public JsonNode createCategory(CatalogCommands.CategoryCommand command) {
        validateCategory(command);
        return catalogPort.createCategory(CallerContextHolder.current(), command);
    }

    @Override
    public JsonNode updateCategory(Integer categoryId, CatalogCommands.CategoryCommand command) {
        validateCategory(command);
        return catalogPort.updateCategory(CallerContextHolder.current(),
                ArgumentChecks.positive(categoryId, "categoryId"), command);
    }

    @Override
    public JsonNode createProduct(CatalogCommands.ProductCommand command) {
        validateProduct(command, false);
        return catalogPort.createProduct(CallerContextHolder.current(), command);
    }

    @Override
    public JsonNode updateProduct(CatalogCommands.ProductCommand command) {
        ArgumentChecks.required(command, "product");
        ArgumentChecks.positive(command.id(), "product.id");
        validateProduct(command, true);
        return catalogPort.updateProduct(CallerContextHolder.current(), command);
    }

    @Override
    public JsonNode createCustomization(CatalogCommands.CustomizationCommand command) {
        validateCustomization(command);
        return catalogPort.createCustomization(CallerContextHolder.current(), command);
    }

    @Override
    public JsonNode updateCustomization(Integer customizationId,
                                        CatalogCommands.CustomizationCommand command) {
        validateCustomization(command);
        return catalogPort.updateCustomization(CallerContextHolder.current(),
                ArgumentChecks.positive(customizationId, "customizationId"), command);
    }

    @Override
    public JsonNode configureProductCustomization(Integer productId, Integer linkId,
                                                  CatalogCommands.ProductCustomizationConfigCommand command) {
        ArgumentChecks.required(command, "configuration");
        return catalogPort.configureProductCustomization(CallerContextHolder.current(),
                ArgumentChecks.positive(productId, "productId"),
                ArgumentChecks.positive(linkId, "linkId"), command);
    }

    @Override
    public JsonNode validateOrderItems(ValidateOrderItemsCommand command) {
        ArgumentChecks.required(command, "request");
        ArgumentChecks.positive(command.storeId(), "storeId");
        ArgumentChecks.requiredList(command.items(), "items").forEach(this::validateOrderItem);
        return catalogPort.validateOrderItems(CallerContextHolder.current(), command);
    }

    @Override
    public JsonNode calculateCustomizationPrice(CustomizationPriceCommand command) {
        ArgumentChecks.required(command, "request");
        ArgumentChecks.positive(command.productId(), "productId");
        ArgumentChecks.positive(command.skuId(), "skuId");
        int quantity = command.quantity() == null ? 1 : ArgumentChecks.positive(command.quantity(), "quantity");
        return catalogPort.calculateCustomizationPrice(CallerContextHolder.current(),
                new CustomizationPriceCommand(command.productId(), command.skuId(), quantity,
                        command.customizationOptionIds() == null ? List.of() : command.customizationOptionIds()));
    }

    private void validateCategory(CatalogCommands.CategoryCommand command) {
        ArgumentChecks.required(command, "category");
        ArgumentChecks.required(command.name(), "category.name");
        ArgumentChecks.required(command.description(), "category.description");
        ArgumentChecks.positive(command.brandId(), "category.brandId");
    }

    private void validateProduct(CatalogCommands.ProductCommand command, boolean update) {
        ArgumentChecks.required(command, "product");
        ArgumentChecks.required(command.name(), "product.name");
        ArgumentChecks.positive(command.categoryId(), "product.categoryId");
        ArgumentChecks.requiredList(command.skus(), "product.skus").forEach(sku -> validateSku(sku, update));
    }

    private void validateSku(CatalogCommands.SkuCommand sku, boolean update) {
        ArgumentChecks.required(sku, "sku");
        if (update) {
            ArgumentChecks.positive(sku.id(), "sku.id");
        }
        ArgumentChecks.required(sku.name(), "sku.name");
        ArgumentChecks.requiredList(sku.tierPrices(), "sku.tierPrices").forEach(this::validateTierPrice);
    }

    private void validateTierPrice(CatalogCommands.TierPriceCommand tierPrice) {
        ArgumentChecks.required(tierPrice, "tierPrice");
        ArgumentChecks.positive(tierPrice.tierId(), "tierPrice.tierId");
        requirePrice(tierPrice.price(), "tierPrice.price");
    }

    private void validateCustomization(CatalogCommands.CustomizationCommand command) {
        ArgumentChecks.required(command, "customization");
        ArgumentChecks.required(command.name(), "customization.name");
        if (command.customizationOptions() == null) {
            return;
        }
        command.customizationOptions().forEach(option -> {
            ArgumentChecks.required(option, "customizationOption");
            ArgumentChecks.required(option.name(), "customizationOption.name");
            if (option.tierPrices() != null) {
                option.tierPrices().forEach(tierPrice -> {
                    ArgumentChecks.required(tierPrice, "customizationOptionTierPrice");
                    ArgumentChecks.positive(tierPrice.tierId(), "customizationOptionTierPrice.tierId");
                    requirePrice(tierPrice.price(), "customizationOptionTierPrice.price");
                });
            }
        });
    }

    private void validateOrderItem(ValidateOrderItemsCommand.Item item) {
        ArgumentChecks.required(item, "order item");
        ArgumentChecks.positive(item.productId(), "item.productId");
        ArgumentChecks.positive(item.skuId(), "item.skuId");
        ArgumentChecks.positive(item.quantity(), "item.quantity");
    }

    private void requirePrice(BigDecimal price, String field) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException(field + " must be zero or positive");
        }
    }
}
