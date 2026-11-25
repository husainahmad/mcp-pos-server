package com.harmoni.pos.mcp.application.port.in;

import com.fasterxml.jackson.databind.JsonNode;

public interface OrganizationUseCase {

    JsonNode organizationTree();

    JsonNode listStores(Integer chainId, int page, int size, String search);

    JsonNode listTables(int storeId);

    JsonNode listTiersByType(int brandId, String tierType);
}
