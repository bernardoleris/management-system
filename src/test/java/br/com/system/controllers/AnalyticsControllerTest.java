package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

class AnalyticsControllerTest extends AbstractIntegrationTest {

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
    void shouldReturnActiveProductsWithoutSalesInPeriod() {
        Long productId = createProduct("Analytics Product", "7890000000002")
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        createSale("COMPLETED", "PIX", 1).statusCode(201);

        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("period", "CURRENT_MONTH")
                .when()
                .get("/analytics/products-without-movement")
                .then()
                .statusCode(200)
                .body("productId", hasItem(productId.intValue()))
                .body("productName", hasItem("Analytics Product"));
    }

    @Test
    void shouldAcceptCustomDateRange() {
        createSale("COMPLETED", "PIX", 1).statusCode(201);

        LocalDate today = LocalDate.now();
        String start = today.withDayOfYear(1).toString();
        String end = today.withMonth(12).withDayOfMonth(31).toString();

        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("start", start)
                .queryParam("end", end)
                .when()
                .get("/analytics/sales-by-month")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("data[0]", equalTo(1));
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
