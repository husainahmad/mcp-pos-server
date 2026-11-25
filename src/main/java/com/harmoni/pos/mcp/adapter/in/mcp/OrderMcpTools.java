package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.OrderUseCase;
import com.harmoni.pos.mcp.domain.CreateOrderCommand;
import com.harmoni.pos.mcp.domain.PayOrderCommand;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMcpTools {

    private final OrderUseCase orders;

    @McpTool(name = "pos_list_orders", description = """
            List the current store's orders for today, newest first.
            Use this to answer questions about recent orders and their status.
            """)
    public String listOrders(
            @McpToolParam(description = "Maximum orders to return, 1-200. Defaults to 50.") Integer limit,
            @McpToolParam(description = "Cursor: return orders older than this order id. Omit for the first page.") Integer beforeId) {
        return ToolExecutor.call(() -> orders.listOrders(limit, beforeId));
    }

    @McpTool(name = "pos_calculate_order", description = """
            Validate and calculate an order without saving it.
            Use this to show the official item prices, discounts and totals before confirmation.
            Prices and discounts are calculated by the order service.
            """)
    public String calculateOrder(
            @McpToolParam(description = "Customer id from pos_search_customers, or omit for walk-in.") Integer customerId,
            @McpToolParam(description = "Dine-in table number, when applicable.") String tableNumber,
            @McpToolParam(description = "Store service type id, when known.") Integer storeServiceTypesId,
            @McpToolParam(description = "Optional order note or special request.") String remark,
            @McpToolParam(description = "Order lines. Each line has productId, skuId, quantity and optional customization groups.") List<CreateOrderCommand.OrderLineCommand> orderDetails) {
        return ToolExecutor.call(() -> orders.calculateOrder(
                order(customerId, tableNumber, storeServiceTypesId, remark, orderDetails)));
    }

    @McpTool(name = "pos_create_confirmed_order", description = """
            Create an order directly in CONFIRMED status.

            Only call this after the customer explicitly confirms the order.
            Use productId and skuId returned by the menu tools. Never invent ids or prices.
            Prices, discounts and totals are calculated by the order service.
            The response contains the created order, including status CONFIRMED.

            Pass a stable idempotencyKey. Reuse the same key when retrying the same order.
            """)
    public String createConfirmedOrder(
            @McpToolParam(description = "Customer id from pos_search_customers, or omit for walk-in.") Integer customerId,
            @McpToolParam(description = "Dine-in table number, when applicable.") String tableNumber,
            @McpToolParam(description = "Store service type id, when known.") Integer storeServiceTypesId,
            @McpToolParam(description = "Optional order note or special request.") String remark,
            @McpToolParam(description = "Order lines. Each line has productId, skuId, quantity and optional customization groups.") List<CreateOrderCommand.OrderLineCommand> orderDetails,
            @McpToolParam(description = "Stable idempotency key for this order attempt.") String idempotencyKey,
            @McpToolParam(description = "Must be true only after the customer explicitly confirmed the order.") boolean confirmed) {
        if (!confirmed) {
            return ToolExecutor.error("Order not confirmed by the customer");
        }
        return ToolExecutor.call(() -> orders.createOrder(
                order(customerId, tableNumber, storeServiceTypesId, remark, orderDetails),
                idempotencyKey));
    }

    @McpTool(name = "pos_pay_order", description = """
            Mark a single order as PAID using an existing payment method id.
            This is not a close-day settlement. Confirm the order id and payment id first.
            """)
    public String payOrder(
            @McpToolParam(description = "Order id from pos_list_orders or pos_create_confirmed_order.") Integer orderId,
            @McpToolParam(description = "Payment method id.") Integer paymentId) {
        return ToolExecutor.call(() -> orders.payOrder(new PayOrderCommand(orderId, paymentId)));
    }

    @McpTool(name = "pos_get_sales_report", description = """
            Get a sales report for a date-time range.
            reportType is one of: settlement, top-products, daily, sales, order-volume.
            Dates use ISO format, for example 2026-11-22T00:00:00.
            """)
    public String salesReport(
            @McpToolParam(description = "Report type: settlement, top-products, daily, sales, or order-volume.") String reportType,
            @McpToolParam(description = "Start date-time in ISO format.") String start,
            @McpToolParam(description = "End date-time in ISO format.") String end,
            @McpToolParam(description = "Optional store ids. Omit for the caller's store.") List<Integer> storeIds,
            @McpToolParam(description = "Result limit for top-products, 1-200.") Integer limit) {
        return ToolExecutor.call(() -> orders.report(reportType, start, end, storeIds, limit));
    }

    @McpTool(name = "pos_get_customer_orders", description = """
            List one customer's order history in a date-time range.
            Returns at most 30 results and inspects at most 5 order lines per order.
            """)
    public String getCustomerOrders(
            @McpToolParam(description = "Customer id from pos_search_customers.") long customerId,
            @McpToolParam(description = "Start date or ISO date-time.") String from,
            @McpToolParam(description = "End date or ISO date-time.") String to) {
        return ToolExecutor.call(() -> orders.getCustomerOrders(customerId, from, to));
    }

    @McpTool(name = "pos_get_best_sellers", description = """
            Get the top-selling products report. Returns at most 30 results.
            """)
    public String getBestSellers(
            @McpToolParam(description = "Start date or ISO date-time.") String start,
            @McpToolParam(description = "End date or ISO date-time.") String end,
            @McpToolParam(description = "Optional store ids, at most 5 are inspected.") List<Integer> storeIds,
            @McpToolParam(description = "Maximum products, capped at 30. Defaults to 20.") Integer limit) {
        return ToolExecutor.call(() -> orders.bestSellers(start, end, storeIds, limit));
    }

    @McpTool(name = "pos_get_sales_totals", description = """
            Get sales totals for a date-time range. Returns at most 30 results.
            """)
    public String getSalesTotals(
            @McpToolParam(description = "Start date or ISO date-time.") String start,
            @McpToolParam(description = "End date or ISO date-time.") String end,
            @McpToolParam(description = "Optional store ids, at most 5 are inspected.") List<Integer> storeIds) {
        return ToolExecutor.call(() -> orders.salesTotals(start, end, storeIds));
    }

    @McpTool(name = "pos_get_daily_sales", description = """
            Get daily sales for a date-time range. Returns at most 30 results.
            """)
    public String getDailySales(
            @McpToolParam(description = "Start date or ISO date-time.") String start,
            @McpToolParam(description = "End date or ISO date-time.") String end,
            @McpToolParam(description = "Optional store ids, at most 5 are inspected.") List<Integer> storeIds) {
        return ToolExecutor.call(() -> orders.dailySales(start, end, storeIds));
    }

    @McpTool(name = "pos_get_settlement", description = """
            Get the settlement report for a date-time range. Returns at most 30 results.
            """)
    public String getSettlement(
            @McpToolParam(description = "Start date or ISO date-time.") String start,
            @McpToolParam(description = "End date or ISO date-time.") String end,
            @McpToolParam(description = "Optional store ids, at most 5 are inspected.") List<Integer> storeIds) {
        return ToolExecutor.call(() -> orders.settlement(start, end, storeIds));
    }

    @McpTool(name = "pos_get_order", description = """
            Get one full order. Result scope: inspect at most 5 entities, including order details and payments.
            """)
    public String getOrder(
            @McpToolParam(description = "Order id from pos_list_orders.") long orderId) {
        return ToolExecutor.call(() -> orders.getOrder(orderId));
    }

    @McpTool(name = "pos_void_order", description = """
            Void an order. The order is read first as a preview.
            Rejected unless confirmed is true. Result scope: inspect at most 5 entities in the result.
            """)
    public String voidOrder(
            @McpToolParam(description = "Order id from pos_get_order.") long orderId,
            @McpToolParam(description = "Must be true only after the customer explicitly confirmed the void.") Boolean confirmed) {
        return ToolExecutor.call(() -> orders.voidOrder(orderId, confirmed));
    }

    private CreateOrderCommand order(Integer customerId, String tableNumber, Integer storeServiceTypesId,
                                     String remark, List<CreateOrderCommand.OrderLineCommand> orderDetails) {
        return new CreateOrderCommand(customerId, tableNumber, storeServiceTypesId, remark, orderDetails);
    }
}
