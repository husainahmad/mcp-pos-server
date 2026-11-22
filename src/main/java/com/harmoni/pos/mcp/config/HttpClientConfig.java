package com.harmoni.pos.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient orderRestClient(McpProperties properties, RestClient.Builder builder) {
        return client(properties.getOrder().getBaseUrl(), properties, builder);
    }

    @Bean
    public RestClient menuRestClient(McpProperties properties, RestClient.Builder builder) {
        return client(properties.getMenu().getBaseUrl(), properties, builder);
    }

    @Bean
    public RestClient customerRestClient(McpProperties properties, RestClient.Builder builder) {
        return client(properties.getCustomer().getBaseUrl(), properties, builder);
    }

    private RestClient client(String baseUrl, McpProperties properties, RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.getReadTimeout());
        return builder.baseUrl(baseUrl).requestFactory(factory).build();
    }
}
