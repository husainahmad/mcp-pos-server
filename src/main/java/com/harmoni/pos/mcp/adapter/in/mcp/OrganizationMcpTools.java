package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.OrganizationUseCase;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationMcpTools {

    private final OrganizationUseCase organization;

    @McpTool(name = "pos_get_organization_tree", description = """
            Read the organization tree, including child stores and tables.
            Result scope: inspect at most 5 entities at each level.
            """)
    public String getOrganizationTree() {
        return ToolExecutor.call(organization::organizationTree);
    }

    @McpTool(name = "pos_list_stores", description = """
            List stores for one chain with a required search term and 1-based page.
            Returns at most 30 results.
            """)
    public String listStores(
            @McpToolParam(description = "Chain id.") Integer chainId,
            @McpToolParam(description = "Page number, 1-based.") int page,
            @McpToolParam(description = "Page size, capped at 30.") int size,
            @McpToolParam(description = "Required store name or code search.") String search) {
        return ToolExecutor.call(() -> organization.listStores(chainId, page, size, search));
    }

    @McpTool(name = "pos_list_tables", description = """
            List tables in one store. Returns at most 30 results.
            """)
    public String listTables(
            @McpToolParam(description = "Store id from pos_list_stores.") int storeId) {
        return ToolExecutor.call(() -> organization.listTables(storeId));
    }

    @McpTool(name = "pos_list_tiers_by_type", description = """
            List tiers of type PRICE, MENU or SERVICE for a brand. Input is case-insensitive.
            Returns at most 30 results.
            """)
    public String listTiersByType(
            @McpToolParam(description = "Brand id from pos_list_brands.") int brandId,
            @McpToolParam(description = "Tier type: PRICE, MENU or SERVICE.") String tierType) {
        return ToolExecutor.call(() -> organization.listTiersByType(brandId, tierType));
    }
}
