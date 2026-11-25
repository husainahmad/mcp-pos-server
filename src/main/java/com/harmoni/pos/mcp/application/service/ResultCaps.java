package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public final class ResultCaps {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 30;
    public static final int ENTITY_LIMIT = 5;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ResultCaps() {
    }

    public static int boundedLimit(Integer limit) {
        return boundedLimit(limit, DEFAULT_LIMIT, MAX_LIMIT);
    }

    public static int boundedLimit(Integer limit, int defaultLimit, int maxLimit) {
        int fallback = defaultLimit > 0 ? defaultLimit : DEFAULT_LIMIT;
        int cap = Math.max(fallback, maxLimit);
        if (limit == null || limit < 1) {
            return fallback;
        }
        return Math.min(limit, cap);
    }

    public static JsonNode capArray(JsonNode node) {
        return capArray(node, MAX_LIMIT);
    }

    public static JsonNode capArray(JsonNode node, int maxResults) {
        return cap(node, Math.max(maxResults, 1), ENTITY_LIMIT);
    }

    public static JsonNode capEntities(JsonNode node) {
        return capEntities(node, ENTITY_LIMIT);
    }

    public static JsonNode capEntities(JsonNode node, int maxEntities) {
        int cap = Math.max(maxEntities, 1);
        return cap(node, cap, cap);
    }

    public static List<Integer> capStoreIds(List<Integer> storeIds) {
        return capStoreIds(storeIds, ENTITY_LIMIT);
    }

    public static List<Integer> capStoreIds(List<Integer> storeIds, int maxStores) {
        if (storeIds == null || storeIds.isEmpty()) {
            return null;
        }
        return storeIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .limit(Math.max(maxStores, 0))
                .toList();
    }

    private static JsonNode cap(JsonNode node, int currentMax, int nestedMax) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            ArrayNode result = MAPPER.createArrayNode();
            int size = Math.min(node.size(), currentMax);
            for (int index = 0; index < size; index++) {
                result.add(cap(node.get(index), nestedMax, nestedMax));
            }
            return result;
        }
        if (node.isObject()) {
            ObjectNode result = MAPPER.createObjectNode();
            node.fields().forEachRemaining(field -> {
                JsonNode value = field.getValue();
                if (value == null || value.isNull()) {
                    result.putNull(field.getKey());
                } else if (value.isArray()) {
                    result.set(field.getKey(), cap(value, currentMax, nestedMax));
                } else if (value.isObject()) {
                    result.set(field.getKey(), cap(value, nestedMax, nestedMax));
                } else {
                    result.set(field.getKey(), value.deepCopy());
                }
            });
            return result;
        }
        return node.deepCopy();
    }
}
