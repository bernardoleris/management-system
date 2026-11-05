package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
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

    @Test
    @Sql(scripts = "classpath:sql/seed-second-company.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldListAvailableCompaniesAndSwitchTheActiveCompany() {
        String companyBToken = accessToken("owner-empresa-b", "password");

        given().auth().oauth2(companyBToken).contentType(ContentType.JSON)
                .body("{\"login\":\"admin\",\"role\":\"ADMIN\"}")
                .when().post("/company/members/existing")
                .then().statusCode(201);

        String defaultCompanyToken = ownerAccessToken();

        List<Map<String, Object>> companies = given().auth().oauth2(defaultCompanyToken)
                .when().get("/auth/companies")
                .then().statusCode(200)
                .body("companyName", hasItem("Empresa B"))
                .extract().jsonPath().getList("$");

        long companyBId = companies.stream()
                .filter(company -> "Empresa B".equals(company.get("companyName")))
                .map(company -> ((Number) company.get("companyId")).longValue())
                .findFirst()
                .orElseThrow();

        String companyBAccessToken = given().auth().oauth2(defaultCompanyToken).contentType(ContentType.JSON)
                .body("{\"companyId\":%d}".formatted(companyBId))
                .when().post("/auth/switch-company")
                .then().statusCode(200)
                .body("companyId", equalTo((int) companyBId))
                .body("companyName", equalTo("Empresa B"))
                .extract().path("accessToken");

        given().auth().oauth2(companyBAccessToken)
                .when().get("/categories")
                .then().statusCode(200)
                .body("name", hasItem("Category From Company B"));
    }

    @Test
    @Sql(scripts = "classpath:sql/seed-second-company.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldRejectSwitchToACompanyWithoutAnActiveMembership() {
        given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON)
                .body("{\"companyId\":999999}")
                .when().post("/auth/switch-company")
                .then().statusCode(400)
                .body("message", equalTo("No active access to this company!"));
    }
}
