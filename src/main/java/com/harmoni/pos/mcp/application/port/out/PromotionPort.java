package com.harmoni.pos.mcp.application.port.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.PromotionCommands;
import com.harmoni.pos.mcp.domain.PromotionPriceCommand;

public interface PromotionPort {

    JsonNode createPromotion(CallerContext caller, PromotionCommands.PromotionCommand command);

    JsonNode updatePromotion(CallerContext caller, PromotionCommands.PromotionCommand command);

    JsonNode setPromotionStatus(CallerContext caller, long promotionId, String status);

    JsonNode calculatePromotionPrice(CallerContext caller, PromotionPriceCommand command);
}
