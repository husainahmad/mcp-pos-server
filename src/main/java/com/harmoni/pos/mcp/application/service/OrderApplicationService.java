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
