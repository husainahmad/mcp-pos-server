package com.harmoni.pos.mcp.adapter.in.web;

import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.config.McpProperties;
import com.harmoni.pos.mcp.domain.CallerContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class CallerContextFilter extends OncePerRequestFilter {

    private final McpProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            CallerContextHolder.set(resolve(request));
            filterChain.doFilter(request, response);
        } finally {
            CallerContextHolder.clear();
        }
    }

    private CallerContext resolve(HttpServletRequest request) {
        String username = request.getHeader("X-Username");
        if (username != null && !username.isBlank()) {
            return new CallerContext(username, request.getHeader("X-Roles"),
                    request.getHeader("X-Permissions"), request.getHeader("X-Scopes"));
        }
        if (!properties.isAllowDefaultCaller()) {
            return new CallerContext(null, null, null, null);
        }
        return new CallerContext(properties.getDefaultUsername(), properties.getDefaultRoles(),
                properties.getDefaultPermissions(), properties.getDefaultScopes());
    }
}
