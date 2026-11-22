package com.harmoni.pos.mcp.domain;

public record PayOrderCommand(Integer orderId, Integer paymentId) {
}
