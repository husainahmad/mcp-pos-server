package com.harmoni.pos.mcp.application.service;

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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromotionApplicationServiceTest {

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
}
