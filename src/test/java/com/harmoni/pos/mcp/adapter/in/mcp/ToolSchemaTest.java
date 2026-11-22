package com.harmoni.pos.mcp.adapter.in.mcp;

import com.harmoni.pos.mcp.application.port.in.CatalogUseCase;
import com.harmoni.pos.mcp.application.port.in.CustomerUseCase;
import com.harmoni.pos.mcp.application.port.in.MenuUseCase;
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
                new PromotionMcpTools(mock(PromotionUseCase.class)));

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
                "pos_calculate_promotion_price");
    }
}
