package com.harmoni.pos.mcp.domain;

public record CallerContext(String username, String roles, String permissions, String scopes) {

    public boolean isPresent() {
        return username != null && !username.isBlank();
    }
}
