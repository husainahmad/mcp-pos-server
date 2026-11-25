package com.harmoni.pos.mcp.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.application.port.in.OrganizationUseCase;
import com.harmoni.pos.mcp.application.port.out.OrganizationPort;
import com.harmoni.pos.mcp.application.security.CallerContextHolder;
import com.harmoni.pos.mcp.domain.CallerContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrganizationApplicationService implements OrganizationUseCase {

    private static final Set<String> TIER_TYPES = Set.of("PRICE", "MENU", "SERVICE");

    private final OrganizationPort organizationPort;

    @Override
    public JsonNode organizationTree() {
        CallerContext caller = CallerContextHolder.current();
        return ResultCaps.capEntities(organizationPort.organizationTree(caller));
    }

    @Override
    public JsonNode listStores(Integer chainId, int page, int size, String search) {
        CallerContext caller = CallerContextHolder.current();
        return ResultCaps.capArray(organizationPort.listStores(caller,
                ArgumentChecks.positive(chainId, "chainId"),
                Math.max(page, 1),
                ResultCaps.boundedLimit(size, ResultCaps.DEFAULT_LIMIT, ResultCaps.MAX_LIMIT),
                ArgumentChecks.required(search, "search")));
    }

    @Override
    public JsonNode listTables(int storeId) {
        CallerContext caller = CallerContextHolder.current();
        return ResultCaps.capArray(organizationPort.listTables(caller,
                ArgumentChecks.positive(storeId, "storeId")));
    }

    @Override
    public JsonNode listTiersByType(int brandId, String tierType) {
        CallerContext caller = CallerContextHolder.current();
        String normalized = ArgumentChecks.required(tierType, "tierType")
                .toUpperCase(Locale.ROOT);
        if (!TIER_TYPES.contains(normalized)) {
            throw new IllegalArgumentException("tierType must be PRICE, MENU or SERVICE");
        }
        return ResultCaps.capArray(organizationPort.listTiersByType(caller,
                ArgumentChecks.positive(brandId, "brandId"), normalized));
    }
}
