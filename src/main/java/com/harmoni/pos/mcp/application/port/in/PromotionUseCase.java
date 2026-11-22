package com.harmoni.pos.mcp.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.PromotionCommands;
import com.harmoni.pos.mcp.domain.PromotionPriceCommand;

public interface PromotionUseCase {

    JsonNode createPromotion(PromotionCommands.PromotionCommand command);

    JsonNode updatePromotion(PromotionCommands.PromotionCommand command);

    JsonNode setPromotionStatus(long promotionId, String status);

    JsonNode calculatePromotionPrice(PromotionPriceCommand command);
}
