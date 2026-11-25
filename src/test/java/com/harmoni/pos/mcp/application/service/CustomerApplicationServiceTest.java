package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.mcp.application.port.out.CustomerPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CustomerCommands;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerApplicationServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private CustomerPort customerPort;

    @InjectMocks
    private CustomerApplicationService service;

    @BeforeEach
    void setUp() {
        CallerContextHolder.set(new CallerContext("Kasir1", "", "customer:read", "BRAND:1"));
    }

    @AfterEach
    void tearDown() {
        CallerContextHolder.clear();
    }

    @Test
    void searchClampsPageAndSize() {
        when(customerPort.searchCustomers(any(), isNull(), isNull(), eq(0), eq(100)))
                .thenReturn(new ObjectMapper().createObjectNode());

        service.searchCustomers(" ", null, -5, 500);

        verify(customerPort).searchCustomers(any(), isNull(), isNull(), eq(0), eq(100));
    }

    @Test
    void loyaltyHistoryAndVouchersCapResults() throws Exception {
        when(customerPort.getLoyaltyHistory(any(), eq(5L))).thenReturn(MAPPER.readTree(ids(35)));
        when(customerPort.getVouchers(any(), eq(5L))).thenReturn(MAPPER.readTree(ids(35)));

        assertThat(service.getLoyaltyHistory(5L)).hasSize(30);
        assertThat(service.getVouchers(5L)).hasSize(30);
    }

    @Test
    void readToolsRequirePositiveCustomerId() {
        assertThatThrownBy(() -> service.getInsights(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customerId");
        verifyNoInteractions(customerPort);
    }

    @Test
    void getInsightsCapsNestedEntities() throws Exception {
        when(customerPort.getInsights(any(), eq(5L)))
                .thenReturn(MAPPER.readTree("{\"segments\":" + ids(9)
                        + ",\"details\":[{\"events\":" + ids(9) + "}]}"));

        JsonNode result = service.getInsights(5L);

        assertThat(result.get("segments")).hasSize(5);
        assertThat(result.get("details").get(0).get("events")).hasSize(5);
    }

    @Test
    void getSalesSummaryParsesInstantsAndRejectsReversedBounds() throws Exception {
        Instant from = Instant.parse("2026-11-22T00:00:00Z");
        Instant to = Instant.parse("2026-11-22T23:59:59Z");
        when(customerPort.getSalesSummary(any(), eq(from), eq(to)))
                .thenReturn(MAPPER.readTree("{\"rows\":" + ids(9) + "}"));

        JsonNode result = service.getSalesSummary("2026-11-22", "2026-11-22");

        assertThat(result.get("rows")).hasSize(5);
        verify(customerPort).getSalesSummary(any(), eq(from), eq(to));
        assertThatThrownBy(() -> service.getSalesSummary("2026-11-23", "2026-11-22"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("from");
    }

    @Test
    void createCustomerRejectsUnconfirmedBeforeDownstream() {
        assertThatThrownBy(() -> service.createCustomer(
                new CustomerCommands.CreateCustomerCommand(1, "Ana", null, null), Boolean.FALSE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirmed");

        verifyNoInteractions(customerPort);
    }

    @Test
    void createCustomerValidatesBrandScope() {
        assertThatThrownBy(() -> service.createCustomer(
                new CustomerCommands.CreateCustomerCommand(2, "Ana", null, null), Boolean.TRUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scope");

        verifyNoInteractions(customerPort);
    }

    @Test
    void createCustomerCapsResult() throws Exception {
        when(customerPort.createCustomer(any(), any()))
                .thenReturn(MAPPER.readTree("{\"links\":" + ids(9) + "}"));

        JsonNode result = service.createCustomer(
                new CustomerCommands.CreateCustomerCommand(1, "Ana", null, null), Boolean.TRUE);

        assertThat(result.get("links")).hasSize(5);
        verify(customerPort).createCustomer(any(), any());
    }

    @Test
    void updateCustomerRejectsUnconfirmedBeforeDownstream() {
        assertThatThrownBy(() -> service.updateCustomer(1L,
                new CustomerCommands.UpdateCustomerCommand(1, "Ana", null, null), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirmed");

        verifyNoInteractions(customerPort);
    }

    @Test
    void redeemVoucherRejectsUnconfirmedBeforePreview() {
        assertThatThrownBy(() -> service.redeemVoucher("V1", 3L, 4L, Boolean.FALSE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirmed");

        verifyNoInteractions(customerPort);
    }

    @Test
    void redeemVoucherRequiresOrderId() {
        assertThatThrownBy(() -> service.redeemVoucher("V1", 3L, 0L, Boolean.TRUE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderId");

        verifyNoInteractions(customerPort);
    }

    @Test
    void redeemVoucherPreviewsBeforeWrite() throws Exception {
        when(customerPort.getVouchers(any(), eq(3L))).thenReturn(MAPPER.readTree(ids(3)));
        when(customerPort.redeemVoucher(any(), eq("V1"), eq(4L)))
                .thenReturn(MAPPER.createObjectNode());

        service.redeemVoucher("V1", 3L, 4L, Boolean.TRUE);

        InOrder order = inOrder(customerPort);
        order.verify(customerPort).getVouchers(any(), eq(3L));
        order.verify(customerPort).redeemVoucher(any(), eq("V1"), eq(4L));
    }

    @Test
    void redeemLoyaltyPointsRejectsUnconfirmedBeforePreview() {
        assertThatThrownBy(() -> service.redeemLoyaltyPoints(6L, 10, Boolean.FALSE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirmed");

        verifyNoInteractions(customerPort);
    }

    @Test
    void redeemLoyaltyPointsPreviewsBeforeWrite() {
        when(customerPort.getLoyalty(any(), eq(6L))).thenReturn(MAPPER.createObjectNode());
        when(customerPort.redeemLoyaltyPoints(any(), eq(6L), eq(10)))
                .thenReturn(MAPPER.createObjectNode());

        service.redeemLoyaltyPoints(6L, 10, Boolean.TRUE);

        InOrder order = inOrder(customerPort);
        order.verify(customerPort).getLoyalty(any(), eq(6L));
        order.verify(customerPort).redeemLoyaltyPoints(any(), eq(6L), eq(10));
    }


    @Test
    void atRiskCustomersRequireBrandAndCapResults() throws Exception {
        when(customerPort.retentionCustomers(any(), eq(1), isNull(), eq("AT_RISK"),
                isNull(), isNull(), eq(0), eq(30))).thenReturn(MAPPER.readTree(ids(35)));

        assertThat(service.atRiskCustomers(1, null, null, 30, null)).hasSize(30);
        verify(customerPort).retentionCustomers(any(), eq(1), isNull(), eq("AT_RISK"),
                isNull(), isNull(), eq(0), eq(30));

        assertThatThrownBy(() -> service.atRiskCustomers(null, null, null, 30, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("brandId");
    }

    @Test
    void previewCampaignAudienceCapsEntities() throws Exception {
        when(customerPort.previewCampaignAudience(any(), eq(9L), eq(1), isNull(),
                eq("AT_RISK"), isNull(), isNull(), eq(0), eq(30)))
                .thenReturn(MAPPER.readTree("{\"data\":" + ids(9) + "}"));

        JsonNode result = service.previewCampaignAudience(9L, 1, null, "at_risk", null, null, 30, null);

        assertThat(result.get("data")).hasSize(5);
    }

    @Test
    void createRetentionCampaignRejectsUnconfirmedBeforeDownstream() {
        assertThatThrownBy(() -> service.createRetentionCampaign("RET", "Retention", 1,
                "FIXED", new BigDecimal("10000"), null, null, Boolean.FALSE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirmed");
        verifyNoInteractions(customerPort);
    }

    @Test
    void issueVouchersBulkRejectsUnconfirmedBeforeDownstream() {
        assertThatThrownBy(() -> service.issueVouchersBulk(3L, Boolean.FALSE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirmed");
        verifyNoInteractions(customerPort);
    }

    private String ids(int size) {
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < size; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append("{\"id\":").append(index).append("}");
        }
        return builder.append(']').toString();
    }
}
