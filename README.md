# Harmoni POS MCP Server

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

Ordering uses the existing `POST /api/v1/order` path. There is no quote endpoint
or order-session endpoint: `pos_calculate_order` calculates without persisting,
and `pos_create_confirmed_order` creates the order with status `CONFIRMED`.
Payment is a separate `pos_pay_order` call.

Inventory, incremental cart/session mutations, persistent promotion application
and close-day settlement have no downstream endpoints and are not exposed.

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
