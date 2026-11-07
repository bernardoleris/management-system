package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import io.restassured.http.ContentType;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@Sql(
        scripts = "classpath:sql/seed-company-roles.sql",
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
    }

    @Test
    void shouldAllowOperatorOnlyOnOperationalRoutes() {
        String operatorToken = accessToken("operator-role", "password");

        given().auth().oauth2(operatorToken).when().get("/products").then().statusCode(200);
        given().auth().oauth2(operatorToken).when().get("/sales").then().statusCode(200);
        given().auth().oauth2(operatorToken).when().get("/clients").then().statusCode(200);
        given().auth().oauth2(operatorToken).when().get("/stock-movements").then().statusCode(200);
        given().auth().oauth2(operatorToken).when().get("/alerts").then().statusCode(200);

        given().auth().oauth2(operatorToken).when().get("/analytics/sales-by-month").then()
                .statusCode(403)
                .contentType(ContentType.JSON)
                .body("status", equalTo(403))
                .body("message", equalTo("Access denied! You don't have permission to access this resource."))
                .body("details", equalTo("uri=/analytics/sales-by-month"));
        given().auth().oauth2(operatorToken).when().get("/suppliers").then().statusCode(403);
        given().auth().oauth2(operatorToken).when().get("/categories").then().statusCode(403);
        given().auth().oauth2(operatorToken).when().get("/brands").then().statusCode(403);
        given().auth().oauth2(operatorToken).when().get("/users").then().statusCode(403);
    }

    @Test
    @Sql(scripts = "classpath:sql/membership-role-precedence.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldAuthorizeUsingTheActiveCompanyMembershipRole() {
        String adminToken = accessToken("admin-role", "password");

        given().auth().oauth2(adminToken).when().get("/products").then().statusCode(200);
        given().auth().oauth2(adminToken).when().get("/analytics/sales-by-month").then().statusCode(403);
        given().auth().oauth2(adminToken).when().get("/company/members").then().statusCode(403);
    }
}
