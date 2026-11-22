package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.out.CatalogPort;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CatalogCommands;
import com.harmoni.pos.mcp.domain.CustomizationPriceCommand;
import com.harmoni.pos.mcp.domain.ValidateOrderItemsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CatalogClientAdapter implements CatalogPort {

    @Qualifier("menuRestClient")
    private final RestClient menuRestClient;

    private final JsonRestClient http;

    @Override
    public JsonNode getCurrentUser(CallerContext caller) {
        String username = UriUtils.encodePathSegment(caller.username(), StandardCharsets.UTF_8);
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/user/" + username, caller, Map.of()));
    }

    @Override
    public JsonNode listBrands(CallerContext caller) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/brand", caller, Map.of()));
    }

    @Override
    public JsonNode listTiers(CallerContext caller, int brandId) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/tier/brand/" + brandId, caller, Map.of()));
    }

    @Override
    public JsonNode getProductSkus(CallerContext caller, long productId) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/product/" + productId + "/sku",
                caller, Map.of()));
    }

    @Override
    public JsonNode getSkuTierPrices(CallerContext caller, List<Integer> skuIds, int tierId) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("skuIds", skuIds);
        query.put("tierId", tierId);
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/skutier", caller, query));
    }

    @Override
    public JsonNode createCategory(CallerContext caller, CatalogCommands.CategoryCommand command) {
        return JsonData.unwrap(http.post(menuRestClient, "/api/v1/category", caller, command));
    }

    @Override
    public JsonNode updateCategory(CallerContext caller, int categoryId,
                                   CatalogCommands.CategoryCommand command) {
        return JsonData.unwrap(http.put(menuRestClient, "/api/v1/category/" + categoryId, caller, command));
    }

    @Override
    public JsonNode createProduct(CallerContext caller, CatalogCommands.ProductCommand command) {
        return JsonData.unwrap(http.post(menuRestClient, "/api/v1/product", caller, command));
    }

    @Override
    public JsonNode updateProduct(CallerContext caller, CatalogCommands.ProductCommand command) {
        return JsonData.unwrap(http.put(menuRestClient, "/api/v1/product", caller, withSkuIds(command)));
    }

    @Override
    public JsonNode createCustomization(CallerContext caller, CatalogCommands.CustomizationCommand command) {
        return JsonData.unwrap(http.post(menuRestClient, "/api/v1/customization", caller, command));
    }

    @Override
    public JsonNode updateCustomization(CallerContext caller, int customizationId,
                                        CatalogCommands.CustomizationCommand command) {
        return JsonData.unwrap(http.put(menuRestClient, "/api/v1/customization/" + customizationId,
                caller, command));
    }

    @Override
    public JsonNode configureProductCustomization(CallerContext caller, int productId, int linkId,
                                                  CatalogCommands.ProductCustomizationConfigCommand command) {
        return JsonData.unwrap(http.put(menuRestClient,
                "/api/v1/product/" + productId + "/customization/" + linkId, caller, command));
    }

    @Override
    public JsonNode validateOrderItems(CallerContext caller, ValidateOrderItemsCommand command) {
        return JsonData.unwrap(http.post(menuRestClient, "/internal/v1/menu/validate-order-items",
                caller, command));
    }

    @Override
    public JsonNode calculateCustomizationPrice(CallerContext caller, CustomizationPriceCommand command) {
        return JsonData.unwrap(http.post(menuRestClient, "/api/v1/customization/price", caller, command));
    }

    private CatalogCommands.ProductCommand withSkuIds(CatalogCommands.ProductCommand command) {
        return new CatalogCommands.ProductCommand(command.id(), command.name(), command.categoryId(),
                command.description(), command.productImage(),
                command.skus().stream().map(this::withSkuId).toList(), command.customizationIds());
    }

    private CatalogCommands.SkuCommand withSkuId(CatalogCommands.SkuCommand sku) {
        return new CatalogCommands.SkuCommand(sku.id(), sku.name(), sku.productId(),
                sku.tierPrices().stream()
                        .map(price -> new CatalogCommands.TierPriceCommand(
                                price.skuId() == null ? sku.id() : price.skuId(),
                                price.tierId(), price.price()))
                        .toList());
    }
}
