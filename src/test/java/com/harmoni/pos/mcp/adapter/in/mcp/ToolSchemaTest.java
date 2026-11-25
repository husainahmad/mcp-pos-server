package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.CatalogUseCase;
import com.harmoni.pos.mcp.application.port.in.CustomerUseCase;
import com.harmoni.pos.mcp.application.port.in.MenuUseCase;
import com.harmoni.pos.mcp.application.port.in.OrganizationUseCase;
import com.harmoni.pos.mcp.application.port.in.OrderUseCase;
import com.harmoni.pos.mcp.application.port.in.PromotionUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.annotation.spring.SyncMcpAnnotationProviders;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ToolSchemaTest {

    @Test
    void registersAllPosTools() {
        List<Object> tools = List.of(
                new OrderMcpTools(mock(OrderUseCase.class)),
                new MenuMcpTools(mock(MenuUseCase.class)),
                new CustomerMcpTools(mock(CustomerUseCase.class)),
                new CatalogMcpTools(mock(CatalogUseCase.class)),
                new PromotionMcpTools(mock(PromotionUseCase.class)),
                new OrganizationMcpTools(mock(OrganizationUseCase.class)));

        Set<String> names = SyncMcpAnnotationProviders.toolSpecifications(tools).stream()
                .map(specification -> specification.tool().name())
                .collect(Collectors.toSet());

        assertThat(names).containsExactlyInAnyOrder(
                "pos_list_orders",
                "pos_calculate_order",
                "pos_create_confirmed_order",
                "pos_pay_order",
                "pos_get_sales_report",
                "pos_search_products",
                "pos_get_product",
                "pos_search_categories",
                "pos_list_promotions",
                "pos_search_customers",
                "pos_get_customer",
                "pos_get_customer_metrics",
                "pos_get_customer_loyalty",
                "pos_get_current_user",
                "pos_list_brands",
                "pos_list_tiers",
                "pos_get_product_skus",
                "pos_get_sku_tier_prices",
                "pos_create_category",
                "pos_update_category",
                "pos_create_product",
                "pos_update_product",
                "pos_create_customization",
                "pos_update_customization",
                "pos_configure_product_customization",
                "pos_validate_order_items",
                "pos_calculate_customization_price",
                "pos_create_promotion",
                "pos_update_promotion",
                "pos_set_promotion_status",
                "pos_calculate_promotion_price",
                "pos_list_products_by_category",
                "pos_list_products_by_brand_tier",
                "pos_get_product_customizations",
                "pos_list_customizations",
                "pos_get_promotion",
                "pos_list_redeemable_promotions",
                "pos_get_customer_orders",
                "pos_get_best_sellers",
                "pos_get_sales_totals",
                "pos_get_daily_sales",
                "pos_get_settlement",
                "pos_get_customer_loyalty_history",
                "pos_get_customer_vouchers",
                "pos_get_customer_insights",
                "pos_get_sales_summary",
                "pos_get_organization_tree",
                "pos_list_stores",
                "pos_list_tables",
                "pos_list_tiers_by_type",
                "pos_find_products_by_customization",
                "pos_find_promotions_for_catalog_item",
                "pos_get_order",
                "pos_void_order",
                "pos_create_customer",
                "pos_update_customer",
                "pos_redeem_voucher",
                "pos_redeem_loyalty_points",
                "pos_get_retention_overview",
                "pos_list_retention_segments",
                "pos_list_retention_cohorts",
                "pos_list_at_risk_customers",
                "pos_list_lapsed_customers",
                "pos_list_campaigns",
                "pos_get_campaign_performance",
                "pos_preview_campaign_audience",
                "pos_create_retention_campaign",
                "pos_issue_vouchers_bulk");
    }
}
