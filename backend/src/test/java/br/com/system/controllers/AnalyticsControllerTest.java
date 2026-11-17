package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsControllerTest extends AbstractIntegrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldAggregateOnlyCompletedSalesInCharts() {
        createSale("COMPLETED", "CASH", 2).statusCode(201);
        createSale("COMPLETED", "PIX", 1).statusCode(201);
        createSale("PENDING", "CREDIT_CARD", 1).statusCode(201);

        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/sales-by-month")
                .then()
                .statusCode(200)
                .body("labels.size()", equalTo(1))
                .body("data.size()", equalTo(1))
                .body("data[0]", equalTo(2));

        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/revenue-by-month")
                .then()
                .statusCode(200)
                .body("labels.size()", equalTo(1))
                .body("data[0]", equalTo(60.0F));

        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/payment-methods")
                .then()
                .statusCode(200)
                .body("labels", hasItem("Dinheiro"))
                .body("labels", hasItem("Pix"))
                .body("labels.size()", equalTo(2))
                .body("data", hasItem(1));
    }

    @Test
    void shouldReturnSevenDailyBucketsAndFillDaysWithoutSalesWithZero() {
        LocalDate today = LocalDate.now();
        Long earlierSaleId = createSale("COMPLETED", "CASH", 1)
                .statusCode(201).extract().jsonPath().getLong("id");
        setSaleDate(earlierSaleId, today.minusDays(3));
        createSale("COMPLETED", "PIX", 1).statusCode(201);

        Response salesResponse = given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "LAST_7_DAYS")
                .when()
                .get("/analytics/sales-over-time")
                .then()
                .statusCode(200)
                .extract().response();

        List<String> salesLabels = salesResponse.jsonPath().getList("labels", String.class);
        List<Number> salesData = salesResponse.jsonPath().getList("data");
        assertEquals(7, salesLabels.size());
        assertEquals(7, salesData.size());
        assertEquals(2, salesData.stream().mapToLong(Number::longValue).sum());
        assertEquals(5, salesData.stream().filter(value -> value.longValue() == 0).count());

        Response lastThirtyDaysResponse = given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "LAST_30_DAYS")
                .when()
                .get("/analytics/sales-over-time")
                .then()
                .statusCode(200)
                .extract().response();
        List<Number> lastThirtyDaysData = lastThirtyDaysResponse.jsonPath().getList("data");
        assertEquals(30, lastThirtyDaysResponse.jsonPath().getList("labels").size());
        assertEquals(30, lastThirtyDaysData.size());
        assertEquals(2, lastThirtyDaysData.stream().mapToLong(Number::longValue).sum());
        assertEquals(28, lastThirtyDaysData.stream().filter(value -> value.longValue() == 0).count());

        Response revenueResponse = given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "LAST_7_DAYS")
                .when()
                .get("/analytics/revenue-over-time")
                .then()
                .statusCode(200)
                .extract().response();

        List<Number> revenueData = revenueResponse.jsonPath().getList("data");
        assertEquals(7, revenueData.size());
        assertEquals(40.0, revenueData.stream().mapToDouble(Number::doubleValue).sum());
        assertEquals(5, revenueData.stream().filter(value -> value.doubleValue() == 0).count());
    }

    @Test
    void shouldGroupLongCustomRangesByYearAndFillMissingMonths() {
        Long saleInFirstYear = createSale("COMPLETED", "CASH", 1)
                .statusCode(201).extract().jsonPath().getLong("id");
        Long saleInSecondYear = createSale("COMPLETED", "PIX", 1)
                .statusCode(201).extract().jsonPath().getLong("id");
        setSaleDate(saleInFirstYear, LocalDate.of(2024, 1, 15));
        setSaleDate(saleInSecondYear, LocalDate.of(2025, 1, 15));

        Response salesResponse = given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("start", "2024-01-01")
                .queryParam("end", "2025-12-31")
                .when()
                .get("/analytics/sales-over-time")
                .then()
                .statusCode(200)
                .extract().response();

        List<String> labels = salesResponse.jsonPath().getList("labels", String.class);
        List<Number> data = salesResponse.jsonPath().getList("data");
        assertEquals(24, labels.size());
        assertTrue(labels.get(0).startsWith("jan"));
        assertTrue(labels.get(0).contains("24"));
        assertTrue(labels.get(12).startsWith("jan"));
        assertTrue(labels.get(12).contains("25"));
        assertEquals(1, data.get(0).intValue());
        assertEquals(0, data.get(1).intValue());
        assertEquals(1, data.get(12).intValue());
        assertEquals(0, data.get(23).intValue());

        List<Number> revenueData = given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("start", "2024-01-01")
                .queryParam("end", "2025-12-31")
                .when()
                .get("/analytics/revenue-over-time")
                .then()
                .statusCode(200)
                .extract().jsonPath().getList("data");
        assertEquals(24, revenueData.size());
        assertEquals(20.0, revenueData.get(0).doubleValue());
        assertEquals(0.0, revenueData.get(1).doubleValue());
        assertEquals(20.0, revenueData.get(12).doubleValue());
    }

    @Test
    void shouldUseDailyBucketsForCustomRangesOfUpToThirtyOneDays() {
        LocalDate end = LocalDate.now();
        Response response = given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("start", end.minusDays(30).toString())
                .queryParam("end", end.toString())
                .when()
                .get("/analytics/sales-over-time")
                .then()
                .statusCode(200)
                .extract().response();

        assertEquals(31, response.jsonPath().getList("labels").size());
        assertEquals(31, response.jsonPath().getList("data").size());
        List<Number> data = response.jsonPath().getList("data");
        assertTrue(data.stream()
                .allMatch(value -> value.longValue() == 0));
    }

    @Test
    void shouldRankProductsUsingOnlyCompletedSalesAndRespectLimit() {
        createSale("COMPLETED", "PIX", 2).statusCode(201);
        createSale("COMPLETED", "CASH", 1).statusCode(201);
        createSale("PENDING", "CREDIT_CARD", 4).statusCode(201);

        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "CURRENT_MONTH")
                .queryParam("limit", 1)
                .when()
                .get("/analytics/top-products")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].productId", equalTo(1))
                .body("[0].productName", equalTo("Test Product"))
                .body("[0].totalSold", equalTo(3));
    }

    @Test
    void shouldReturnProductsWithoutCompletedSalesAndRealDaysSinceLastSale() {
        createProduct("Analytics Product", "7890000000002")
                .statusCode(201)
                .extract().jsonPath().getLong("id");

        LocalDate today = LocalDate.now();
        Long previousCompletedSale = createSale("COMPLETED", "PIX", 1)
                .statusCode(201).extract().jsonPath().getLong("id");
        setSaleDate(previousCompletedSale, today.minusDays(10));
        createSale("PENDING", "CASH", 1).statusCode(201);

        Response response = given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("start", today.minusDays(6).toString())
                .queryParam("end", today.toString())
                .when()
                .get("/analytics/products-without-sales")
                .then()
                .statusCode(200)
                .extract().response();

        List<Map<String, Object>> products = response.jsonPath().getList("");
        Map<String, Object> neverSold = products.stream()
                .filter(product -> "Analytics Product".equals(product.get("productName")))
                .findFirst().orElseThrow();
        Map<String, Object> soldBeforeRange = products.stream()
                .filter(product -> "Test Product".equals(product.get("productName")))
                .findFirst().orElseThrow();

        assertNull(neverSold.get("daysSinceLastSale"));
        assertEquals(10, ((Number) soldBeforeRange.get("daysSinceLastSale")).longValue());
    }

    @Test
    void shouldUseDailyBucketsForShortCustomDateRange() {
        createSale("COMPLETED", "PIX", 1).statusCode(201);

        LocalDate today = LocalDate.now();
        String start = today.minusDays(6).toString();
        String end = today.toString();

        Response response = given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("start", start)
                .queryParam("end", end)
                .when()
                .get("/analytics/sales-over-time")
                .then()
                .statusCode(200)
                .extract().response();
        List<Number> data = response.jsonPath().getList("data");
        assertEquals(7, data.size());
        assertEquals(1, data.stream().mapToLong(Number::longValue).sum());
    }

    @Test
    void shouldRejectIncompleteOrReversedCustomDateRanges() {
        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("start", LocalDate.now().toString())
                .when()
                .get("/analytics/sales-over-time")
                .then()
                .statusCode(400);

        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("start", "2025-02-01")
                .queryParam("end", "2025-01-01")
                .when()
                .get("/analytics/sales-over-time")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRejectInvalidAnalyticsPeriod() {
        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "INVALID")
                .when()
                .get("/analytics/sales-by-month")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRequireAuthenticationForAnalytics() {
        given()
                .when()
                .get("/analytics/sales-by-month")
                .then()
                .statusCode(401);
    }

    @Test
    @Sql(scripts = "classpath:sql/seed-second-company.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldOnlyAggregateDataFromTheActiveCompany() {
        String defaultCompanyToken = ownerAccessToken();
        String companyBToken = accessToken("owner-empresa-b", "password");

        createCompletedSale(defaultCompanyToken, 1, "CASH", 2);
        createCompletedSale(companyBToken, 2, "PIX", 3);

        createProduct(defaultCompanyToken, "Default Company Unsold Product", "7890000000003", 1);
        createProduct(companyBToken, "Company B Unsold Product", "7890000000004", 2);

        given()
                .auth().oauth2(companyBToken)
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/sales-by-month")
                .then()
                .statusCode(200)
                .body("data", contains(1));

        given()
                .auth().oauth2(companyBToken)
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/revenue-by-month")
                .then()
                .statusCode(200)
                .body("data", contains(60.0F));

        given()
                .auth().oauth2(companyBToken)
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/payment-methods")
                .then()
                .statusCode(200)
                .body("labels", contains("Pix"))
                .body("data", contains(1));

        given()
                .auth().oauth2(companyBToken)
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/top-products")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1))
                .body("[0].productId", equalTo(2))
                .body("[0].productName", equalTo("Product From Company B"))
                .body("[0].totalSold", equalTo(3));

        given()
                .auth().oauth2(companyBToken)
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/products-without-movement")
                .then()
                .statusCode(200)
                .body("productName", contains("Company B Unsold Product"));
    }

    private io.restassured.response.ValidatableResponse createSale(
            String status, String paymentMethod, int quantity) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "status": "%s",
                            "paymentMethod": "%s",
                            "items": [
                                {
                                    "productId": 1,
                                    "quantity": %d
                                }
                            ]
                        }
                        """.formatted(status, paymentMethod, quantity))
                .when()
                .post("/sales")
                .then()
                .log().ifValidationFails();
    }

    private void setSaleDate(Long saleId, LocalDate date) {
        jdbcTemplate.update("UPDATE sale SET date = ? WHERE id = ?",
                java.sql.Timestamp.valueOf(date.atTime(12, 0)), saleId);
    }

    private io.restassured.response.ValidatableResponse createProduct(String name, String barcode) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s",
                            "barcode": "%s",
                            "purchasePrice": 10.00,
                            "salePrice": 20.00,
                            "quantity": 5,
                            "categoryId": 1,
                            "brandId": 1,
                            "supplierId": 1
                        }
                        """.formatted(name, barcode))
                .when()
                .post("/products")
                .then()
                .log().ifValidationFails();
    }

    private void createCompletedSale(String token, long productId, String paymentMethod, int quantity) {
        given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "status": "COMPLETED",
                            "paymentMethod": "%s",
                            "items": [
                                {
                                    "productId": %d,
                                    "quantity": %d
                                }
                            ]
                        }
                        """.formatted(paymentMethod, productId, quantity))
                .when()
                .post("/sales")
                .then()
                .statusCode(201);
    }

    private void createProduct(String token, String name, String barcode, long relationshipId) {
        given()
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "%s",
                            "barcode": "%s",
                            "purchasePrice": 10.00,
                            "salePrice": 20.00,
                            "quantity": 5,
                            "categoryId": %d,
                            "brandId": %d,
                            "supplierId": %d
                        }
                        """.formatted(name, barcode, relationshipId, relationshipId, relationshipId))
                .when()
                .post("/products")
                .then()
                .statusCode(201);
    }
}
