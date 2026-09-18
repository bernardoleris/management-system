package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(
        scripts = "classpath:sql/seed-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
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
}
