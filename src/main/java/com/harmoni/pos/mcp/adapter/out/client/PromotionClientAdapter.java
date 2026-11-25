package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.out.PromotionPort;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.PromotionCommands;
import com.harmoni.pos.mcp.domain.PromotionPriceCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PromotionClientAdapter implements PromotionPort {

    @Qualifier("menuRestClient")
    private final RestClient menuRestClient;

    private final JsonRestClient http;

    @Override
    public JsonNode createPromotion(CallerContext caller, PromotionCommands.PromotionCommand command) {
        return JsonData.unwrap(http.post(menuRestClient, "/api/v1/promotion", caller, command));
    }

    @Override
    public JsonNode updatePromotion(CallerContext caller, PromotionCommands.PromotionCommand command) {
        return JsonData.unwrap(http.put(menuRestClient, "/api/v1/promotion", caller, command));
    }

    @Override
    public JsonNode setPromotionStatus(CallerContext caller, long promotionId, String status) {
        return JsonData.unwrap(http.patch(menuRestClient, "/api/v1/promotion/" + promotionId + "/status",
                caller, Map.of("status", status)));
    }

    @Override
    public JsonNode calculatePromotionPrice(CallerContext caller, PromotionPriceCommand command) {
        return JsonData.unwrap(http.post(menuRestClient, "/api/v1/promotion/price", caller, command));
    }

    @Override
    public JsonNode getPromotion(CallerContext caller, Long id, String code) {
        if (id != null) {
            return JsonData.unwrap(http.get(menuRestClient, "/api/v1/promotion/" + id, caller, Map.of()));
        }
        String encoded = UriUtils.encodePathSegment(code, StandardCharsets.UTF_8);
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/promotion/code/" + encoded, caller, Map.of()));
    }

    @Override
    public JsonNode listRedeemablePromotions(CallerContext caller) {
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/promotion/redeemable", caller, Map.of()));
    }

    @Override
    public JsonNode findPromotionsForCatalogItem(CallerContext caller, int productId, int skuId, int categoryId) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("productId", productId);
        query.put("skuId", skuId);
        query.put("categoryId", categoryId);
        return JsonData.unwrap(http.get(menuRestClient, "/api/v1/promotion/for-catalog-item", caller, query));
    }
}
