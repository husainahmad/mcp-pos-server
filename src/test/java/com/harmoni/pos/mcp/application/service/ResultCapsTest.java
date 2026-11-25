package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultCapsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void boundedLimitUsesGlobalDefaultsAndMaximum() {
        assertThat(ResultCaps.boundedLimit(null)).isEqualTo(20);
        assertThat(ResultCaps.boundedLimit(0)).isEqualTo(20);
        assertThat(ResultCaps.boundedLimit(500)).isEqualTo(30);
        assertThat(ResultCaps.boundedLimit(7)).isEqualTo(7);
    }

    @Test
    void capArrayKeepsThirtyResultsAndFiveNestedEntities() throws Exception {
        JsonNode response = MAPPER.readTree("{" + array(35, "products", "options") + "}");

        JsonNode capped = ResultCaps.capArray(response, 30);

        assertThat(capped.get("products")).hasSize(30);
        assertThat(capped.get("products").get(0).get("options")).hasSize(5);
    }

    @Test
    void capEntitiesLimitsEveryArrayToFive() throws Exception {
        JsonNode response = MAPPER.readTree("{" + array(9, "items", "children") + "}");

        JsonNode capped = ResultCaps.capEntities(response);

        assertThat(capped.get("items")).hasSize(5);
        assertThat(capped.get("items").get(0).get("children")).hasSize(5);
    }

    @Test
    void capStoreIdsDeduplicatesAndLimits() {
        assertThat(ResultCaps.capStoreIds(List.of(1, 2, 3, 4, 5, 6, 6), 5))
                .containsExactly(1, 2, 3, 4, 5);
        assertThat(ResultCaps.capStoreIds(null)).isNull();
    }

    private String array(int size, String field, String nestedField) {
        StringBuilder builder = new StringBuilder("\"").append(field).append("\":[");
        for (int index = 0; index < size; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append("{\"id\":").append(index).append(",\"").append(nestedField)
                    .append("\":[");
            for (int child = 0; child < 7; child++) {
                if (child > 0) {
                    builder.append(',');
                }
                builder.append(child);
            }
            builder.append("]}");
        }
        return builder.append(']').toString();
    }
}
