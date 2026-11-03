package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldReturnTokenWhenLoginIsValid() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "login": "admin",
                    "password": "password"
                }
                """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("companyId", notNullValue())
                .body("companyName", equalTo("Empresa padrão"))
                .body("authenticated", equalTo(true));
    }

    @Test
    void shouldReturn400WhenLoginIsInvalid() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "login": "wrong",
                    "password": "wrong"
                }
                """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn400WhenLoginIsMissing() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                {
                    "password": "123456"
                }
                """)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(400);
    }
}
