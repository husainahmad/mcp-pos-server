package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void getCustomerOrdersRequiresPositiveIdAndValidDateBounds() {
        assertThatThrownBy(() -> service.getCustomerOrders(0L, "2026-11-22", "2026-11-23"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customerId");
        assertThatThrownBy(() -> service.getCustomerOrders(1L, "2026-11-23", "2026-11-22"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("start");
        verifyNoInteractions(orderPort);
    }

    @Test
    void getCustomerOrdersCapsResults() throws Exception {
        when(orderPort.getCustomerOrders(any(), eq(5L), any(), any()))
                .thenReturn(MAPPER.readTree(ids(35)));

        JsonNode result = service.getCustomerOrders(5L, "2026-11-22", "2026-11-23");

        assertThat(result).hasSize(30);
        verify(orderPort).getCustomerOrders(any(), eq(5L), any(), any());
    }

    @Test
    void bestSellersCapsLimitStoreIdsAndResults() throws Exception {
        when(orderPort.report(any(), eq(ReportType.TOP_PRODUCTS), any(), any(),
                eq(List.of(1, 2, 3, 4, 5)), eq(30))).thenReturn(MAPPER.readTree(ids(35)));

        JsonNode result = service.bestSellers("2026-11-22", "2026-11-23",
                List.of(1, 2, 3, 4, 5, 6), 999);

        assertThat(result).hasSize(30);
        verify(orderPort).report(any(), eq(ReportType.TOP_PRODUCTS), any(), any(),
                eq(List.of(1, 2, 3, 4, 5)), eq(30));
    }

    @Test
    void salesTotalsCapsStoreIdsAndResults() throws Exception {
        when(orderPort.report(any(), eq(ReportType.SALES), any(), any(),
                eq(List.of(2)), eq(20))).thenReturn(MAPPER.readTree(ids(35)));

        JsonNode result = service.salesTotals("2026-11-22", "2026-11-23", List.of(2, 2));

        assertThat(result).hasSize(30);
        verify(orderPort).report(any(), eq(ReportType.SALES), any(), any(),
                eq(List.of(2)), eq(20));
    }

    @Test
    void getOrderCapsEntities() throws Exception {
        when(orderPort.getOrder(any(), eq(7L)))
                .thenReturn(MAPPER.readTree("{\"orderDetails\":" + ids(9) + "}"));

        JsonNode result = service.getOrder(7L);

        assertThat(result.get("orderDetails")).hasSize(5);
        verify(orderPort).getOrder(any(), eq(7L));
    }

    @Test
    void voidOrderRejectsUnconfirmedBeforePreview() {
        assertThatThrownBy(() -> service.voidOrder(9L, Boolean.FALSE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("confirmed");

        verifyNoInteractions(orderPort);
    }

    @Test
    void voidOrderPreviewsBeforeWrite() {
        when(orderPort.getOrder(any(), eq(9L))).thenReturn(MAPPER.createObjectNode());
        when(orderPort.voidOrder(any(), eq(9L))).thenReturn(MAPPER.createObjectNode());

        service.voidOrder(9L, Boolean.TRUE);

        InOrder order = inOrder(orderPort);
        order.verify(orderPort).getOrder(any(), eq(9L));
        order.verify(orderPort).voidOrder(any(), eq(9L));
    }

    private CreateOrderCommand command() {
        return new CreateOrderCommand(1, null, 1, null,
                List.of(new CreateOrderCommand.OrderLineCommand(10, 20, 1, List.of())));
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
