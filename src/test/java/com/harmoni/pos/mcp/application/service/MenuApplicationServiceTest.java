package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.mcp.application.port.out.MenuPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuApplicationServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private MenuPort menuPort;

    @InjectMocks
    private MenuApplicationService service;

    @BeforeEach
    void setUp() {
        CallerContextHolder.set(new CallerContext("Kasir1", "", "menu:view", "BRAND:1"));
    }

    @AfterEach
    void tearDown() {
        CallerContextHolder.clear();
    }

    @Test
    void searchProductsTrimsName() {
        when(menuPort.searchProducts(any(), eq("latte"))).thenReturn(MAPPER.createObjectNode());

        service.searchProducts("  latte  ");

        verify(menuPort).searchProducts(any(), eq("latte"));
    }

    @Test
    void promotionsClampPageAndSize() {
        when(menuPort.listPromotions(any(), isNull(), isNull(), isNull(), eq(1), eq(100)))
                .thenReturn(MAPPER.createObjectNode());

        service.listPromotions(null, null, null, 0, 500);

        verify(menuPort).listPromotions(any(), isNull(), isNull(), isNull(), eq(1), eq(100));
    }

    @Test
    void listProductsByCategoryAllowsAllSentinelAndCapsResults() throws Exception {
        when(menuPort.listProductsByCategory(any(), eq(-1L), eq(20), isNull()))
                .thenReturn(MAPPER.readTree("{\"products\":" + products(35) + "}"));

        JsonNode result = service.listProductsByCategory(-1L, null, null);

        assertThat(result.get("products")).hasSize(30);
        assertThat(result.get("products").get(0).get("options")).hasSize(5);
        verify(menuPort).listProductsByCategory(any(), eq(-1L), eq(20), isNull());
    }

    @Test
    void listProductsByBrandTierRequiresPositiveIds() {
        assertThatThrownBy(() -> service.listProductsByBrandTier(0, 1, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("brandId");
    }

    @Test
    void listProductsByBrandTierCapsLimitAndSearch() throws Exception {
        when(menuPort.listProductsByBrandTier(any(), eq(2), eq(3), eq("latte"), eq(30), eq(9)))
                .thenReturn(MAPPER.readTree("{\"products\":" + products(35) + "}"));

        JsonNode result = service.listProductsByBrandTier(2, 3, " latte ", 500, 9);

        assertThat(result.get("products")).hasSize(30);
        verify(menuPort).listProductsByBrandTier(any(), eq(2), eq(3), eq("latte"), eq(30), eq(9));
    }

    @Test
    void getProductCustomizationsCapsEntities() throws Exception {
        when(menuPort.getProductCustomizations(any(), eq(5L)))
                .thenReturn(MAPPER.readTree(products(7)));

        JsonNode result = service.getProductCustomizations(5L);

        assertThat(result).hasSize(5);
        assertThat(result.get(0).get("options")).hasSize(5);
    }

    @Test
    void getProductCustomizationsRequiresPositiveId() {
        assertThatThrownBy(() -> service.getProductCustomizations(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("productId");
    }

    @Test
    void listCustomizationsCapsSizeToThirty() throws Exception {
        when(menuPort.listCustomizations(any(), eq(1), eq(30)))
                .thenReturn(MAPPER.readTree("{\"content\":" + products(35) + "}"));

        JsonNode result = service.listCustomizations(0, 500);

        assertThat(result.get("content")).hasSize(30);
        verify(menuPort).listCustomizations(any(), eq(1), eq(30));
    }

    @Test
    void findProductsByCustomizationCapsResults() throws Exception {
        when(menuPort.findProductsByCustomization(any(), eq(3), eq(30)))
                .thenReturn(MAPPER.readTree("{\"products\":" + products(35) + "}"));

        JsonNode result = service.findProductsByCustomization(3, 500);

        assertThat(result.get("products")).hasSize(30);
        verify(menuPort).findProductsByCustomization(any(), eq(3), eq(30));
    }

    private String products(int size) {
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < size; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append("{\"id\":").append(index).append(",\"options\":[1,2,3,4,5,6,7]}");
        }
        return builder.append(']').toString();
    }
}
