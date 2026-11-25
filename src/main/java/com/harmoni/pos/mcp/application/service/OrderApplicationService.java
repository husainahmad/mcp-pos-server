package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.in.OrderUseCase;
import com.harmoni.pos.mcp.application.port.out.OrderPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CreateOrderCommand;
import com.harmoni.pos.mcp.domain.PayOrderCommand;
import com.harmoni.pos.mcp.domain.ReportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderApplicationService implements OrderUseCase {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 200;

    private final OrderPort orderPort;

    @Override
    public JsonNode listOrders(Integer limit, Integer beforeId) {
        CallerContext caller = CallerContextHolder.current();
        return orderPort.listOrders(caller, boundedLimit(limit), beforeId);
    }

    @Override
    public JsonNode calculateOrder(CreateOrderCommand command) {
        CallerContext caller = CallerContextHolder.current();
        requireOrderLines(command);
        return orderPort.calculateOrder(caller, command);
    }

    @Override
    public JsonNode createOrder(CreateOrderCommand command, String idempotencyKey) {
        CallerContext caller = CallerContextHolder.current();
        requireOrderLines(command);
        String key = idempotencyKey == null || idempotencyKey.isBlank()
                ? UUID.randomUUID().toString()
                : idempotencyKey.trim();
        return orderPort.createOrder(caller, command, key);
    }

    @Override
    public JsonNode payOrder(PayOrderCommand command) {
        CallerContext caller = CallerContextHolder.current();
        ArgumentChecks.required(command, "payment");
        ArgumentChecks.positive(command.orderId(), "orderId");
        ArgumentChecks.positive(command.paymentId(), "paymentId");
        return orderPort.payOrder(caller, command);
    }

    @Override
    public JsonNode report(String reportType, String start, String end, List<Integer> storeIds, Integer limit) {
        CallerContext caller = CallerContextHolder.current();
        ReportType type = ReportType.from(reportType);
        LocalDateTime startTime = parseStart(start);
        LocalDateTime endTime = parseEnd(end);
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("start must be before end");
        }
        return orderPort.report(caller, type, startTime, endTime, storeIds, boundedLimit(limit));
    }

    @Override
    public JsonNode getCustomerOrders(long customerId, String from, String to) {
        CallerContext caller = CallerContextHolder.current();
        LocalDateTime startTime = parseStart(from);
        LocalDateTime endTime = parseEnd(to);
        validateRange(startTime, endTime);
        JsonNode result = orderPort.getCustomerOrders(caller,
                ArgumentChecks.positive(customerId, "customerId"), startTime, endTime);
        return ResultCaps.capArray(result);
    }

    @Override
    public JsonNode bestSellers(String start, String end, List<Integer> storeIds, Integer limit) {
        return cappedReport(ReportType.TOP_PRODUCTS, start, end, storeIds, limit);
    }

    @Override
    public JsonNode salesTotals(String start, String end, List<Integer> storeIds) {
        return cappedReport(ReportType.SALES, start, end, storeIds, null);
    }

    @Override
    public JsonNode dailySales(String start, String end, List<Integer> storeIds) {
        return cappedReport(ReportType.DAILY, start, end, storeIds, null);
    }

    @Override
    public JsonNode settlement(String start, String end, List<Integer> storeIds) {
        return cappedReport(ReportType.SETTLEMENT, start, end, storeIds, null);
    }

    @Override
    public JsonNode getOrder(long orderId) {
        CallerContext caller = CallerContextHolder.current();
        JsonNode result = orderPort.getOrder(caller, ArgumentChecks.positive(orderId, "orderId"));
        return ResultCaps.capEntities(result);
    }

    @Override
    public JsonNode voidOrder(long orderId, Boolean confirmed) {
        CallerContext caller = CallerContextHolder.current();
        requireConfirmed(confirmed);
        long id = ArgumentChecks.positive(orderId, "orderId");
        orderPort.getOrder(caller, id);
        return ResultCaps.capEntities(orderPort.voidOrder(caller, id));
    }

    private JsonNode cappedReport(ReportType type, String start, String end,
                                  List<Integer> storeIds, Integer limit) {
        CallerContext caller = CallerContextHolder.current();
        LocalDateTime startTime = parseStart(start);
        LocalDateTime endTime = parseEnd(end);
        validateRange(startTime, endTime);
        JsonNode result = orderPort.report(caller, type, startTime, endTime,
                ResultCaps.capStoreIds(storeIds), ResultCaps.boundedLimit(limit));
        return ResultCaps.capArray(result);
    }

    private void requireConfirmed(Boolean confirmed) {
        if (!Boolean.TRUE.equals(confirmed)) {
            throw new IllegalArgumentException("confirmed=true is required");
        }
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start must be before end");
        }
    }

    private int boundedLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private void requireOrderLines(CreateOrderCommand command) {
        ArgumentChecks.required(command, "order");
        ArgumentChecks.requiredList(command.orderDetails(), "orderDetails");
    }

    private LocalDateTime parseStart(String value) {
        return parseDateTime(value, false);
    }

    private LocalDateTime parseEnd(String value) {
        return parseDateTime(value, true);
    }

    private LocalDateTime parseDateTime(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("start and end are required in ISO date-time format");
        }
        String normalized = value.trim();
        try {
            return LocalDateTime.parse(normalized);
        } catch (RuntimeException ignored) {
            LocalDate date = LocalDate.parse(normalized);
            return endOfDay ? date.atTime(23, 59, 59) : date.atStartOfDay();
        }
    }
}
