package com.harmoni.pos.mcp.domain;

import java.math.BigDecimal;
import java.util.List;

public record PromotionPriceCommand(List<Line> lines, Context context) {

    public record Line(Integer lineIndex, Long productId, Long skuId, Long categoryId,
                       BigDecimal unitPrice, BigDecimal quantity) {
    }

    public record Context(Long storeId, Long chainId, Long brandId, Long tenantId, String zone) {
    }
}
