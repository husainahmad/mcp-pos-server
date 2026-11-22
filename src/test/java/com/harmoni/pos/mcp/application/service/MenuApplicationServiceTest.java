package com.harmoni.pos.mcp.application.service;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuApplicationServiceTest {

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
        when(menuPort.searchProducts(any(), eq("latte"))).thenReturn(new ObjectMapper().createObjectNode());

        service.searchProducts("  latte  ");

        verify(menuPort).searchProducts(any(), eq("latte"));
    }

    @Test
    void promotionsClampPageAndSize() {
        when(menuPort.listPromotions(any(), isNull(), isNull(), isNull(), eq(1), eq(100)))
                .thenReturn(new ObjectMapper().createObjectNode());

        service.listPromotions(null, null, null, 0, 500);

        verify(menuPort).listPromotions(any(), isNull(), isNull(), isNull(), eq(1), eq(100));
    }
}
