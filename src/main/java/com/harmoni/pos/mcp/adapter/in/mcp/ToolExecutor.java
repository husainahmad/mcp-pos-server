package com.harmoni.pos.mcp.adapter.in.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public final class ToolExecutor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ToolExecutor() {
    }

    public static String call(Supplier<JsonNode> action) {
        try {
            JsonNode result = action.get();
            return result == null ? "{}" : result.toString();
        } catch (RuntimeException exception) {
            log.warn("MCP tool failed: {}", exception.getMessage());
            return error(exception.getMessage() == null ? "tool call failed" : exception.getMessage());
        }
    }

    public static String error(String message) {
        return MAPPER.createObjectNode().put("error", message).toString();
    }
}
