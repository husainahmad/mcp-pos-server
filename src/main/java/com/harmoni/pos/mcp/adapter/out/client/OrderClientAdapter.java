package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.out.OrderPort;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CreateOrderCommand;
import com.harmoni.pos.mcp.domain.PayOrderCommand;
import com.harmoni.pos.mcp.domain.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderClientAdapter implements OrderPort {

    @Qualifier("orderRestClient")
    private final RestClient orderRestClient;

    private final JsonRestClient http;

    @Override
    public JsonNode listOrders(CallerContext caller, int limit, Integer beforeId) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("limit", limit);
        if (beforeId != null) {
            query.put("beforeId", beforeId);
        }
        return JsonData.unwrap(http.get(orderRestClient, "/api/v1/order/page", caller, query));
    }

    @Override
    public JsonNode calculateOrder(CallerContext caller, CreateOrderCommand command) {
        return JsonData.unwrap(http.post(orderRestClient, "/api/v1/order/print", caller,
                orderBody(command)));
    }

    @Override
    public JsonNode createOrder(CallerContext caller, CreateOrderCommand command, String idempotencyKey) {
        return JsonData.unwrap(http.post(orderRestClient, "/api/v1/order", caller,
                orderBody(command), Map.of("Idempotency-Key", idempotencyKey)));
    }

    @Override
    public JsonNode payOrder(CallerContext caller, PayOrderCommand command) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderId", command.orderId());
        body.put("paymentId", command.paymentId());
        return JsonData.unwrap(http.put(orderRestClient, "/api/v1/order", caller, body));
    }

    @Override
    public JsonNode report(CallerContext caller, ReportType type, LocalDateTime start, LocalDateTime end,
                           List<Integer> storeIds, int limit) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("start", start.toString());
        query.put("end", end.toString());
        if (storeIds != null && !storeIds.isEmpty()) {
            query.put("storeIds", storeIds);
        }
        if (type == ReportType.TOP_PRODUCTS) {
            query.put("limit", limit);
        }
        return JsonData.unwrap(http.get(orderRestClient, "/api/v1/reports/" + type.path(), caller, query));
    }

    @Override
    public JsonNode getCustomerOrders(CallerContext caller, long customerId,
                                      LocalDateTime from, LocalDateTime to) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("from", from.toString());
        query.put("to", to.toString());
        return JsonData.unwrap(http.get(orderRestClient,
                "/api/v1/order-customers/customer/" + customerId + "/history", caller, query));
    }

    @Override
    public JsonNode getOrder(CallerContext caller, long orderId) {
        return JsonData.unwrap(http.get(orderRestClient, "/api/v1/order/" + orderId, caller, Map.of()));
    }

    @Override
    public JsonNode voidOrder(CallerContext caller, long orderId) {
        return JsonData.unwrap(http.put(orderRestClient, "/api/v1/order/" + orderId + "/void",
                caller, Map.of()));
    }

    private Map<String, Object> orderBody(CreateOrderCommand command) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (command.customerId() != null && command.customerId() > 0) {
            body.put("customerId", command.customerId());
        }
        putIfPresent(body, "tableNumber", command.tableNumber());
        if (command.storeServiceTypesId() != null) {
            body.put("storeServiceTypesId", command.storeServiceTypesId());
        }
        putIfPresent(body, "remark", command.remark());
        body.put("orderDetails", command.orderDetails().stream().map(this::detailBody).toList());
        return body;
    }

    private Map<String, Object> detailBody(CreateOrderCommand.OrderLineCommand line) {
        Map<String, Object> sku = new LinkedHashMap<>();
        sku.put("skuId", line.skuId());
        sku.put("quantity", line.quantity());
        sku.put("customizations", line.customizations() == null
                ? List.of()
                : line.customizations().stream().map(this::groupBody).toList());

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("productId", line.productId());
        detail.put("orderDetailSkus", List.of(sku));
        return detail;
    }

    private Map<String, Object> groupBody(CreateOrderCommand.CustomizationGroupCommand group) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customizationId", group.customizationId());
        body.put("options", group.options().stream().map(option -> {
            Map<String, Object> optionBody = new LinkedHashMap<>();
            optionBody.put("customizationOptionId", option.customizationOptionId());
            optionBody.put("quantity", option.quantity() == null ? 1 : option.quantity());
            return optionBody;
        }).toList());
        return body;
    }

    private void putIfPresent(Map<String, Object> body, String key, String value) {
        if (value != null && !value.isBlank()) {
            body.put(key, value.trim());
        }
    }
}
