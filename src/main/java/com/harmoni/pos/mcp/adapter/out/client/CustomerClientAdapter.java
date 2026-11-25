package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.out.CustomerPort;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CustomerCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomerClientAdapter implements CustomerPort {

    @Qualifier("customerRestClient")
    private final RestClient customerRestClient;

    private final JsonRestClient http;

    @Override
    public JsonNode searchCustomers(CallerContext caller, String search, Integer brandId, int page, int size) {
        Map<String, Object> query = new LinkedHashMap<>();
        if (search != null && !search.isBlank()) {
            query.put("search", search);
        }
        if (brandId != null) {
            query.put("brandId", brandId);
        }
        query.put("page", page);
        query.put("size", size);
        return JsonData.unwrap(http.get(customerRestClient, "/api/v1/customers", caller, query));
    }

    @Override
    public JsonNode getCustomer(CallerContext caller, long customerId) {
        return JsonData.unwrap(http.get(customerRestClient, "/api/v1/customers/" + customerId, caller, Map.of()));
    }

    @Override
    public JsonNode getMetrics(CallerContext caller, long customerId) {
        return JsonData.unwrap(http.get(customerRestClient,
                "/api/v1/growth/customers/" + customerId + "/metrics", caller, Map.of()));
    }

    @Override
    public JsonNode getLoyalty(CallerContext caller, long customerId) {
        return JsonData.unwrap(http.get(customerRestClient,
                "/api/v1/growth/loyalty/" + customerId, caller, Map.of()));
    }

    @Override
    public JsonNode getLoyaltyHistory(CallerContext caller, long customerId) {
        return http.get(customerRestClient,
                "/api/v1/growth/loyalty/" + customerId + "/history", caller, Map.of());
    }

    @Override
    public JsonNode getVouchers(CallerContext caller, long customerId) {
        return http.get(customerRestClient, "/api/v1/campaigns/vouchers", caller,
                Map.of("customerId", customerId));
    }

    @Override
    public JsonNode getInsights(CallerContext caller, long customerId) {
        return http.get(customerRestClient,
                "/api/v1/analytics/customers/" + customerId + "/insights", caller, Map.of());
    }

    @Override
    public JsonNode getSalesSummary(CallerContext caller, Instant from, Instant to) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("from", from.toString());
        query.put("to", to.toString());
        return http.get(customerRestClient, "/api/v1/analytics/sales", caller, query);
    }

    @Override
    public JsonNode createCustomer(CallerContext caller, CustomerCommands.CreateCustomerCommand command) {
        return http.post(customerRestClient, "/api/v1/customers", caller, command);
    }

    @Override
    public JsonNode updateCustomer(CallerContext caller, long id,
                                   CustomerCommands.UpdateCustomerCommand command) {
        return http.put(customerRestClient, "/api/v1/customers/" + id, caller, command);
    }

    @Override
    public JsonNode redeemVoucher(CallerContext caller, String code, long orderId) {
        String encoded = UriUtils.encodePathSegment(code, StandardCharsets.UTF_8);
        return http.post(customerRestClient, "/api/v1/campaigns/vouchers/" + encoded + "/redeem",
                caller, Map.of("orderId", orderId));
    }

    @Override
    public JsonNode redeemLoyaltyPoints(CallerContext caller, long id, Integer points) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (points != null) {
            body.put("points", points);
        }
        return http.post(customerRestClient, "/api/v1/growth/loyalty/" + id + "/redeem", caller, body);
    }

    @Override
    public JsonNode retentionOverview(CallerContext caller, Integer brandId, Integer storeId,
                                      Instant from, Instant to) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("brandId", brandId);
        if (storeId != null) {
            query.put("storeId", storeId);
        }
        if (from != null) {
            query.put("from", from.toString());
        }
        if (to != null) {
            query.put("to", to.toString());
        }
        return http.get(customerRestClient, "/api/v1/growth/retention/overview", caller, query);
    }

    @Override
    public JsonNode retentionSegments(CallerContext caller, Integer brandId, Integer storeId) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("brandId", brandId);
        if (storeId != null) {
            query.put("storeId", storeId);
        }
        return http.get(customerRestClient, "/api/v1/growth/retention/segments", caller, query);
    }

    @Override
    public JsonNode retentionCohorts(CallerContext caller, Integer brandId, Instant from, Instant to) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("brandId", brandId);
        if (from != null) {
            query.put("from", from.toString());
        }
        if (to != null) {
            query.put("to", to.toString());
        }
        return http.get(customerRestClient, "/api/v1/growth/retention/cohorts", caller, query);
    }

    @Override
    public JsonNode retentionCustomers(CallerContext caller, Integer brandId, Integer storeId,
                                       String segment, Integer inactiveDays, BigDecimal minSpend,
                                       int page, int size) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("brandId", brandId);
        if (storeId != null) {
            query.put("storeId", storeId);
        }
        if (segment != null) {
            query.put("segment", segment);
        }
        if (inactiveDays != null) {
            query.put("inactiveDays", inactiveDays);
        }
        if (minSpend != null) {
            query.put("minSpend", minSpend);
        }
        query.put("page", page);
        query.put("size", size);
        query.put("optInOnly", false);
        return http.get(customerRestClient, "/api/v1/growth/retention/customers", caller, query);
    }

    @Override
    public JsonNode listCampaigns(CallerContext caller, Integer brandId, int limit) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("brandId", brandId);
        query.put("limit", limit);
        return http.get(customerRestClient, "/api/v1/campaigns", caller, query);
    }

    @Override
    public JsonNode campaignPerformance(CallerContext caller, long campaignId) {
        return http.get(customerRestClient, "/api/v1/campaigns/" + campaignId + "/performance",
                caller, Map.of());
    }

    @Override
    public JsonNode previewCampaignAudience(CallerContext caller, long campaignId,
                                            Integer brandId, Integer storeId, String segment,
                                            Integer inactiveDays, BigDecimal minSpend,
                                            int page, int size) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("campaignId", campaignId);
        body.put("brandId", brandId);
        body.put("storeId", storeId);
        body.put("segment", segment);
        body.put("inactiveDays", inactiveDays);
        body.put("minSpend", minSpend);
        body.put("page", page);
        body.put("size", size);
        return http.post(customerRestClient, "/api/v1/campaigns/audience/preview", caller, body);
    }

    @Override
    public JsonNode createRetentionCampaign(CallerContext caller, String code, String name,
                                            Integer brandId, String discountType,
                                            BigDecimal discountValue, Instant startAt, Instant endAt) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("name", name);
        body.put("type", "VOUCHER");
        body.put("brandId", brandId);
        body.put("discountType", discountType);
        body.put("discountValue", discountValue);
        body.put("startAt", startAt == null ? null : startAt.toString());
        body.put("endAt", endAt == null ? null : endAt.toString());
        return http.post(customerRestClient, "/api/v1/campaigns/retention", caller, body);
    }

    @Override
    public JsonNode issueVouchersBulk(CallerContext caller, long audienceId, boolean confirmed) {
        return http.post(customerRestClient,
                "/api/v1/campaigns/audience/" + audienceId + "/issue", caller,
                Map.of("confirmed", confirmed));
    }
}
