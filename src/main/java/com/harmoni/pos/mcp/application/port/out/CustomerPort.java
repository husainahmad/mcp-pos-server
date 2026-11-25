package com.harmoni.pos.mcp.application.port.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CustomerCommands;

import java.math.BigDecimal;
import java.time.Instant;

public interface CustomerPort {

    JsonNode searchCustomers(CallerContext caller, String search, Integer brandId, int page, int size);

    JsonNode getCustomer(CallerContext caller, long customerId);

    JsonNode getMetrics(CallerContext caller, long customerId);

    JsonNode getLoyalty(CallerContext caller, long customerId);

    JsonNode getLoyaltyHistory(CallerContext caller, long customerId);

    JsonNode getVouchers(CallerContext caller, long customerId);

    JsonNode getInsights(CallerContext caller, long customerId);

    JsonNode getSalesSummary(CallerContext caller, Instant from, Instant to);

    JsonNode createCustomer(CallerContext caller, CustomerCommands.CreateCustomerCommand command);

    JsonNode updateCustomer(CallerContext caller, long id, CustomerCommands.UpdateCustomerCommand command);

    JsonNode redeemVoucher(CallerContext caller, String code, long orderId);

    JsonNode redeemLoyaltyPoints(CallerContext caller, long id, Integer points);

    JsonNode retentionOverview(CallerContext caller, Integer brandId, Integer storeId,
                               Instant from, Instant to);

    JsonNode retentionSegments(CallerContext caller, Integer brandId, Integer storeId);

    JsonNode retentionCohorts(CallerContext caller, Integer brandId, Instant from, Instant to);

    JsonNode retentionCustomers(CallerContext caller, Integer brandId, Integer storeId,
                                String segment, Integer inactiveDays, BigDecimal minSpend,
                                int page, int size);

    JsonNode listCampaigns(CallerContext caller, Integer brandId, int limit);

    JsonNode campaignPerformance(CallerContext caller, long campaignId);

    JsonNode previewCampaignAudience(CallerContext caller, long campaignId, Integer brandId,
                                     Integer storeId, String segment, Integer inactiveDays,
                                     BigDecimal minSpend, int page, int size);

    JsonNode createRetentionCampaign(CallerContext caller, String code, String name,
                                     Integer brandId, String discountType, BigDecimal discountValue,
                                     Instant startAt, Instant endAt);

    JsonNode issueVouchersBulk(CallerContext caller, long audienceId, boolean confirmed);
}
