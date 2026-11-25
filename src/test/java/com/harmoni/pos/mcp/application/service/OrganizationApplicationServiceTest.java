package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harmoni.pos.mcp.application.port.out.OrganizationPort;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationApplicationServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Mock
    private OrganizationPort organizationPort;

    @InjectMocks
    private OrganizationApplicationService service;

    @BeforeEach
    void setUp() {
        CallerContextHolder.set(new CallerContext("Kasir1", "", "organization:read", "BRAND:1"));
    }

    @AfterEach
    void tearDown() {
        CallerContextHolder.clear();
    }

    @Test
    void organizationTreeCapsNestedEntities() throws Exception {
        when(organizationPort.organizationTree(any()))
                .thenReturn(MAPPER.readTree("{\"children\":" + nodes(9) + "}"));

        JsonNode result = service.organizationTree();

        assertThat(result.get("children")).hasSize(5);
        assertThat(result.get("children").get(0).get("children")).hasSize(5);
    }

    @Test
    void listStoresCapsSizeAndRequiresAllParameters() throws Exception {
        when(organizationPort.listStores(any(), eq(2), eq(1), eq(30), eq("west")))
                .thenReturn(MAPPER.readTree("{\"content\":" + nodes(35) + "}"));

        JsonNode result = service.listStores(2, 0, 500, " west ");

        assertThat(result.get("content")).hasSize(30);
        verify(organizationPort).listStores(any(), eq(2), eq(1), eq(30), eq("west"));
        assertThatThrownBy(() -> service.listStores(0, 1, 20, "west"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("chainId");
        assertThatThrownBy(() -> service.listStores(2, 1, 20, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("search");
    }

    @Test
    void listTablesRequiresPositiveStoreAndCapsResults() throws Exception {
        when(organizationPort.listTables(any(), eq(3))).thenReturn(MAPPER.readTree(nodes(35)));

        JsonNode result = service.listTables(3);

        assertThat(result).hasSize(30);
        assertThatThrownBy(() -> service.listTables(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("storeId");
    }

    @Test
    void listTiersByTypeUppercasesAndWhitelists() throws Exception {
        when(organizationPort.listTiersByType(any(), eq(4), eq("PRICE")))
                .thenReturn(MAPPER.readTree(nodes(35)));

        JsonNode result = service.listTiersByType(4, "price");

        assertThat(result).hasSize(30);
        verify(organizationPort).listTiersByType(any(), eq(4), eq("PRICE"));
        assertThatThrownBy(() -> service.listTiersByType(4, "UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PRICE");
    }

    private String nodes(int size) {
        StringBuilder builder = new StringBuilder("[");
        for (int index = 0; index < size; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append("{\"id\":").append(index).append(",\"children\":[1,2,3,4,5,6]}");
        }
        return builder.append(']').toString();
    }
}
