package com.harmoni.pos.mcp.adapter.out.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.harmoni.pos.mcp.domain.CallerContext;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ClientAdapterTest {

    private static final CallerContext CALLER =
            new CallerContext("Kasir1", "", "menu:view", "BRAND:1");

    @Test
    void menuAdapterUsesCategoryPathAndUnwrapsData() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://menu");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MenuClientAdapter adapter = new MenuClientAdapter(builder.build(), new JsonRestClient());

        server.expect(requestTo("http://menu/api/v1/product/category/7/price/page?limit=20"))
                .andExpect(queryParam("limit", "20"))
                .andRespond(withSuccess("{\"data\":[{\"id\":1}]}", MediaType.APPLICATION_JSON));

        JsonNode result = adapter.listProductsByCategory(CALLER, 7, 20, null);

        assertThat(result.isArray()).isTrue();
        server.verify();
    }

    @Test
    void menuAdapterUsesBrandTierCursorQuery() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://menu");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        MenuClientAdapter adapter = new MenuClientAdapter(builder.build(), new JsonRestClient());

        server.expect(requestTo(
                "http://menu/api/v1/product/brand/2/tier/3/cursor?search=latte&limit=20&beforeId=9"))
                .andExpect(queryParam("search", "latte"))
                .andExpect(queryParam("limit", "20"))
                .andExpect(queryParam("beforeId", "9"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        adapter.listProductsByBrandTier(CALLER, 2, 3, "latte", 20, 9);

        server.verify();
    }

    @Test
    void promotionAdapterUsesCodeAndCatalogItemPaths() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://menu");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        PromotionClientAdapter adapter = new PromotionClientAdapter(builder.build(), new JsonRestClient());

        server.expect(requestTo("http://menu/api/v1/promotion/code/SUMMER-SALE"))
                .andRespond(withSuccess("{\"data\":{\"id\":1}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(
                "http://menu/api/v1/promotion/for-catalog-item?productId=10&skuId=11&categoryId=12"))
                .andExpect(queryParam("productId", "10"))
                .andExpect(queryParam("skuId", "11"))
                .andExpect(queryParam("categoryId", "12"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        assertThat(adapter.getPromotion(CALLER, null, "SUMMER-SALE").get("id").asInt()).isEqualTo(1);
        assertThat(adapter.findPromotionsForCatalogItem(CALLER, 10, 11, 12).isArray()).isTrue();

        server.verify();
    }

    @Test
    void orderAdapterUsesHistoryQueryAndVoidPath() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://order");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OrderClientAdapter adapter = new OrderClientAdapter(builder.build(), new JsonRestClient());

        server.expect(requestTo(
                "http://order/api/v1/order-customers/customer/5/history?from=2026-11-22T00:00&to=2026-11-22T23:59:59"))
                .andExpect(queryParam("from", "2026-11-22T00:00"))
                .andExpect(queryParam("to", "2026-11-22T23:59:59"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://order/api/v1/order/9/void"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("{\"data\":{}}", MediaType.APPLICATION_JSON));

        adapter.getCustomerOrders(CALLER, 5, LocalDateTime.of(2026, 11, 22, 0, 0),
                LocalDateTime.of(2026, 11, 22, 23, 59, 59));
        adapter.voidOrder(CALLER, 9);

        server.verify();
    }

    @Test
    void customerAdapterPassesBareJsonThrough() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://customer");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CustomerClientAdapter adapter = new CustomerClientAdapter(builder.build(), new JsonRestClient());

        server.expect(requestTo("http://customer/api/v1/campaigns/vouchers?customerId=3"))
                .andExpect(queryParam("customerId", "3"))
                .andRespond(withSuccess("{\"id\":1,\"data\":{\"inner\":true}}", MediaType.APPLICATION_JSON));

        JsonNode result = adapter.getVouchers(CALLER, 3);

        assertThat(result.has("id")).isTrue();
        server.verify();
    }

    @Test
    void organizationAdapterUsesAllStoreQueryParameters() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://menu");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OrganizationClientAdapter adapter =
                new OrganizationClientAdapter(builder.build(), new JsonRestClient());

        server.expect(requestTo("http://menu/api/v1/store?chainId=2&page=1&size=30&search=west"))
                .andExpect(queryParam("chainId", "2"))
                .andExpect(queryParam("page", "1"))
                .andExpect(queryParam("size", "30"))
                .andExpect(queryParam("search", "west"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://menu/api/v1/tier/brand/4/type/PRICE"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        adapter.listStores(CALLER, 2, 1, 30, "west");
        adapter.listTiersByType(CALLER, 4, "PRICE");

        server.verify();
    }

    @Test
    void customerAdapterUsesRetentionPathsAndBareJson() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://customer");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CustomerClientAdapter adapter = new CustomerClientAdapter(builder.build(), new JsonRestClient());

        server.expect(requestTo("http://customer/api/v1/growth/retention/segments?brandId=1&storeId=2"))
                .andRespond(withSuccess("[{\"segmentCode\":\"AT_RISK\"}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://customer/api/v1/growth/retention/customers?brandId=1&segment=AT_RISK&page=0&size=30&optInOnly=false"))
                .andRespond(withSuccess("[{\"customerId\":5}]", MediaType.APPLICATION_JSON));

        assertThat(adapter.retentionSegments(CALLER, 1, 2).isArray()).isTrue();
        assertThat(adapter.retentionCustomers(CALLER, 1, null, "AT_RISK", null, null, 0, 30)
                .isArray()).isTrue();
        server.verify();
    }

    @Test
    void customerAdapterUsesAudiencePreviewAndIssuePaths() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://customer");
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        CustomerClientAdapter adapter = new CustomerClientAdapter(builder.build(), new JsonRestClient());

        server.expect(requestTo("http://customer/api/v1/campaigns/audience/preview"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {"campaignId":9,"brandId":1,"segment":"AT_RISK","page":0,"size":30}
                        """))
                .andRespond(withSuccess("{\"id\":3,\"audienceSize\":100}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://customer/api/v1/campaigns/audience/3/issue"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"confirmed\":true}"))
                .andRespond(withSuccess("{\"issued\":10,\"skipped\":2}", MediaType.APPLICATION_JSON));

        assertThat(adapter.previewCampaignAudience(CALLER, 9, 1, null, "AT_RISK", null, null, 0, 30)
                .get("audienceSize").asInt()).isEqualTo(100);
        assertThat(adapter.issueVouchersBulk(CALLER, 3, true).get("issued").asInt()).isEqualTo(10);
        server.verify();
    }
}
