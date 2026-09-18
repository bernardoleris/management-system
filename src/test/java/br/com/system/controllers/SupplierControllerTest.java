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
class SupplierControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateActiveSupplier() {
        createSupplier(supplierBody("New Supplier", "98765432000199", "new@supplier.com"))
                .statusCode(201)
                .body("id", notNullValue())
                .body("tradeName", equalTo("New Supplier"))
                .body("cnpj", equalTo("98765432000199"))
                .body("email", equalTo("new@supplier.com"))
                .body("active", equalTo(true));
    }

    @Test
    void shouldUpdateSupplier() {
        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(supplierBody("Updated Supplier", "12345678000199", "updated@supplier.com"))
                .when()
                .put("/suppliers/{id}", 1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("tradeName", equalTo("Updated Supplier"))
                .body("email", equalTo("updated@supplier.com"));
    }

    @Test
    void shouldReturn409WhenCnpjAlreadyExists() {
        createSupplier(supplierBody("Duplicate Supplier", "12345678000199", "duplicate@supplier.com"))
                .statusCode(409)
                .body("message", equalTo("CNPJ already registered!"));
    }

    @Test
    void shouldListActiveAndDisabledSuppliersAfterToggle() {
        given().auth().oauth2(ownerAccessToken()).when().get("/suppliers/active")
                .then().statusCode(200).body("content.size()", equalTo(1)).body("content[0].id", equalTo(1));

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .patch("/suppliers/{id}/toggle-active", 1)
                .then()
                .statusCode(204);

        given().auth().oauth2(ownerAccessToken()).when().get("/suppliers/active")
                .then().statusCode(200).body("content.size()", equalTo(0));

        given().auth().oauth2(ownerAccessToken()).when().get("/suppliers/disabled")
                .then().statusCode(200).body("content.size()", equalTo(1)).body("content[0].active", equalTo(false));
    }

    @Test
    void shouldListAndFindSupplierById() {
        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("size", 1)
                .when()
                .get("/suppliers")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("totalElements", equalTo(1));

        given().auth().oauth2(ownerAccessToken()).when().get("/suppliers/{id}", 1)
                .then().statusCode(200).body("tradeName", equalTo("Test Supplier"));
    }

    @Test
    void shouldReturn400WhenSupplierDataIsInvalid() {
        createSupplier("""
                {
                    "tradeName": "",
                    "cnpj": "123",
                    "phone": "",
                    "email": "invalid"
                }
                """)
                .statusCode(400)
                .body("message", equalTo("Validation failed"));
    }

    @Test
    void shouldReturn404WhenSupplierDoesNotExist() {
        given().auth().oauth2(ownerAccessToken()).when().get("/suppliers/{id}", 999)
                .then().statusCode(404).body("message", equalTo("No supplier found for this ID!"));
    }

    @Test
    void shouldRequireAuthenticationForSuppliers() {
        given().when().get("/suppliers").then().statusCode(401);
    }

    private io.restassured.response.ValidatableResponse createSupplier(String body) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/suppliers")
                .then()
                .log().ifValidationFails();
    }

    private String supplierBody(String tradeName, String cnpj, String email) {
        return """
                {
                    "tradeName": "%s",
                    "cnpj": "%s",
                    "phone": "11999999999",
                    "email": "%s"
                }
                """.formatted(tradeName, cnpj, email);
    }
}
