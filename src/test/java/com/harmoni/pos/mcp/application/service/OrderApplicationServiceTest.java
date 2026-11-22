package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.mcp.application.port.out.OrderPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.CreateOrderCommand;
import com.harmoni.pos.mcp.domain.PayOrderCommand;
import com.harmoni.pos.mcp.domain.ReportType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private OrderPort orderPort;

    @InjectMocks
    private OrderApplicationService service;

    @BeforeEach
    void setUp() {
        CallerContextHolder.set(new CallerContext("Kasir1", "", "order:view", "STORE:1"));
    }

    @AfterEach
    void tearDown() {
        CallerContextHolder.clear();
    }

    @Test
    void listOrdersClampsLimit() {
        when(orderPort.listOrders(any(), eq(200), isNull())).thenReturn(MAPPER.createObjectNode());

        service.listOrders(999, null);

        verify(orderPort).listOrders(any(), eq(200), isNull());
    }

    @Test
    void createOrderGeneratesIdempotencyKeyWhenMissing() {
        when(orderPort.createOrder(any(), any(), any())).thenReturn(MAPPER.createObjectNode());

        service.createOrder(command(), " ");

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(orderPort).createOrder(any(), any(), key.capture());
        assertThat(key.getValue()).isNotBlank();
    }

    @Test
    void calculateOrderUsesOrderLinesWithoutPersisting() {
        when(orderPort.calculateOrder(any(), any())).thenReturn(MAPPER.createObjectNode());

        service.calculateOrder(command());

        verify(orderPort).calculateOrder(any(), any());
    }

    @Test
    void payOrderRequiresIds() {
        when(orderPort.payOrder(any(), any())).thenReturn(MAPPER.createObjectNode());

        service.payOrder(new PayOrderCommand(10, 2));

        verify(orderPort).payOrder(any(), any());
    }

    @Test
    void reportTreatsDateOnlyEndAsEndOfDay() {
        when(orderPort.report(any(), eq(ReportType.DAILY), any(), any(), isNull(), eq(50)))
                .thenReturn(MAPPER.createObjectNode());

        service.report("daily", "2026-11-22", "2026-11-22", null, null);

        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(orderPort).report(any(), eq(ReportType.DAILY), start.capture(), end.capture(),
                isNull(), eq(50));
        assertThat(start.getValue()).isEqualTo(LocalDateTime.of(2026, 11, 22, 0, 0));
        assertThat(end.getValue()).isEqualTo(LocalDateTime.of(2026, 11, 22, 23, 59, 59));
    }

    private CreateOrderCommand command() {
        return new CreateOrderCommand(1, null, 1, null,
                List.of(new CreateOrderCommand.OrderLineCommand(10, 20, 1, List.of())));
    }
}
