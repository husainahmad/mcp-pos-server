package com.harmoni.pos.mcp.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CustomerCommands;

import java.math.BigDecimal;

public interface CustomerUseCase {

    JsonNode searchCustomers(String search, Integer brandId, int page, int size);

    JsonNode getCustomer(long customerId);

    JsonNode getMetrics(long customerId);

    JsonNode getLoyalty(long customerId);

    JsonNode getLoyaltyHistory(long customerId);

    JsonNode getVouchers(long customerId);

    JsonNode getInsights(long customerId);

    JsonNode getSalesSummary(String from, String to);

    JsonNode createCustomer(CustomerCommands.CreateCustomerCommand command, Boolean confirmed);

    JsonNode updateCustomer(long id, CustomerCommands.UpdateCustomerCommand command, Boolean confirmed);

    JsonNode redeemVoucher(String code, long customerId, long orderId, Boolean confirmed);

    JsonNode redeemLoyaltyPoints(long id, Integer points, Boolean confirmed);

    JsonNode retentionOverview(Integer brandId, Integer storeId, String from, String to);

    JsonNode retentionSegments(Integer brandId, Integer storeId);

    JsonNode retentionCohorts(Integer brandId, String from, String to);

    JsonNode atRiskCustomers(Integer brandId, Integer storeId, Integer inactiveDays,
                             Integer limit, Integer page);

    JsonNode lapsedCustomers(Integer brandId, Integer storeId, Integer limit, Integer page);

    JsonNode listCampaigns(Integer brandId, Integer limit);

    JsonNode campaignPerformance(Long campaignId);

    JsonNode previewCampaignAudience(Long campaignId, Integer brandId, Integer storeId,
                                     String segment, Integer inactiveDays, BigDecimal minSpend,
                                     Integer limit, Integer page);

    JsonNode createRetentionCampaign(String code, String name, Integer brandId,
                                     String discountType, BigDecimal discountValue,
                                     String startAt, String endAt, Boolean confirmed);

    JsonNode issueVouchersBulk(Long audienceId, Boolean confirmed);
}
