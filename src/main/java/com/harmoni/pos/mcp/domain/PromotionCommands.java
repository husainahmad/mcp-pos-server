package com.harmoni.pos.mcp.domain;

import java.math.BigDecimal;
import java.util.List;

public final class PromotionCommands {

    private PromotionCommands() {
    }

    public record PromotionCommand(Long id, Integer brandId, String code, String name, String description,
                                   String promotionType, String status, Integer priority, String startDate,
                                   String endDate, List<ScheduleCommand> schedules, List<TargetCommand> targets,
                                   List<RuleCommand> rules, List<SpecialPriceCommand> specialPrices,
                                   List<ScopeCommand> scopes) {
    }

    public record ScheduleCommand(String dayOfWeek, String startTime, String endTime, Boolean enabled) {
    }

    public record TargetCommand(String targetType, Long productId, Long skuId, Long categoryId) {
    }

    public record RuleCommand(String ruleType, BigDecimal discountValue, BigDecimal maxDiscountAmount,
                              BigDecimal minQuantity, BigDecimal minAmount) {
    }

    public record SpecialPriceCommand(Long skuId, BigDecimal specialPrice) {
    }

    public record ScopeCommand(String scopeType, Long scopeId) {
    }
}
