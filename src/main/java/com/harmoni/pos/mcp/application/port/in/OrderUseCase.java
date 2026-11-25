package com.harmoni.pos.mcp.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CreateOrderCommand;
import com.harmoni.pos.mcp.domain.PayOrderCommand;

import java.util.List;

public interface OrderUseCase {

    JsonNode listOrders(Integer limit, Integer beforeId);

    JsonNode calculateOrder(CreateOrderCommand command);

    JsonNode createOrder(CreateOrderCommand command, String idempotencyKey);

    JsonNode payOrder(PayOrderCommand command);

    JsonNode report(String reportType, String start, String end, List<Integer> storeIds, Integer limit);

    JsonNode getCustomerOrders(long customerId, String from, String to);

    JsonNode bestSellers(String start, String end, List<Integer> storeIds, Integer limit);

    JsonNode salesTotals(String start, String end, List<Integer> storeIds);

    JsonNode dailySales(String start, String end, List<Integer> storeIds);

    JsonNode settlement(String start, String end, List<Integer> storeIds);

    JsonNode getOrder(long orderId);

    JsonNode voidOrder(long orderId, Boolean confirmed);
}
