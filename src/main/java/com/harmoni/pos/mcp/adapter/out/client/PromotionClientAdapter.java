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
}
