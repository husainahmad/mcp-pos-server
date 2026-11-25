package com.harmoni.pos.mcp.application.port.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CallerContext;

public interface OrganizationPort {

    JsonNode organizationTree(CallerContext caller);

    JsonNode listStores(CallerContext caller, int chainId, int page, int size, String search);

    JsonNode listTables(CallerContext caller, int storeId);

    JsonNode listTiersByType(CallerContext caller, int brandId, String tierType);
}
