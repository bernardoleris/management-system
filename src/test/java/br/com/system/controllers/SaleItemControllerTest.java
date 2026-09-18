package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(
        scripts = "classpath:sql/seed-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class SaleItemControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldReturn422AndKeepStockWhenCompletedSaleItemExceedsAvailableStock() {
        Long saleId = createCompletedSaleWithAllStock();

        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "productId": 1,
                            "quantity": 1
                        }
                        """)
                .when()
                .post("/sales/{saleId}/items", saleId)
                .then()
                .statusCode(422);

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/products/{id}", 1)
                .then()
                .statusCode(200)
                .body("quantity", equalTo(0));

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/sales/{saleId}/items", saleId)
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }

    private Long createCompletedSaleWithAllStock() {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "status": "COMPLETED",
                            "paymentMethod": "PIX",
                            "items": [
                                {
                                    "productId": 1,
                                    "quantity": 10
                                }
                            ]
                        }
                        """)
                .when()
                .post("/sales")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }
}
