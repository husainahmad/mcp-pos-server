package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.in.PromotionUseCase;
import com.harmoni.pos.mcp.application.port.out.PromotionPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.PromotionCommands;
import com.harmoni.pos.mcp.domain.PromotionPriceCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromotionApplicationService implements PromotionUseCase {

    private final PromotionPort promotionPort;

    @Override
    public JsonNode createPromotion(PromotionCommands.PromotionCommand command) {
        validatePromotion(command, false);
        return promotionPort.createPromotion(CallerContextHolder.current(), command);
    }

    @Override
    public JsonNode updatePromotion(PromotionCommands.PromotionCommand command) {
        validatePromotion(command, true);
        return promotionPort.updatePromotion(CallerContextHolder.current(), command);
    }

    @Override
    public JsonNode setPromotionStatus(long promotionId, String status) {
        return promotionPort.setPromotionStatus(CallerContextHolder.current(),
                ArgumentChecks.positive(promotionId, "promotionId"),
                ArgumentChecks.required(status, "status").toUpperCase());
    }

    @Override
    public JsonNode calculatePromotionPrice(PromotionPriceCommand command) {
        ArgumentChecks.required(command, "request");
        ArgumentChecks.requiredList(command.lines(), "lines").forEach(this::validateLine);
        validateContext(command.context());
        return promotionPort.calculatePromotionPrice(CallerContextHolder.current(), command);
    }

    private void validatePromotion(PromotionCommands.PromotionCommand command, boolean update) {
        ArgumentChecks.required(command, "promotion");
        if (update) {
            ArgumentChecks.required(command.id(), "promotion.id");
        }
        ArgumentChecks.positive(command.brandId(), "promotion.brandId");
        ArgumentChecks.required(command.code(), "promotion.code");
        ArgumentChecks.required(command.name(), "promotion.name");
        ArgumentChecks.required(command.promotionType(), "promotion.promotionType");
        ArgumentChecks.required(command.status(), "promotion.status");
    }

    private void validateLine(PromotionPriceCommand.Line line) {
        ArgumentChecks.required(line, "line");
        ArgumentChecks.required(line.lineIndex(), "line.lineIndex");
        ArgumentChecks.required(line.productId(), "line.productId");
        ArgumentChecks.required(line.skuId(), "line.skuId");
        ArgumentChecks.required(line.unitPrice(), "line.unitPrice");
        ArgumentChecks.required(line.quantity(), "line.quantity");
    }

    private void validateContext(PromotionPriceCommand.Context context) {
        ArgumentChecks.required(context, "context");
        ArgumentChecks.required(context.storeId(), "context.storeId");
    }
}
