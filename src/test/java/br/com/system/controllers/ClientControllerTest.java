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
class ClientControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateClientWithAddress() {
        createClient(clientBody("New", "Client", "new.client@test.com", "98765432100", true))
                .statusCode(201)
                .body("id", notNullValue())
                .body("documentType", equalTo("CPF"))
                .body("documentNumber", equalTo("98765432100"))
                .body("birthDate", equalTo("1995-05-10"))
                .body("user.firstName", equalTo("New"))
                .body("user.lastName", equalTo("Client"))
                .body("user.email", equalTo("new.client@test.com"))
                .body("user.active", equalTo(true))
                .body("address.street", equalTo("Rua dos Testes"))
                .body("address.city", equalTo("Sao Paulo"));
    }

    @Test
    void shouldUpdateClientPersonalAndDocumentData() {
        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(clientBody("Updated", "Client", "updated.client@test.com", "98765432100", false))
                .when()
                .put("/clients/{id}", 1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("documentNumber", equalTo("98765432100"))
                .body("user.firstName", equalTo("Updated"))
                .body("user.email", equalTo("updated.client@test.com"));
    }

    @Test
    void shouldReturn409WhenEmailOrDocumentIsAlreadyRegistered() {
        createClient(clientBody("Another", "Client", "client@test.com", "98765432100", false))
                .statusCode(409)
                .body("message", equalTo("Email already registered!"));

        createClient(clientBody("Another", "Client", "another.client@test.com", "12345678901", false))
                .statusCode(409)
                .body("message", equalTo("Document number already registered!"));
    }

    @Test
    void shouldReturn400WhenDocumentDoesNotMatchItsType() {
        createClient(clientBody("Invalid", "Cpf", "invalid.cpf@test.com", "123", false))
                .statusCode(400)
                .body("message", equalTo("CPF must have exactly 11 digits!"));
    }

    @Test
    void shouldListAndFindClientById() {
        given()
                .auth().oauth2(ownerAccessToken())
                .queryParam("size", 1)
                .when()
                .get("/clients")
                .then()
                .statusCode(200)
                .body("content.size()", equalTo(1))
                .body("totalElements", equalTo(1));

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/clients/{id}", 1)
                .then()
                .statusCode(200)
                .body("documentNumber", equalTo("12345678901"))
                .body("user.email", equalTo("client@test.com"));
    }

    @Test
    void shouldToggleClientActiveStatus() {
        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .patch("/clients/{id}/toggle-active", 1)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/clients/{id}", 1)
                .then()
                .statusCode(200)
                .body("user.active", equalTo(false));
    }

    @Test
    void shouldReturn404WhenClientDoesNotExist() {
        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/clients/{id}", 999)
                .then()
                .statusCode(404)
                .body("message", equalTo("No client found for this ID!"));
    }

    @Test
    void shouldRequireAuthenticationForClients() {
        given()
                .when()
                .get("/clients")
                .then()
                .statusCode(401);
    }

    private io.restassured.response.ValidatableResponse createClient(String body) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/clients")
                .then()
                .log().ifValidationFails();
    }

    private String clientBody(
            String firstName, String lastName, String email, String documentNumber, boolean withAddress) {
        String address = withAddress ? """
                ,
                "address": {
                    "street": "Rua dos Testes",
                    "number": "123",
                    "neighborhood": "Centro",
                    "city": "Sao Paulo",
                    "state": "SP",
                    "zipCode": "01001000"
                }
                """ : "";

        return """
                {
                    "firstName": "%s",
                    "lastName": "%s",
                    "email": "%s",
                    "phone": "11999999999",
                    "documentType": "CPF",
                    "documentNumber": "%s",
                    "birthDate": "1995-05-10"
                    %s
                }
                """.formatted(firstName, lastName, email, documentNumber, address);
    }
}
