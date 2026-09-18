package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(
        scripts = "classpath:sql/seed-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class AdministratorControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldReturnAllAdministratorsWhenAuthenticated() {
        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/administrators")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("login", hasItem("admin"));
    }

    @Test
    void shouldReturnAdministratorByIdWhenAuthenticated() {
        Long id = 1L;

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/administrators/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id.intValue()))
                .body("login", equalTo("admin"))
                .body("user.email", equalTo("admin@test.com"))
                .body("permissions", hasItem("ROLE_OWNER"));
    }

    @Test
    void shouldCreateAdministratorWhenRequestIsValid() {
        String suffix = uniqueSuffix();
        String login = "admin_" + suffix;
        String email = "admin." + suffix + "@test.com";

        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                {
                    "firstName": "Maria",
                    "lastName": "Silva",
                    "email": "%s",
                    "phone": "11999999999",
                    "login": "%s",
                    "password": "secret123",
                    "permissionId": 1
                }
                """.formatted(email, login))
                .when()
                .post("/administrators")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("login", equalTo(login))
                .body("user.firstName", equalTo("Maria"))
                .body("user.email", equalTo(email))
                .body("permissions", hasItem("ROLE_OWNER"));
    }

    @Test
    void shouldUpdateAdministratorWhenRequestIsValid() {
        Long id = createAdministrator("update_" + uniqueSuffix(), "update." + uniqueSuffix() + "@test.com");

        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                {
                    "firstName": "Ana",
                    "lastName": "Souza",
                    "email": "ana.souza@test.com",
                    "phone": "11988887777",
                    "login": "admin_updated",
                    "permissionId": 1
                }
                """)
                .when()
                .put("/administrators/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id.intValue()))
                .body("login", equalTo("admin_updated"))
                .body("user.firstName", equalTo("Ana"))
                .body("user.email", equalTo("ana.souza@test.com"))
                .body("permissions", hasItem("ROLE_OWNER"));
    }

    @Test
    void shouldToggleAdministratorActiveStatus() {
        Long id = createAdministrator("toggle_" + uniqueSuffix(), "toggle." + uniqueSuffix() + "@test.com");

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .patch("/administrators/{id}/toggle-active", id)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .get("/administrators/{id}", id)
                .then()
                .statusCode(200)
                .body("user.active", equalTo(false));
    }

    @Test
    void shouldChangePasswordWhenRequestIsValid() {
        String suffix = uniqueSuffix();
        String login = "password_" + suffix;
        String email = "password." + suffix + "@test.com";
        String currentPassword = "oldPass123";
        String newPassword = "newPass123";

        Long id = createAdministrator(login, email, currentPassword);

        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                {
                    "currentPassword": "%s",
                    "newPassword": "%s",
                    "confirmPassword": "%s"
                }
                """.formatted(currentPassword, newPassword, newPassword))
                .when()
                .patch("/administrators/{id}/password", id)
                .then()
                .statusCode(204);

        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "login": "%s",
                    "password": "%s"
                }
                """.formatted(login, newPassword))
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("authenticated", equalTo(true));
    }

    private Long createAdministrator(String login, String email) {
        return createAdministrator(login, email, "secret123");
    }

    private Long createAdministrator(String login, String email, String password) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body("""
                {
                    "firstName": "Test",
                    "lastName": "Admin",
                    "email": "%s",
                    "phone": "11999990000",
                    "login": "%s",
                    "password": "%s",
                    "permissionId": 1
                }
                """.formatted(email, login, password))
                .when()
                .post("/administrators")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
