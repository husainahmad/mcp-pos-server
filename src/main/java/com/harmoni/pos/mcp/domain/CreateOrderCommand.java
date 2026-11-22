package com.harmoni.pos.mcp.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CreateOrderCommand(
        Integer customerId,
        String tableNumber,
        Integer storeServiceTypesId,
        String remark,
        @NotEmpty @Valid List<OrderLineCommand> orderDetails) {

    public record OrderLineCommand(
            @Positive Integer productId,
            @Positive Integer skuId,
            @Positive int quantity,
            @Valid List<CustomizationGroupCommand> customizations) {
    }

    public record CustomizationGroupCommand(
            @Positive Integer customizationId,
            @NotEmpty @Valid List<CustomizationOptionCommand> options) {
    }

    public record CustomizationOptionCommand(
            @Positive Integer customizationOptionId,
            @Positive Integer quantity) {
    }
}
