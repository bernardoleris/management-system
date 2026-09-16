package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(
        scripts = "classpath:sql/seed-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class SaleControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreatePendingSaleWithoutChangingProductQuantity() {
        createSale("""
                {
                    "status": "PENDING",
                    "paymentMethod": "PIX",
                    "discount": 5.00,
                    "clientId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 2,
                            "unitPrice": 20.00
                        }
                    ]
                }
                """)
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("PENDING"))
                .body("paymentMethod", equalTo("PIX"))
                .body("discount", equalTo(5.0F))
                .body("totalValue", equalTo(35.0F))
                .body("adminLogin", equalTo("admin"))
                .body("clientId", equalTo(1))
                .body("items[0].productId", equalTo(1))
                .body("items[0].quantity", equalTo(2))
                .body("items[0].subtotal", equalTo(40.0F));

        assertProductQuantity(10);
    }

    @Test
    void shouldCreateCompletedSaleAndDecreaseProductQuantity() {
        Long saleId = createSale("""
                {
                    "status": "COMPLETED",
                    "paymentMethod": "CASH",
                    "clientId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 3
                        }
                    ]
                }
                """)
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("COMPLETED"))
                .body("paymentMethod", equalTo("CASH"))
                .body("totalValue", equalTo(60.0F))
                .body("items[0].unitPrice", equalTo(20.0F))
                .body("items[0].subtotal", equalTo(60.0F))
                .extract()
                .jsonPath()
                .getLong("id");

        assertProductQuantity(7);

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/stock-movements/type/SALE")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("content[0].saleId", equalTo(saleId.intValue()))
                .body("content[0].items[0].quantity", equalTo(3));
    }

    @Test
    void shouldCancelCompletedSaleAndRestoreProductQuantity() {
        Long saleId = createCompletedSale(4);

        assertProductQuantity(6);

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .patch("/sales/{id}/cancel", saleId)
                .then()
                .statusCode(200)
                .body("id", equalTo(saleId.intValue()))
                .body("status", equalTo("CANCELED"));

        assertProductQuantity(10);
    }

    @Test
    void shouldUpdateCompletedSaleAndRecalculateProductQuantity() {
        Long saleId = createCompletedSale(4);

        assertProductQuantity(6);

        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                {
                    "status": "COMPLETED",
                    "paymentMethod": "DEBIT_CARD",
                    "clientId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 2
                        }
                    ]
                }
                """)
                .when()
                .put("/sales/{id}", saleId)
                .then()
                .statusCode(200)
                .body("id", equalTo(saleId.intValue()))
                .body("status", equalTo("COMPLETED"))
                .body("paymentMethod", equalTo("DEBIT_CARD"))
                .body("totalValue", equalTo(40.0F))
                .body("items[0].quantity", equalTo(2));

        assertProductQuantity(8);
    }

    @Test
    void shouldReturn404WhenSaleProductDoesNotExist() {
        createSale("""
                {
                    "status": "COMPLETED",
                    "paymentMethod": "CREDIT_CARD",
                    "clientId": 1,
                    "items": [
                        {
                            "productId": 999,
                            "quantity": 1
                        }
                    ]
                }
                """)
                .statusCode(404);

        assertProductQuantity(10);
    }

    @Test
    void shouldReturn422AndKeepStockWhenCompletedSaleHasInsufficientStock() {
        createSale("""
                {
                    "status": "COMPLETED",
                    "paymentMethod": "PIX",
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 11
                        }
                    ]
                }
                """)
                .statusCode(422);

        assertProductQuantity(10);
    }

    @Test
    void shouldCreateSaleWithoutClient() {
        createSale("""
                {
                    "status": "PENDING",
                    "paymentMethod": "PIX",
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 1
                        }
                    ]
                }
                """)
                .statusCode(201)
                .body("clientId", org.hamcrest.Matchers.nullValue())
                .body("totalValue", equalTo(20.0F));
    }

    @Test
    void shouldReturn400WhenDiscountExceedsItemsTotal() {
        createSale("""
                {
                    "status": "PENDING",
                    "paymentMethod": "PIX",
                    "discount": 21.00,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 1
                        }
                    ]
                }
                """)
                .statusCode(400);
    }

    private Long createCompletedSale(int quantity) {
        return createSale("""
                {
                    "status": "COMPLETED",
                    "paymentMethod": "PIX",
                    "clientId": 1,
                    "items": [
                        {
                            "productId": 1,
                            "quantity": %d
                        }
                    ]
                }
                """.formatted(quantity))
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private io.restassured.response.ValidatableResponse createSale(String body) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/sales")
                .then()
                .log().ifValidationFails();
    }

    private void assertProductQuantity(int quantity) {
        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/products/{id}", 1)
                .then()
                .statusCode(200)
                .body("quantity", equalTo(quantity));
    }
}
