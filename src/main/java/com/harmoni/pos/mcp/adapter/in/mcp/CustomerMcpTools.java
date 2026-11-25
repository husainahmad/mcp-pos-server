package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.CustomerUseCase;
import com.harmoni.pos.mcp.domain.CustomerCommands;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class CustomerMcpTools {

    private final CustomerUseCase customers;

    @McpTool(name = "pos_search_customers", description = """
            Search customers by name, phone or email.
            Use the returned customer id for order creation; do not invent customer ids.
            """)
    public String searchCustomers(
            @McpToolParam(description = "Optional name, phone or email search.") String search,
            @McpToolParam(description = "Optional brand id filter.") Integer brandId,
            @McpToolParam(description = "Zero-based page number.") int page,
            @McpToolParam(description = "Page size, 1-100.") int size) {
        return ToolExecutor.call(() -> customers.searchCustomers(search, brandId, page, size));
    }

    @McpTool(name = "pos_get_customer", description = "Get a customer by id.")
    public String getCustomer(@McpToolParam(description = "Customer id.") long customerId) {
        return ToolExecutor.call(() -> customers.getCustomer(customerId));
    }

    @McpTool(name = "pos_get_customer_metrics", description = """
            Get customer spend, visit count, average order value and segment.
            """)
    public String getCustomerMetrics(@McpToolParam(description = "Customer id.") long customerId) {
        return ToolExecutor.call(() -> customers.getMetrics(customerId));
    }

    @McpTool(name = "pos_get_customer_loyalty", description = "Get a customer's loyalty account and points.")
    public String getCustomerLoyalty(@McpToolParam(description = "Customer id.") long customerId) {
        return ToolExecutor.call(() -> customers.getLoyalty(customerId));
    }

    @McpTool(name = "pos_get_customer_loyalty_history", description = """
            List a customer's loyalty point history. Returns at most 30 results.
            """)
    public String getCustomerLoyaltyHistory(
            @McpToolParam(description = "Customer id from pos_search_customers.") long customerId) {
        return ToolExecutor.call(() -> customers.getLoyaltyHistory(customerId));
    }

    @McpTool(name = "pos_get_customer_vouchers", description = """
            List vouchers owned by a customer. Returns at most 30 results.
            """)
    public String getCustomerVouchers(
            @McpToolParam(description = "Customer id from pos_search_customers.") long customerId) {
        return ToolExecutor.call(() -> customers.getVouchers(customerId));
    }

    @McpTool(name = "pos_get_customer_insights", description = """
            Read customer insights. Result scope: inspect at most 5 entities.
            """)
    public String getCustomerInsights(
            @McpToolParam(description = "Customer id from pos_search_customers.") long customerId) {
        return ToolExecutor.call(() -> customers.getInsights(customerId));
    }

    @McpTool(name = "pos_get_sales_summary", description = """
            Read sales analytics for a date-time range. Result scope: inspect at most 5 entities.
            """)
    public String getSalesSummary(
            @McpToolParam(description = "Start date or ISO date-time.") String from,
            @McpToolParam(description = "End date or ISO date-time.") String to) {
        return ToolExecutor.call(() -> customers.getSalesSummary(from, to));
    }

    @McpTool(name = "pos_create_customer", description = """
            Create a customer in the caller's brand scope.
            Rejected unless confirmed is true. Result scope: inspect at most 5 entities in the result.
            """)
    public String createCustomer(
            @McpToolParam(description = "Brand id from pos_list_brands, checked against caller scope.") Integer brandId,
            @McpToolParam(description = "Customer name.") String name,
            @McpToolParam(description = "Optional phone number.") String phone,
            @McpToolParam(description = "Optional email address.") String email,
            @McpToolParam(description = "Must be true only after the customer explicitly confirmed creation.") Boolean confirmed) {
        return ToolExecutor.call(() -> customers.createCustomer(
                new CustomerCommands.CreateCustomerCommand(brandId, name, phone, email), confirmed));
    }

    @McpTool(name = "pos_update_customer", description = """
            Update an existing customer. Brand is checked against caller scope.
            Rejected unless confirmed is true. Result scope: inspect at most 5 entities in the result.
            """)
    public String updateCustomer(
            @McpToolParam(description = "Customer id from pos_get_customer.") long id,
            @McpToolParam(description = "Brand id from pos_list_brands, checked against caller scope.") Integer brandId,
            @McpToolParam(description = "Customer name.") String name,
            @McpToolParam(description = "Optional phone number.") String phone,
            @McpToolParam(description = "Optional email address.") String email,
            @McpToolParam(description = "Must be true only after the customer explicitly confirmed the update.") Boolean confirmed) {
        return ToolExecutor.call(() -> customers.updateCustomer(id,
                new CustomerCommands.UpdateCustomerCommand(brandId, name, phone, email), confirmed));
    }

    @McpTool(name = "pos_redeem_voucher", description = """
            Redeem one voucher after previewing the customer's vouchers.
            Requires an order id and confirmed=true. Result scope: inspect at most 5 entities in the result.
            """)
    public String redeemVoucher(
            @McpToolParam(description = "Voucher code from pos_get_customer_vouchers.") String code,
            @McpToolParam(description = "Customer id used for the voucher preview.") long customerId,
            @McpToolParam(description = "Order id from pos_get_order.") long orderId,
            @McpToolParam(description = "Must be true only after the customer explicitly confirmed redemption.") Boolean confirmed) {
        return ToolExecutor.call(() -> customers.redeemVoucher(code, customerId, orderId, confirmed));
    }

    @McpTool(name = "pos_redeem_loyalty_points", description = """
            Redeem loyalty points after previewing the loyalty account.
            Rejected unless confirmed is true. Result scope: inspect at most 5 entities in the result.
            """)
    public String redeemLoyaltyPoints(
            @McpToolParam(description = "Loyalty account id from pos_get_customer_loyalty.") long id,
            @McpToolParam(description = "Optional points to redeem. Omit to use the downstream default.") Integer points,
            @McpToolParam(description = "Must be true only after the customer explicitly confirmed redemption.") Boolean confirmed) {
        return ToolExecutor.call(() -> customers.redeemLoyaltyPoints(id, points, confirmed));
    }

    @McpTool(name = "pos_get_retention_overview", description = """
            Retention KPIs for one brand (optionally one store): total/new/returning,
            at-risk, lapsed, repeat rate, revenue at risk and reactivation rate.
            Result scope: inspect at most 5 entities.
            """)
    public String retentionOverview(
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Optional store id from pos_list_stores.") Integer storeId,
            @McpToolParam(description = "Optional start date or ISO date-time.") String from,
            @McpToolParam(description = "Optional end date or ISO date-time.") String to) {
        return ToolExecutor.call(() -> customers.retentionOverview(brandId, storeId, from, to));
    }

    @McpTool(name = "pos_list_retention_segments", description = """
            Retention segment distribution for one brand (optionally one store).
            Returns at most 30 results.
            """)
    public String retentionSegments(
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Optional store id from pos_list_stores.") Integer storeId) {
        return ToolExecutor.call(() -> customers.retentionSegments(brandId, storeId));
    }

    @McpTool(name = "pos_list_retention_cohorts", description = """
            Monthly franchise cohorts with 30-day repeat retention.
            Returns at most 30 results.
            """)
    public String retentionCohorts(
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Optional start date or ISO date-time.") String from,
            @McpToolParam(description = "Optional end date or ISO date-time.") String to) {
        return ToolExecutor.call(() -> customers.retentionCohorts(brandId, from, to));
    }

    @McpTool(name = "pos_list_at_risk_customers", description = """
            List at-risk customers (no phone or email) for a brand. Read-only.
            Use pos_preview_campaign_audience before issuing any voucher.
            Returns at most 30 results.
            """)
    public String atRiskCustomers(
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Optional store id from pos_list_stores.") Integer storeId,
            @McpToolParam(description = "Optional days since last visit threshold.") Integer inactiveDays,
            @McpToolParam(description = "Result limit, capped at 30.") Integer limit,
            @McpToolParam(description = "Zero-based page number.") Integer page) {
        return ToolExecutor.call(() -> customers.atRiskCustomers(brandId, storeId, inactiveDays,
                limit, page));
    }

    @McpTool(name = "pos_list_lapsed_customers", description = """
            List lapsed customers (no phone or email) for a brand. Read-only.
            Returns at most 30 results.
            """)
    public String lapsedCustomers(
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Optional store id from pos_list_stores.") Integer storeId,
            @McpToolParam(description = "Result limit, capped at 30.") Integer limit,
            @McpToolParam(description = "Zero-based page number.") Integer page) {
        return ToolExecutor.call(() -> customers.lapsedCustomers(brandId, storeId, limit, page));
    }

    @McpTool(name = "pos_list_campaigns", description = """
            List campaigns for a brand. Returns at most 30 results.
            """)
    public String listCampaigns(
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Result limit, capped at 30.") Integer limit) {
        return ToolExecutor.call(() -> customers.listCampaigns(brandId, limit));
    }

    @McpTool(name = "pos_get_campaign_performance", description = """
            Campaign performance: issued, redeemed, redemption rate, revenue and
            reactivated customers. Result scope: inspect at most 5 entities.
            """)
    public String campaignPerformance(
            @McpToolParam(description = "Campaign id from pos_list_campaigns.") Long campaignId) {
        return ToolExecutor.call(() -> customers.campaignPerformance(campaignId));
    }

    @McpTool(name = "pos_preview_campaign_audience", description = """
            Preview a retention audience for a campaign without issuing anything.
            Returns audience size, expected redemptions and discount exposure.
            Result scope: inspect at most 5 entities.
            """)
    public String previewCampaignAudience(
            @McpToolParam(description = "Campaign id from pos_list_campaigns.") Long campaignId,
            @McpToolParam(description = "Brand id from pos_list_brands, checked against the campaign.") Integer brandId,
            @McpToolParam(description = "Optional store id from pos_list_stores.") Integer storeId,
            @McpToolParam(description = "Optional segment: AT_RISK, LAPSED, ONE_TIME or FREQUENT.") String segment,
            @McpToolParam(description = "Optional days since last visit threshold.") Integer inactiveDays,
            @McpToolParam(description = "Optional minimum lifetime spend.") BigDecimal minSpend,
            @McpToolParam(description = "Result limit, capped at 30.") Integer limit,
            @McpToolParam(description = "Zero-based page number.") Integer page) {
        return ToolExecutor.call(() -> customers.previewCampaignAudience(campaignId, brandId,
                storeId, segment, inactiveDays, minSpend, limit, page));
    }

    @McpTool(name = "pos_create_retention_campaign", description = """
            Create a brand-scoped retention campaign in DRAFT status.
            Rejected unless confirmed is true. Result scope: inspect at most 5 entities in the result.
            """)
    public String createRetentionCampaign(
            @McpToolParam(description = "Unique campaign code.") String code,
            @McpToolParam(description = "Campaign name.") String name,
            @McpToolParam(description = "Brand id from pos_list_brands.") Integer brandId,
            @McpToolParam(description = "Optional discount type, e.g. FIXED or PERCENT.") String discountType,
            @McpToolParam(description = "Optional discount value per redemption.") BigDecimal discountValue,
            @McpToolParam(description = "Optional start date or ISO date-time.") String startAt,
            @McpToolParam(description = "Optional end date or ISO date-time.") String endAt,
            @McpToolParam(description = "Must be true only after the owner explicitly confirmed creation.") Boolean confirmed) {
        return ToolExecutor.call(() -> customers.createRetentionCampaign(code, name, brandId,
                discountType, discountValue, startAt, endAt, confirmed));
    }

    @McpTool(name = "pos_issue_vouchers_bulk", description = """
            Issue vouchers for an approved audience preview. Rejected unless confirmed is true.
            Never call this without the exact preview and explicit owner approval.
            Result scope: inspect at most 5 entities in the result.
            """)
    public String issueVouchersBulk(
            @McpToolParam(description = "Audience id from pos_preview_campaign_audience.") Long audienceId,
            @McpToolParam(description = "Must be true only after the owner explicitly approved the audience.") Boolean confirmed) {
        return ToolExecutor.call(() -> customers.issueVouchersBulk(audienceId, confirmed));
    }
}
