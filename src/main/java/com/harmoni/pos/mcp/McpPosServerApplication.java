package com.harmoni.pos.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class McpPosServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpPosServerApplication.class, args);
    }
}
