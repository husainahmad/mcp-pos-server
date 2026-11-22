package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;

public final class JsonData {

    private JsonData() {
    }

    public static JsonNode unwrap(JsonNode response) {
        if (response != null && response.has("data")) {
            return response.get("data");
        }
        return response;
    }
}
