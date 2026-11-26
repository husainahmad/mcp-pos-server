# Harmoni POS MCP Server

[![M8ven Score](https://m8ven.ai/badge/mcp/husainahmad-mcp-pos-server-ey9jqs?v=ba2d954bae85b5673890b42b59a4b30a)](https://m8ven.ai/mcp/husainahmad-mcp-pos-server-ey9jqs?s=readme)

MCP tool gateway for the Harmoni POS. It exposes catalog, order, promotion and
customer operations to MCP clients.

## Architecture

Java 21, Spring Boot 3.5.7, Spring AI 1.1.0, hexagonal ports and adapters.

```
domain/             records and exceptions
application/        port.in use cases, port.out interfaces, services
adapter/in/mcp/     MCP @McpTool methods
adapter/in/web/     caller header filter
adapter/out/client/ order/menu/customer HTTP adapters
config/             HTTP clients and properties
```

The server has no datasource and no DB dependency. All data comes from the existing order, menu and customer services.

## Identity

The MCP client must send the gateway headers:

- `X-Username` required
- `X-Roles`
- `X-Permissions`
- `X-Scopes`

The server forwards them to downstream services, so the existing RBAC and scope rules still apply.

For local-only testing, `harmoni.mcp.allow-default-caller=true` enables a configured fallback identity.

## Tools

Tools use `@McpTool` and `@McpToolParam` from `org.springaicommunity.mcp.annotation`.
Spring AI's annotation scanner registers them automatically.

| Tool | Purpose |
| --- | --- |
| `pos_get_current_user` | Signed-in user and store |
| `pos_list_brands` | Brands visible to the caller |
| `pos_list_tiers` | Price tiers for a brand |
| `pos_get_product_skus` | SKUs for a product |
| `pos_get_sku_tier_prices` | Existing SKU prices for one tier |
| `pos_search_categories` | Search menu categories |
| `pos_create_category` | Create a category |
| `pos_update_category` | Update a category |
| `pos_search_products` | Search menu products |
| `pos_get_product` | Product details, SKUs, prices and customizations |
| `pos_create_product` | Create a product with SKUs and tier prices |
| `pos_update_product` | Update a product and its complete SKU list |
| `pos_create_customization` | Create a customization group and options |
| `pos_update_customization` | Update a customization group and options |
| `pos_configure_product_customization` | Configure per-product customization overrides |
| `pos_validate_order_items` | Validate items, customizations and official prices |
| `pos_calculate_customization_price` | Calculate customization surcharges |
| `pos_list_promotions` | List promotions |
| `pos_create_promotion` | Create a promotion |
| `pos_update_promotion` | Update a promotion |
| `pos_set_promotion_status` | Change a promotion status |
| `pos_calculate_promotion_price` | Calculate promotion discounts |
| `pos_list_orders` | List today's orders for the caller's store |
| `pos_calculate_order` | Validate and calculate an order without saving it |
| `pos_create_confirmed_order` | Create an order directly in `CONFIRMED` status |
| `pos_pay_order` | Mark one order as `PAID` |
| `pos_get_sales_report` | Settlement, top-products, daily, sales, order-volume reports |
| `pos_search_customers` | Search customers |
| `pos_get_customer` | Customer details |
| `pos_get_customer_metrics` | Customer spend, visits, average order value, segment |
| `pos_get_customer_loyalty` | Loyalty account and points |
| `pos_list_products_by_category` | Priced products in a category, or all categories with `-1` |
| `pos_list_products_by_brand_tier` | Products in a brand and tier with cursor pagination |
| `pos_get_product_customizations` | Customization groups linked to a product |
| `pos_list_customizations` | Customization groups with required page and size |
| `pos_get_promotion` | Promotion by id or code |
| `pos_list_redeemable_promotions` | Redeemable promotions |
| `pos_get_customer_orders` | Customer order history in a date range |
| `pos_get_best_sellers` | Top-selling products report |
| `pos_get_sales_totals` | Sales totals report |
| `pos_get_daily_sales` | Daily sales report |
| `pos_get_settlement` | Settlement report |
| `pos_get_customer_loyalty_history` | Loyalty point history |
| `pos_get_customer_vouchers` | Customer vouchers |
| `pos_get_customer_insights` | Customer insights |
| `pos_get_sales_summary` | Sales analytics summary |
| `pos_get_organization_tree` | Organization tree |
| `pos_list_stores` | Stores in a chain |
| `pos_list_tables` | Tables in a store |
| `pos_list_tiers_by_type` | Tiers of type `PRICE`, `MENU` or `SERVICE` |
| `pos_find_products_by_customization` | Products using one customization |
| `pos_find_promotions_for_catalog_item` | Promotions for a product/SKU/category item |
| `pos_get_order` | Full order details |
| `pos_void_order` | Void an order after a preview and explicit confirmation |
| `pos_create_customer` | Create a customer after explicit confirmation |
| `pos_update_customer` | Update a customer after explicit confirmation |
| `pos_redeem_voucher` | Redeem a voucher after a preview and explicit confirmation |
| `pos_redeem_loyalty_points` | Redeem loyalty points after a preview and explicit confirmation |
| `pos_get_retention_overview` | Retention KPIs for a brand or store, including revenue at risk |
| `pos_list_retention_segments` | Retention segment distribution |
| `pos_list_retention_cohorts` | Monthly cohorts with 30-day repeat retention |
| `pos_list_at_risk_customers` | At-risk customers (no personal data) |
| `pos_list_lapsed_customers` | Lapsed customers (no personal data) |
| `pos_list_campaigns` | Campaigns for a brand |
| `pos_get_campaign_performance` | Issued, redeemed, revenue and reactivations for a campaign |
| `pos_preview_campaign_audience` | Preview a retention audience, size, expected redemptions and discount exposure |
| `pos_create_retention_campaign` | Create a brand-scoped retention campaign after confirmation |
| `pos_issue_vouchers_bulk` | Issue vouchers for an approved audience after explicit confirmation |

Read tools cap broad result lists at 30 entries and nested entity lists at 5.
Write tools reject `confirmed != true` before calling a downstream service.

Ordering uses the existing `POST /api/v1/order` path. There is no quote endpoint
or order-session endpoint: `pos_calculate_order` calculates without persisting,
and `pos_create_confirmed_order` creates the order with status `CONFIRMED`.
Payment is a separate `pos_pay_order` call.

Incremental cart/session mutations and persistent promotion application have no
downstream endpoints and are not exposed. Close-day settlement is available as a
read-only report through `pos_get_settlement`.

## Configuration

| Property | Default |
| --- | --- |
| `server.port` | `8087` |
| `spring.ai.mcp.server.sse-endpoint` | `/api/v1/mcp/sse` |
| `spring.ai.mcp.server.sse-message-endpoint` | `/api/v1/mcp/message` |
| `harmoni.mcp.order.base-url` | `http://localhost:8083` |
| `harmoni.mcp.menu.base-url` | `http://localhost:8082` |
| `harmoni.mcp.customer.base-url` | `http://localhost:8084` |
| `harmoni.mcp.allow-default-caller` | `false` |

## Running

```bash
mvn spring-boot:run
mvn test
```

The gateway routes `/api/v1/mcp/**` to this service.
