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
@Sql(scripts = "classpath:sql/seed-base.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserEntityControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateUser() {
        createUser(userBody("New", "User", "new.user@test.com"))
                .statusCode(201)
                .body("id", notNullValue())
                .body("firstName", equalTo("New"))
                .body("lastName", equalTo("User"))
                .body("email", equalTo("new.user@test.com"))
                .body("active", equalTo(true));
    }

    @Test
    void shouldUpdateUser() {
        given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON)
                .body(userBody("Updated", "Owner", "updated.owner@test.com"))
                .when().put("/users/{id}", 1)
                .then().statusCode(200)
                .body("id", equalTo(1))
                .body("firstName", equalTo("Updated"))
                .body("email", equalTo("updated.owner@test.com"));
    }

    @Test
    void shouldReturn409WhenEmailIsDuplicated() {
        createUser(userBody("Duplicate", "User", "admin@test.com"))
                .statusCode(409)
                .body("message", equalTo("Email already registered!"));
    }

    @Test
    void shouldListFindAndDeleteUserWithoutRelationships() {
        Long userId = createUser(userBody("Disposable", "User", "disposable.user@test.com"))
                .statusCode(201).extract().jsonPath().getLong("id");

        given().auth().oauth2(ownerAccessToken()).when().get("/users")
                .then().statusCode(200).body("id", notNullValue());

        given().auth().oauth2(ownerAccessToken()).when().get("/users/{id}", userId)
                .then().statusCode(200).body("email", equalTo("disposable.user@test.com"));

        given().auth().oauth2(ownerAccessToken()).when().delete("/users/{id}", userId)
                .then().statusCode(204);

        given().auth().oauth2(ownerAccessToken()).when().get("/users/{id}", userId)
                .then().statusCode(404);
    }

    @Test
    void shouldReturn400WhenUserDataIsInvalid() {
        createUser("""
                { "firstName": "", "lastName": "", "email": "invalid" }
                """)
                .statusCode(400)
                .body("message", equalTo("Validation failed"));
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() {
        given().auth().oauth2(ownerAccessToken()).when().get("/users/{id}", 999)
                .then().statusCode(404).body("message", equalTo("No user found for this ID!"));
    }

    @Test
    void shouldRequireAuthenticationForUsers() {
        given().when().get("/users").then().statusCode(401);
    }

    private io.restassured.response.ValidatableResponse createUser(String body) {
        return given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON).body(body)
                .when().post("/users").then().log().ifValidationFails();
    }

    private String userBody(String firstName, String lastName, String email) {
        return """
                {
                    "firstName": "%s",
                    "lastName": "%s",
                    "email": "%s",
                    "phone": "11999999999"
                }
                """.formatted(firstName, lastName, email);
    }
}
