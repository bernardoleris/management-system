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
class StockMovementControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldIncreaseProductQuantityWhenEntryMovementIsCreated() {
        createStockMovement("""
                {
                    "type": "ENTRY",
                    "supplierId": 1,
                    "observation": "Integration test entry",
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 5
                        }
                    ]
                }
                """)
                .statusCode(201)
                .body("id", notNullValue())
                .body("type", equalTo("ENTRY"))
                .body("adminLogin", equalTo("admin"))
                .body("supplierId", equalTo(1))
                .body("items[0].productId", equalTo(1))
                .body("items[0].quantity", equalTo(5));

        assertProductQuantity(15);
    }

    @Test
    void shouldDecreaseProductQuantityWhenExitMovementIsCreated() {
        createStockMovement("""
                {
                    "type": "EXIT",
                    "exitReason": "DAMAGED",
                    "observation": "Integration test exit",
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 4
                        }
                    ]
                }
                """)
                .statusCode(201)
                .body("id", notNullValue())
                .body("type", equalTo("EXIT"))
                .body("exitReason", equalTo("DAMAGED"))
                .body("items[0].quantity", equalTo(4));

        assertProductQuantity(6);
    }

    @Test
    void shouldSetProductQuantityWhenAdjustmentMovementIsCreated() {
        createStockMovement("""
                {
                    "type": "ADJUSTMENT",
                    "reason": "Inventory count",
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 7
                        }
                    ]
                }
                """)
                .statusCode(201)
                .body("id", notNullValue())
                .body("type", equalTo("ADJUSTMENT"))
                .body("reason", equalTo("Inventory count"))
                .body("items[0].quantity", equalTo(7))
                .body("items[0].quantityBefore", equalTo(10))
                .body("items[0].quantityDifference", equalTo(-3));

        assertProductQuantity(7);
    }

    @Test
    void shouldReturn422WhenExitMovementHasInsufficientStock() {
        createStockMovement("""
                {
                    "type": "EXIT",
                    "exitReason": "LOST",
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
    void shouldReturn400WhenMovementTypeIsSale() {
        createStockMovement("""
                {
                    "type": "SALE",
                    "items": [
                        {
                            "productId": 1,
                            "quantity": 1
                        }
                    ]
                }
                """)
                .statusCode(400);

        assertProductQuantity(10);
    }

    private io.restassured.response.ValidatableResponse createStockMovement(String body) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/stock-movements")
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
