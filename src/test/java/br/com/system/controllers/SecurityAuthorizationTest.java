package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(
        scripts = {"classpath:sql/seed-base.sql", "classpath:sql/seed-permissions.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class SecurityAuthorizationTest extends AbstractIntegrationTest {

    @Test
    void shouldAllowAdminOnOwnerAndAdminRoutesButNotOwnerOnlyRoutes() {
        String adminToken = accessToken("admin-role", "password");

        given().auth().oauth2(adminToken).when().get("/analytics/sales-by-month").then().statusCode(200);
        given().auth().oauth2(adminToken).when().get("/suppliers").then().statusCode(200);
        given().auth().oauth2(adminToken).when().get("/categories").then().statusCode(200);
        given().auth().oauth2(adminToken).when().get("/brands").then().statusCode(200);
        given().auth().oauth2(adminToken).when().get("/users").then().statusCode(200);
        given().auth().oauth2(adminToken).when().get("/administrators").then().statusCode(403);
    }

    @Test
    void shouldAllowOperatorOnlyOnOperationalRoutes() {
        String operatorToken = accessToken("operator-role", "password");

        given().auth().oauth2(operatorToken).when().get("/products").then().statusCode(200);
        given().auth().oauth2(operatorToken).when().get("/sales").then().statusCode(200);
        given().auth().oauth2(operatorToken).when().get("/clients").then().statusCode(200);
        given().auth().oauth2(operatorToken).when().get("/stock-movements").then().statusCode(200);
        given().auth().oauth2(operatorToken).when().get("/alerts").then().statusCode(200);

        given().auth().oauth2(operatorToken).when().get("/analytics/sales-by-month").then().statusCode(403);
        given().auth().oauth2(operatorToken).when().get("/suppliers").then().statusCode(403);
        given().auth().oauth2(operatorToken).when().get("/categories").then().statusCode(403);
        given().auth().oauth2(operatorToken).when().get("/brands").then().statusCode(403);
        given().auth().oauth2(operatorToken).when().get("/users").then().statusCode(403);
        given().auth().oauth2(operatorToken).when().get("/administrators").then().statusCode(403);
    }
}
