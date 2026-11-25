package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.in.CustomerUseCase;
import com.harmoni.pos.mcp.application.port.out.CustomerPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CustomerCommands;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CustomerApplicationService implements CustomerUseCase {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Pattern BRAND_SCOPE = Pattern.compile("BRAND:(\\d+)", Pattern.CASE_INSENSITIVE);

    private final CustomerPort customerPort;

    @Override
    public JsonNode searchCustomers(String search, Integer brandId, int page, int size) {
        CallerContext caller = CallerContextHolder.current();
        return customerPort.searchCustomers(caller, blankToNull(search), brandId,
                Math.max(page, 0), boundedSize(size));
    }

    @Override
    public JsonNode getCustomer(long customerId) {
        return customerPort.getCustomer(CallerContextHolder.current(), positive(customerId));
    }

    @Override
    public JsonNode getMetrics(long customerId) {
        return customerPort.getMetrics(CallerContextHolder.current(), positive(customerId));
    }

    @Override
    public JsonNode getLoyalty(long customerId) {
        return customerPort.getLoyalty(CallerContextHolder.current(), positive(customerId));
    }

    @Override
    public JsonNode getLoyaltyHistory(long customerId) {
        CallerContext caller = CallerContextHolder.current();
        return ResultCaps.capArray(customerPort.getLoyaltyHistory(caller, positive(customerId)));
    }

    @Override
    public JsonNode getVouchers(long customerId) {
        CallerContext caller = CallerContextHolder.current();
        return ResultCaps.capArray(customerPort.getVouchers(caller, positive(customerId)));
    }

    @Override
    public JsonNode getInsights(long customerId) {
        CallerContext caller = CallerContextHolder.current();
        return ResultCaps.capEntities(customerPort.getInsights(caller, positive(customerId)));
    }

    @Override
    public JsonNode getSalesSummary(String from, String to) {
        CallerContext caller = CallerContextHolder.current();
        Instant startTime = parseInstant(from, false);
        Instant endTime = parseInstant(to, true);
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return ResultCaps.capEntities(customerPort.getSalesSummary(caller, startTime, endTime));
    }

    @Override
    public JsonNode createCustomer(CustomerCommands.CreateCustomerCommand command, Boolean confirmed) {
        CallerContext caller = CallerContextHolder.current();
        requireConfirmed(confirmed);
        validateCustomer(caller, command);
        return ResultCaps.capEntities(customerPort.createCustomer(caller, command));
    }

    @Override
    public JsonNode updateCustomer(long id, CustomerCommands.UpdateCustomerCommand command,
                                   Boolean confirmed) {
        CallerContext caller = CallerContextHolder.current();
        requireConfirmed(confirmed);
        validateCustomer(caller, command);
        return ResultCaps.capEntities(customerPort.updateCustomer(caller, positive(id), command));
    }

    @Override
    public JsonNode redeemVoucher(String code, long customerId, long orderId, Boolean confirmed) {
        CallerContext caller = CallerContextHolder.current();
        requireConfirmed(confirmed);
        String voucherCode = ArgumentChecks.required(code, "code");
        long customer = ArgumentChecks.positive(customerId, "customerId");
        long order = ArgumentChecks.positive(orderId, "orderId");
        customerPort.getVouchers(caller, customer);
        return ResultCaps.capEntities(customerPort.redeemVoucher(caller, voucherCode, order));
    }

    @Override
    public JsonNode redeemLoyaltyPoints(long id, Integer points, Boolean confirmed) {
        CallerContext caller = CallerContextHolder.current();
        requireConfirmed(confirmed);
        long loyaltyId = ArgumentChecks.positive(id, "id");
        Integer amount = points == null ? null : ArgumentChecks.positive(points, "points");
        customerPort.getLoyalty(caller, loyaltyId);
        return ResultCaps.capEntities(customerPort.redeemLoyaltyPoints(caller, loyaltyId, amount));
    }

    @Override
    public JsonNode retentionOverview(Integer brandId, Integer storeId, String from, String to) {
        CallerContext caller = CallerContextHolder.current();
        Instant start = optionalInstant(from, false);
        Instant end = optionalInstant(to, true);
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return ResultCaps.capEntities(customerPort.retentionOverview(caller,
                ArgumentChecks.positive(brandId, "brandId"), storeId, start, end));
    }

    @Override
    public JsonNode retentionSegments(Integer brandId, Integer storeId) {
        CallerContext caller = CallerContextHolder.current();
        return ResultCaps.capArray(customerPort.retentionSegments(caller,
                ArgumentChecks.positive(brandId, "brandId"), storeId));
    }

    @Override
    public JsonNode retentionCohorts(Integer brandId, String from, String to) {
        CallerContext caller = CallerContextHolder.current();
        Instant start = optionalInstant(from, false);
        Instant end = optionalInstant(to, true);
        if (start != null && end != null && start.isAfter(end)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return ResultCaps.capArray(customerPort.retentionCohorts(caller,
                ArgumentChecks.positive(brandId, "brandId"), start, end));
    }

    @Override
    public JsonNode atRiskCustomers(Integer brandId, Integer storeId, Integer inactiveDays,
                                    Integer limit, Integer page) {
        return retentionCustomers(brandId, storeId, "AT_RISK", inactiveDays, null, limit, page);
    }

    @Override
    public JsonNode lapsedCustomers(Integer brandId, Integer storeId, Integer limit, Integer page) {
        return retentionCustomers(brandId, storeId, "LAPSED", null, null, limit, page);
    }

    private JsonNode retentionCustomers(Integer brandId, Integer storeId, String segment,
                                        Integer inactiveDays, BigDecimal minSpend,
                                        Integer limit, Integer page) {
        CallerContext caller = CallerContextHolder.current();
        int size = ResultCaps.boundedLimit(limit);
        int zeroPage = page == null ? 0 : Math.max(page, 0);
        Integer window = inactiveDays == null ? null : ArgumentChecks.positive(inactiveDays, "inactiveDays");
        JsonNode result = customerPort.retentionCustomers(caller,
                ArgumentChecks.positive(brandId, "brandId"), storeId, segment, window, minSpend,
                zeroPage, size);
        return ResultCaps.capArray(result);
    }

    @Override
    public JsonNode listCampaigns(Integer brandId, Integer limit) {
        CallerContext caller = CallerContextHolder.current();
        JsonNode result = customerPort.listCampaigns(caller,
                ArgumentChecks.positive(brandId, "brandId"), ResultCaps.boundedLimit(limit));
        return ResultCaps.capArray(result);
    }

    @Override
    public JsonNode campaignPerformance(Long campaignId) {
        CallerContext caller = CallerContextHolder.current();
        return ResultCaps.capEntities(customerPort.campaignPerformance(caller,
                ArgumentChecks.positive(campaignId, "campaignId")));
    }

    @Override
    public JsonNode previewCampaignAudience(Long campaignId, Integer brandId, Integer storeId,
                                            String segment, Integer inactiveDays, BigDecimal minSpend,
                                            Integer limit, Integer page) {
        CallerContext caller = CallerContextHolder.current();
        int size = ResultCaps.boundedLimit(limit);
        int zeroPage = page == null ? 0 : Math.max(page, 0);
        Integer window = inactiveDays == null ? null : ArgumentChecks.positive(inactiveDays, "inactiveDays");
        String normalized = segment == null || segment.isBlank() ? null : segment.trim().toUpperCase();
        JsonNode result = customerPort.previewCampaignAudience(caller,
                ArgumentChecks.positive(campaignId, "campaignId"),
                ArgumentChecks.positive(brandId, "brandId"), storeId, normalized, window, minSpend,
                zeroPage, size);
        return ResultCaps.capEntities(result);
    }

    @Override
    public JsonNode createRetentionCampaign(String code, String name, Integer brandId,
                                            String discountType, BigDecimal discountValue,
                                            String startAt, String endAt, Boolean confirmed) {
        CallerContext caller = CallerContextHolder.current();
        requireConfirmed(confirmed);
        if (discountValue != null && discountValue.signum() < 0) {
            throw new IllegalArgumentException("discountValue must be zero or positive");
        }
        JsonNode result = customerPort.createRetentionCampaign(caller,
                ArgumentChecks.required(code, "code"), ArgumentChecks.required(name, "name"),
                ArgumentChecks.positive(brandId, "brandId"),
                discountType == null || discountType.isBlank() ? null : discountType.trim(),
                discountValue, optionalInstant(startAt, false), optionalInstant(endAt, true));
        return ResultCaps.capEntities(result);
    }

    @Override
    public JsonNode issueVouchersBulk(Long audienceId, Boolean confirmed) {
        CallerContext caller = CallerContextHolder.current();
        requireConfirmed(confirmed);
        return ResultCaps.capEntities(customerPort.issueVouchersBulk(caller,
                ArgumentChecks.positive(audienceId, "audienceId"), true));
    }

    private Instant optionalInstant(String value, boolean endOfDay) {
        return value == null || value.isBlank() ? null : parseInstant(value, endOfDay);
    }

    static Instant parseInstant(String value, boolean endOfDay) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("from and to are required in ISO format");
        }
        String normalized = value.trim();
        try {
            return Instant.parse(normalized);
        } catch (RuntimeException ignored) {
            try {
                return OffsetDateTime.parse(normalized).toInstant();
            } catch (RuntimeException ignoredOffset) {
                try {
                    return LocalDateTime.parse(normalized).atZone(ZoneOffset.UTC).toInstant();
                } catch (RuntimeException ignoredDateTime) {
                    LocalDate date = LocalDate.parse(normalized);
                    return endOfDay ? date.atTime(23, 59, 59).toInstant(ZoneOffset.UTC)
                            : date.atStartOfDay().toInstant(ZoneOffset.UTC);
                }
            }
        }
    }

    private void validateCustomer(CallerContext caller, CustomerCommands.CreateCustomerCommand command) {
        ArgumentChecks.required(command, "customer");
        ArgumentChecks.required(command.name(), "customer.name");
        validateBrandScope(caller, command.brandId());
    }

    private void validateCustomer(CallerContext caller, CustomerCommands.UpdateCustomerCommand command) {
        ArgumentChecks.required(command, "customer");
        ArgumentChecks.required(command.name(), "customer.name");
        validateBrandScope(caller, command.brandId());
    }

    private void validateBrandScope(CallerContext caller, Integer brandId) {
        int id = ArgumentChecks.positive(brandId, "brandId");
        String scopes = caller.scopes();
        if (scopes == null || scopes.isBlank()) {
            return;
        }
        Matcher matcher = BRAND_SCOPE.matcher(scopes);
        boolean hasBrandScope = false;
        boolean allowed = false;
        while (matcher.find()) {
            hasBrandScope = true;
            if (Long.parseLong(matcher.group(1)) == id) {
                allowed = true;
            }
        }
        if (hasBrandScope && !allowed) {
            throw new IllegalArgumentException("brandId is outside caller scope");
        }
    }

    private void requireConfirmed(Boolean confirmed) {
        if (!Boolean.TRUE.equals(confirmed)) {
            throw new IllegalArgumentException("confirmed=true is required");
        }
    }

    private int boundedSize(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private long positive(long value) {
        if (value < 1) {
            throw new IllegalArgumentException("customerId must be positive");
        }
        return value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
