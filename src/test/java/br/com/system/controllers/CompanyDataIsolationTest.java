package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Sql(
        scripts = "classpath:sql/seed-second-company.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class CompanyDataIsolationTest extends AbstractIntegrationTest {

    @Test
    void shouldOnlyExposeCategoriesFromTheActiveCompany() {
        String defaultCompanyToken = ownerAccessToken();
        String companyBToken = accessToken("owner-empresa-b", "password");

        List<Integer> defaultCompanyCategoryIds = given()
                .auth().oauth2(defaultCompanyToken)
                .when().get("/categories")
                .then().statusCode(200)
                .extract().path("id");

        List<Integer> companyBCategoryIds = given()
                .auth().oauth2(companyBToken)
                .when().get("/categories")
                .then().statusCode(200)
                .extract().path("id");

        assertEquals(List.of(1), defaultCompanyCategoryIds);
        assertEquals(List.of(2), companyBCategoryIds);

        given().auth().oauth2(defaultCompanyToken)
                .when().get("/categories/2")
                .then().statusCode(404);
    }

    @Test
    void shouldAllowTheSameClientEmailAndDocumentInDifferentCompanies() {
        String companyBToken = accessToken("owner-empresa-b", "password");

        Integer clientId = given()
                .auth().oauth2(companyBToken)
                .contentType(JSON)
                .body("""
                        {
                          "firstName": "Client",
                          "lastName": "From Company B",
                          "email": "client@test.com",
                          "phone": "11988888888",
                          "documentType": "CPF",
                          "documentNumber": "12345678901",
                          "birthDate": "1990-01-01"
                        }
                        """)
                .when().post("/clients")
                .then().statusCode(201)
                .extract().path("id");

        given().auth().oauth2(ownerAccessToken())
                .when().get("/clients/" + clientId)
                .then().statusCode(404);
    }
}
