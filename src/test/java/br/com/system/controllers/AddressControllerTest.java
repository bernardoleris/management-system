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
class AddressControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateAddressAndLinkItToClient() {
        createAddress(1L, addressBody("Rua Inicial", "100", "Sao Paulo"))
                .statusCode(201)
                .body("id", notNullValue())
                .body("street", equalTo("Rua Inicial"))
                .body("number", equalTo("100"))
                .body("neighborhood", equalTo("Centro"))
                .body("city", equalTo("Sao Paulo"))
                .body("state", equalTo("SP"))
                .body("zipCode", equalTo("01001000"));

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/clients/{id}/address", 1)
                .then()
                .statusCode(200)
                .body("street", equalTo("Rua Inicial"));
    }

    @Test
    void shouldUpdateClientAddress() {
        createAddress(1L, addressBody("Rua Inicial", "100", "Sao Paulo"))
                .statusCode(201);

        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(addressBody("Avenida Atualizada", "200", "Campinas"))
                .when()
                .put("/clients/{id}/address", 1)
                .then()
                .statusCode(200)
                .body("street", equalTo("Avenida Atualizada"))
                .body("number", equalTo("200"))
                .body("city", equalTo("Campinas"));
    }

    @Test
    void shouldDeleteClientAddress() {
        createAddress(1L, addressBody("Rua para Remover", "100", "Sao Paulo"))
                .statusCode(201);

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .delete("/clients/{id}/address", 1)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(addressBody("Nova Rua", "300", "Santos"))
                .when()
                .post("/clients/{id}/address", 1)
                .then()
                .statusCode(201)
                .body("street", equalTo("Nova Rua"));
    }

    @Test
    void shouldReturn400WhenAddressIsInvalid() {
        createAddress(1L, """
                {
                    "street": "",
                    "number": "",
                    "neighborhood": "",
                    "city": "",
                    "state": "S",
                    "zipCode": "123"
                }
                """)
                .statusCode(400)
                .body("message", equalTo("Validation failed"));
    }

    @Test
    void shouldReturn404WhenClientDoesNotExist() {
        createAddress(999L, addressBody("Rua Inexistente", "1", "Sao Paulo"))
                .statusCode(404)
                .body("message", equalTo("No client found for this ID!"));
    }

    @Test
    void shouldReturn404WhenUpdatingOrDeletingAddressThatDoesNotExist() {
        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(addressBody("Rua", "1", "Sao Paulo"))
                .when()
                .put("/clients/{id}/address", 1)
                .then()
                .statusCode(404)
                .body("message", equalTo("No address found for this client!"));

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .delete("/clients/{id}/address", 1)
                .then()
                .statusCode(404)
                .body("message", equalTo("No address found for this client!"));
    }

    @Test
    void shouldRequireAuthenticationForAddressEndpoints() {
        given()
                .when()
                .get("/clients/{id}/address", 1)
                .then()
                .statusCode(401);
    }

    private io.restassured.response.ValidatableResponse createAddress(Long clientId, String body) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/clients/{id}/address", clientId)
                .then()
                .log().ifValidationFails();
    }

    private String addressBody(String street, String number, String city) {
        return """
                {
                    "street": "%s",
                    "number": "%s",
                    "complement": "Apartment 1",
                    "neighborhood": "Centro",
                    "city": "%s",
                    "state": "SP",
                    "zipCode": "01001000"
                }
                """.formatted(street, number, city);
    }
}
