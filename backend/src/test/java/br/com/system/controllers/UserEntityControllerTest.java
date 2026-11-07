package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;

class UserEntityControllerTest extends AbstractIntegrationTest {

    @Test
    @Sql(scripts = "classpath:sql/seed-second-company.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldOnlyExposeUsersLinkedToTheActiveCompany() {
        String defaultCompanyToken = ownerAccessToken();
        String companyBToken = accessToken("owner-empresa-b", "password");

        given().auth().oauth2(defaultCompanyToken)
                .when().get("/users")
                .then().statusCode(200)
                .body("id", contains(1, 2));

        given().auth().oauth2(companyBToken)
                .when().get("/users")
                .then().statusCode(200)
                .body("id", contains(10, 11));

        given().auth().oauth2(defaultCompanyToken)
                .when().get("/users/{id}", 10)
                .then().statusCode(404);

        given().auth().oauth2(companyBToken)
                .when().get("/users/{id}", 1)
                .then().statusCode(404);
    }

    @Test
    void shouldReturn404WhenUserDoesNotExist() {
        given().auth().oauth2(ownerAccessToken()).when().get("/users/{id}", 999)
                .then().statusCode(404);
    }

    @Test
    void shouldRequireAuthenticationForUsers() {
        given().when().get("/users").then().statusCode(401);
    }

    @Test
    void shouldRejectLegacyUserMutations() {
        given().auth().oauth2(ownerAccessToken())
                .when().post("/users")
                .then().statusCode(405);

        given().auth().oauth2(ownerAccessToken())
                .when().put("/users/{id}", 1)
                .then().statusCode(405);

        given().auth().oauth2(ownerAccessToken())
                .when().delete("/users/{id}", 1)
                .then().statusCode(405);
    }
}
