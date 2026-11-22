package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.harmoni.pos.mcp.domain.CallerContext;
import com.harmoni.pos.mcp.domain.exception.DownstreamException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class JsonRestClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public JsonNode get(RestClient client, String path, CallerContext caller, Map<String, Object> query) {
        try {
            return client.get()
                    .uri(builder -> {
                        builder.path(path);
                        addQuery(builder, query);
                        return builder.build();
                    })
                    .headers(headers -> applyCaller(headers, caller))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException exception) {
            throw new DownstreamException("GET " + path + " failed", exception);
        }
    }

    public JsonNode post(RestClient client, String path, CallerContext caller, Object body) {
        return post(client, path, caller, body, Map.of());
    }

    public JsonNode post(RestClient client, String path, CallerContext caller, Object body,
                         Map<String, String> extraHeaders) {
        try {
            return client.post()
                    .uri(path)
                    .headers(headers -> {
                        applyCaller(headers, caller);
                        extraHeaders.forEach(headers::set);
                    })
                    .body(clean(body))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException exception) {
            throw new DownstreamException("POST " + path + " failed", exception);
        }
    }

    public JsonNode put(RestClient client, String path, CallerContext caller, Object body) {
        try {
            return client.put()
                    .uri(path)
                    .headers(headers -> applyCaller(headers, caller))
                    .body(clean(body))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException exception) {
            throw new DownstreamException("PUT " + path + " failed", exception);
        }
    }

    public JsonNode patch(RestClient client, String path, CallerContext caller, Map<String, Object> query) {
        try {
            return client.patch()
                    .uri(builder -> {
                        builder.path(path);
                        addQuery(builder, query);
                        return builder.build();
                    })
                    .headers(headers -> applyCaller(headers, caller))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException exception) {
            throw new DownstreamException("PATCH " + path + " failed", exception);
        }
    }

    private void addQuery(org.springframework.web.util.UriBuilder builder, Map<String, Object> query) {
        query.forEach((key, value) -> {
            if (value instanceof Collection<?> values) {
                values.forEach(item -> builder.queryParam(key, item));
            } else {
                builder.queryParam(key, value);
            }
        });
    }

    private void applyCaller(HttpHeaders headers, CallerContext caller) {
        headers.set("X-Username", caller.username());
        setIfPresent(headers, "X-Roles", caller.roles());
        setIfPresent(headers, "X-Permissions", caller.permissions());
        setIfPresent(headers, "X-Scopes", caller.scopes());
    }

    private void setIfPresent(HttpHeaders headers, String name, String value) {
        if (value != null && !value.isBlank()) {
            headers.set(name, value);
        }
    }

    private JsonNode clean(Object body) {
        JsonNode node = MAPPER.valueToTree(body);
        removeNulls(node);
        return node;
    }

    private void removeNulls(JsonNode node) {
        if (node instanceof ObjectNode object) {
            Iterator<Map.Entry<String, JsonNode>> fields = object.fields();
            List<String> nullFields = new ArrayList<>();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getValue() == null || field.getValue().isNull()) {
                    nullFields.add(field.getKey());
                } else {
                    removeNulls(field.getValue());
                }
            }
            nullFields.forEach(object::remove);
        } else if (node != null && node.isArray()) {
            node.forEach(this::removeNulls);
        }
    }
}
