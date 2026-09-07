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
