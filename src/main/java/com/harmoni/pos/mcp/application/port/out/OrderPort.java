package com.harmoni.pos.mcp.application.port.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CreateOrderCommand;
import com.harmoni.pos.mcp.domain.PayOrderCommand;
import com.harmoni.pos.mcp.domain.ReportType;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderPort {

    JsonNode listOrders(CallerContext caller, int limit, Integer beforeId);

    JsonNode calculateOrder(CallerContext caller, CreateOrderCommand command);

    JsonNode createOrder(CallerContext caller, CreateOrderCommand command, String idempotencyKey);

    JsonNode payOrder(CallerContext caller, PayOrderCommand command);

    JsonNode report(CallerContext caller, ReportType type, LocalDateTime start, LocalDateTime end,
                    List<Integer> storeIds, int limit);
}
