package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.mcp.application.port.out.CatalogPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CatalogCommands;
import com.harmoni.pos.mcp.domain.ValidateOrderItemsCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogApplicationServiceTest {

    @Mock
    private CatalogPort catalogPort;

    @InjectMocks
    private CatalogApplicationService service;

    @BeforeEach
    void setUp() {
        CallerContextHolder.set(new CallerContext("Kasir1", "", "menu:edit", "BRAND:1"));
    }

    @AfterEach
    void tearDown() {
        CallerContextHolder.clear();
    }

    @Test
    void listTiersUsesBrandId() {
        when(catalogPort.listTiers(any(), eq(7))).thenReturn(new ObjectMapper().createObjectNode());

        service.listTiers(7);

        verify(catalogPort).listTiers(any(), eq(7));
    }

    @Test
    void createProductRequiresAtLeastOneSku() {
        CatalogCommands.ProductCommand command = new CatalogCommands.ProductCommand(
                null, "Caramel Latte", 1, null, null, List.of(), null);

        assertThatThrownBy(() -> service.createProduct(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("product.skus");
    }

    @Test
    void createProductRequiresTierPrice() {
        CatalogCommands.SkuCommand sku = new CatalogCommands.SkuCommand(
                null, "Regular", null, List.of());
        CatalogCommands.ProductCommand command = new CatalogCommands.ProductCommand(
                null, "Caramel Latte", 1, null, null, List.of(sku), null);

        assertThatThrownBy(() -> service.createProduct(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sku.tierPrices");
    }

    @Test
    void createProductAcceptsCompleteSkuConfiguration() {
        CatalogCommands.TierPriceCommand price =
                new CatalogCommands.TierPriceCommand(null, 2, new BigDecimal("20000"));
        CatalogCommands.SkuCommand sku = new CatalogCommands.SkuCommand(
                null, "Regular", null, List.of(price));
        CatalogCommands.ProductCommand command = new CatalogCommands.ProductCommand(
                null, "Caramel Latte", 1, null, null, List.of(sku), List.of(3));
        when(catalogPort.createProduct(any(), any())).thenReturn(new ObjectMapper().createObjectNode());

        service.createProduct(command);

        verify(catalogPort).createProduct(any(), eq(command));
    }

    @Test
    void validateOrderItemsRequiresStore() {
        ValidateOrderItemsCommand command = new ValidateOrderItemsCommand(
                null, List.of(new ValidateOrderItemsCommand.Item(1, 2, 1, List.of())));

        assertThatThrownBy(() -> service.validateOrderItems(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("storeId");
    }
}
