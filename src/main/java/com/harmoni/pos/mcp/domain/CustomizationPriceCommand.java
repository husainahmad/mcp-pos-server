package com.harmoni.pos.mcp.domain;

import java.util.List;

public record CustomizationPriceCommand(Integer productId, Integer skuId, Integer quantity,
                                        List<Integer> customizationOptionIds) {
}
