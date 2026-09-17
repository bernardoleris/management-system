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
class ProductControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateProductWithItsRelationships() {
        createProduct(productBody("New Product", "7890000000002", 8))
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("New Product"))
                .body("barcode", equalTo("7890000000002"))
                .body("purchasePrice", equalTo(12.5F))
                .body("salePrice", equalTo(25.0F))
                .body("quantity", equalTo(8))
                .body("active", equalTo(true))
                .body("categoryId", equalTo(1))
                .body("brandId", equalTo(1))
                .body("supplierId", equalTo(1));
    }

    @Test
    void shouldReturn409WhenBarcodeAlreadyExists() {
        createProduct(productBody("Duplicate barcode", "7890000000001", 3))
                .statusCode(409)
                .body("message", equalTo("Barcode already registered!"));
    }

    @Test
    void shouldReturn404WhenProductRelationshipDoesNotExist() {
        createProduct("""
                {
                    "name": "Invalid Product",
                    "purchasePrice": 10.00,
                    "salePrice": 20.00,
                    "quantity": 1,
                    "categoryId": 999,
                    "brandId": 1
                }
                """)
                .statusCode(404)
                .body("message", equalTo("No category found for this ID!"));
    }

    @Test
    void shouldUpdateProductAndKeepItAvailableByBarcode() {
        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(productBody("Updated Product", "7890000000009", 25))
                .when()
                .put("/products/{id}", 1)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated Product"))
                .body("barcode", equalTo("7890000000009"))
                .body("quantity", equalTo(25));

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/products/barcode/{barcode}", "7890000000009")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("Updated Product"));
    }

    @Test
    void shouldFilterActiveProductsByCategoryBrandAndSupplier() {
        given().auth().oauth2(ownerAccessToken()).when().get("/products/active")
                .then().statusCode(200).body("content.size()", equalTo(1));

        given().auth().oauth2(ownerAccessToken()).when().get("/products/category/{id}", 1)
                .then().statusCode(200).body("content.size()", equalTo(1)).body("content[0].id", equalTo(1));

        given().auth().oauth2(ownerAccessToken()).when().get("/products/brand/{id}", 1)
                .then().statusCode(200).body("content.size()", equalTo(1)).body("content[0].id", equalTo(1));

        given().auth().oauth2(ownerAccessToken()).when().get("/products/supplier/{id}", 1)
                .then().statusCode(200).body("content.size()", equalTo(1)).body("content[0].id", equalTo(1));
    }

    @Test
    void shouldToggleProductAndRemoveItFromActiveLists() {
        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .patch("/products/{id}/toggle-active", 1)
                .then()
                .statusCode(204);

        given().auth().oauth2(ownerAccessToken()).when().get("/products/{id}", 1)
                .then().statusCode(200).body("active", equalTo(false));

        given().auth().oauth2(ownerAccessToken()).when().get("/products/active")
                .then().statusCode(200).body("content.size()", equalTo(0));
    }

    @Test
    void shouldReturn400WhenRequiredProductDataIsMissing() {
        createProduct("""
                {
                    "name": "",
                    "salePrice": 0,
                    "quantity": -1
                }
                """)
                .statusCode(400)
                .body("message", equalTo("Validation failed"));
    }

    @Test
    void shouldDeleteProductWithoutLinkedRecords() {
        Long productId = createProduct(productBody("Disposable Product", "7890000000003", 0))
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .delete("/products/{id}", productId)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/products/{id}", productId)
                .then()
                .statusCode(404);
    }

    @Test
    void shouldRequireAuthenticationForProducts() {
        given()
                .when()
                .get("/products")
                .then()
                .statusCode(401);
    }

    private io.restassured.response.ValidatableResponse createProduct(String body) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/products")
                .then()
                .log().ifValidationFails();
    }

    private String productBody(String name, String barcode, int quantity) {
        return """
                {
                    "name": "%s",
                    "description": "Product used by integration tests",
                    "barcode": "%s",
                    "purchasePrice": 12.50,
                    "salePrice": 25.00,
                    "quantity": %d,
                    "categoryId": 1,
                    "brandId": 1,
                    "supplierId": 1
                }
                """.formatted(name, barcode, quantity);
    }
}
