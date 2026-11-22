package com.harmoni.pos.mcp.domain;

import java.util.List;

public record ValidateOrderItemsCommand(Integer storeId, List<Item> items) {

    public record Item(Integer productId, Integer skuId, Integer quantity,
                       List<CreateOrderCommand.CustomizationGroupCommand> customizations) {
    }
}
