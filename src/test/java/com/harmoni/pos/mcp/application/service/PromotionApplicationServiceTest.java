package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.mcp.application.port.out.PromotionPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.PromotionCommands;
import com.harmoni.pos.mcp.domain.PromotionPriceCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionApplicationServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private PromotionPort promotionPort;

    @InjectMocks
    private PromotionApplicationService service;

    @BeforeEach
    void setUp() {
        CallerContextHolder.set(new CallerContext("Kasir1", "", "menu:edit", "BRAND:1"));
    }

    @AfterEach
    void tearDown() {
        CallerContextHolder.clear();
    }

    @Test
    void createPromotionRequiresBrand() {
        PromotionCommands.PromotionCommand command = new PromotionCommands.PromotionCommand(
                null, null, "PROMO", "Promo", null, "PERCENTAGE", "DRAFT", 0, null, null,
                null, null, null, null, null);

        assertThatThrownBy(() -> service.createPromotion(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("promotion.brandId");
    }

    @Test
    void setStatusUppercasesValue() {
        when(promotionPort.setPromotionStatus(any(), eq(5L), eq("ACTIVE")))
                .thenReturn(new ObjectMapper().createObjectNode());

        service.setPromotionStatus(5L, "active");

        verify(promotionPort).setPromotionStatus(any(), eq(5L), eq("ACTIVE"));
    }

    @Test
    void calculatePromotionPriceRejectsEmptyLines() {
        PromotionPriceCommand command = new PromotionPriceCommand(
                List.of(), new PromotionPriceCommand.Context(1L, null, null, null, null));

        assertThatThrownBy(() -> service.calculatePromotionPrice(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lines");
    }

    @Test
    void getPromotionRequiresPositiveId() {
        assertThatThrownBy(() -> service.getPromotion(0L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("id");
    }

    @Test
    void getPromotionCapsChildren() throws Exception {
        when(promotionPort.getPromotion(any(), eq(5L), isNull()))
                .thenReturn(MAPPER.readTree("{\"schedules\":" + ids(9)
                        + ",\"rules\":" + ids(9) + "}"));

        JsonNode result = service.getPromotion(5L, null);

        assertThat(result.get("schedules")).hasSize(5);
        assertThat(result.get("rules")).hasSize(5);
        verify(promotionPort).getPromotion(any(), eq(5L), isNull());
    }

    @Test
    void listRedeemablePromotionsCapsResults() throws Exception {
        when(promotionPort.listRedeemablePromotions(any())).thenReturn(MAPPER.readTree(ids(35)));

        JsonNode result = service.listRedeemablePromotions();

        assertThat(result).hasSize(30);
    }

    @Test
    void findPromotionsForCatalogItemRequiresPositiveIds() {
        assertThatThrownBy(() -> service.findPromotionsForCatalogItem(1, 0, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("skuId");
    }

    @Test
    void findPromotionsForCatalogItemCapsResults() throws Exception {
        when(promotionPort.findPromotionsForCatalogItem(any(), eq(1), eq(2), eq(3)))
                .thenReturn(MAPPER.readTree(ids(35)));

        JsonNode result = service.findPromotionsForCatalogItem(1, 2, 3);

        assertThat(result).hasSize(30);
        verify(promotionPort).findPromotionsForCatalogItem(any(), eq(1), eq(2), eq(3));
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
