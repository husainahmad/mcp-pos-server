package com.harmoni.pos.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "harmoni.mcp")
public class McpProperties {

    private Service order = new Service();
    private Service menu = new Service();
    private Service customer = new Service();
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(30);
    private boolean allowDefaultCaller;
    private String defaultUsername;
    private String defaultRoles;
    private String defaultPermissions;
    private String defaultScopes;

    @Data
    public static class Service {
        private String baseUrl;
    }
}
