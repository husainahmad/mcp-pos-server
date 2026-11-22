package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.PromotionUseCase;
import com.harmoni.pos.mcp.domain.PromotionCommands;
import com.harmoni.pos.mcp.domain.PromotionPriceCommand;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PromotionMcpTools {

    private final PromotionUseCase promotions;

    @McpTool(name = "pos_create_promotion", description = """
            Create a promotion with optional schedules, targets, rules, special prices and scopes.
            Use real brand, product, SKU and category ids. BUY_X_GET_Y and BUNDLE are not supported by pricing.
            """)
    public String createPromotion(
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Promotion code.") String code,
            @McpToolParam(description = "Promotion name.") String name,
            @McpToolParam(description = "Optional description.") String description,
            @McpToolParam(description = "Type: PERCENTAGE, FIXED_AMOUNT, SPECIAL_PRICE, BUY_X_GET_Y or BUNDLE.") String promotionType,
            @McpToolParam(description = "Status: DRAFT, SCHEDULED, ACTIVE, PAUSED, EXPIRED or CANCELLED.") String status,
            @McpToolParam(description = "Optional priority, defaults to 0.") Integer priority,
            @McpToolParam(description = "Optional start date, ISO yyyy-MM-dd.") String startDate,
            @McpToolParam(description = "Optional end date, ISO yyyy-MM-dd.") String endDate,
            @McpToolParam(description = "Optional schedules.") List<PromotionCommands.ScheduleCommand> schedules,
            @McpToolParam(description = "Optional targets.") List<PromotionCommands.TargetCommand> targets,
            @McpToolParam(description = "Optional rules.") List<PromotionCommands.RuleCommand> rules,
            @McpToolParam(description = "Optional special prices.") List<PromotionCommands.SpecialPriceCommand> specialPrices,
            @McpToolParam(description = "Optional scopes.") List<PromotionCommands.ScopeCommand> scopes) {
        return ToolExecutor.call(() -> promotions.createPromotion(promotion(
                null, brandId, code, name, description, promotionType, status, priority, startDate, endDate,
                schedules, targets, rules, specialPrices, scopes)));
    }

    @McpTool(name = "pos_update_promotion", description = """
            Update a promotion. Omit a child list to keep it, or send an empty list to clear it.
            """)
    public String updatePromotion(
            @McpToolParam(description = "Promotion id.") Long promotionId,
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Promotion code.") String code,
            @McpToolParam(description = "Promotion name.") String name,
            @McpToolParam(description = "Optional description.") String description,
            @McpToolParam(description = "Type: PERCENTAGE, FIXED_AMOUNT, SPECIAL_PRICE, BUY_X_GET_Y or BUNDLE.") String promotionType,
            @McpToolParam(description = "Status: DRAFT, SCHEDULED, ACTIVE, PAUSED, EXPIRED or CANCELLED.") String status,
            @McpToolParam(description = "Optional priority, defaults to 0.") Integer priority,
            @McpToolParam(description = "Optional start date, ISO yyyy-MM-dd.") String startDate,
            @McpToolParam(description = "Optional end date, ISO yyyy-MM-dd.") String endDate,
            @McpToolParam(description = "Optional schedules.") List<PromotionCommands.ScheduleCommand> schedules,
            @McpToolParam(description = "Optional targets.") List<PromotionCommands.TargetCommand> targets,
            @McpToolParam(description = "Optional rules.") List<PromotionCommands.RuleCommand> rules,
            @McpToolParam(description = "Optional special prices.") List<PromotionCommands.SpecialPriceCommand> specialPrices,
            @McpToolParam(description = "Optional scopes.") List<PromotionCommands.ScopeCommand> scopes) {
        return ToolExecutor.call(() -> promotions.updatePromotion(promotion(
                promotionId, brandId, code, name, description, promotionType, status, priority, startDate,
                endDate, schedules, targets, rules, specialPrices, scopes)));
    }

    @McpTool(name = "pos_set_promotion_status", description = """
            Change a promotion's status. Use a real promotion id from pos_list_promotions.
            """)
    public String setPromotionStatus(
            @McpToolParam(description = "Promotion id.") long promotionId,
            @McpToolParam(description = "New status.") String status) {
        return ToolExecutor.call(() -> promotions.setPromotionStatus(promotionId, status));
    }

    @McpTool(name = "pos_calculate_promotion_price", description = """
            Calculate promotion discounts for a basket. This is read-only and does not apply or persist a promotion.
            Use official unit prices from menu tools. Provide the store context.
            """)
    public String calculatePromotionPrice(
            @McpToolParam(description = "Cart lines with lineIndex, productId, skuId, categoryId, unitPrice and quantity.") List<PromotionPriceCommand.Line> lines,
            @McpToolParam(description = "Pricing context with storeId and optional chainId, brandId, tenantId, zone.") PromotionPriceCommand.Context context) {
        return ToolExecutor.call(() -> promotions.calculatePromotionPrice(
                new PromotionPriceCommand(lines, context)));
    }

    private PromotionCommands.PromotionCommand promotion(
            Long id, Integer brandId, String code, String name, String description, String promotionType,
            String status, Integer priority, String startDate, String endDate,
            List<PromotionCommands.ScheduleCommand> schedules, List<PromotionCommands.TargetCommand> targets,
            List<PromotionCommands.RuleCommand> rules,
            List<PromotionCommands.SpecialPriceCommand> specialPrices,
            List<PromotionCommands.ScopeCommand> scopes) {
        return new PromotionCommands.PromotionCommand(id, brandId, code, name, description, promotionType,
                status, priority, startDate, endDate, schedules, targets, rules, specialPrices, scopes);
    }
}
