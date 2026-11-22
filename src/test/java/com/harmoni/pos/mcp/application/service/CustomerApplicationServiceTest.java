package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.mcp.application.port.out.CustomerPort;
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
class CustomerApplicationServiceTest {

    @Mock
    private CustomerPort customerPort;

    @InjectMocks
    private CustomerApplicationService service;

    @BeforeEach
    void setUp() {
        CallerContextHolder.set(new CallerContext("Kasir1", "", "customer:read", "STORE:1"));
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
}
