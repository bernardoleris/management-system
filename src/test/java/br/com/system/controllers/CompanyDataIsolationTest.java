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

    @Test
    void shouldRejectAdministratorFiltersFromAnotherCompany() {
        String defaultCompanyToken = ownerAccessToken();

        given().auth().oauth2(defaultCompanyToken)
                .when().get("/alerts/admin/10")
                .then().statusCode(404);

        given().auth().oauth2(defaultCompanyToken)
                .when().get("/sales/admin/10")
                .then().statusCode(404);

        given().auth().oauth2(defaultCompanyToken)
                .when().get("/stock-movements/admin/10")
                .then().statusCode(404);
    }

    @Test
    void shouldRejectRelationshipsWithEntitiesFromAnotherCompany() {
        String defaultCompanyToken = ownerAccessToken();

        given().auth().oauth2(defaultCompanyToken).contentType(JSON)
                .body("""
                        {"name":"Invalid product","barcode":"7890000000003","purchasePrice":10,
                         "salePrice":20,"quantity":1,"categoryId":2,"brandId":1,"supplierId":1}
                        """)
                .when().post("/products")
                .then().statusCode(404);

        given().auth().oauth2(defaultCompanyToken).contentType(JSON)
                .body("""
                        {"clientId":2,"status":"PENDING","paymentMethod":"PIX",
                         "items":[{"productId":1,"quantity":1}]}
                        """)
                .when().post("/sales")
                .then().statusCode(404);

        given().auth().oauth2(defaultCompanyToken).contentType(JSON)
                .body("""
                        {"status":"PENDING","paymentMethod":"PIX",
                         "items":[{"productId":2,"quantity":1}]}
                        """)
                .when().post("/sales")
                .then().statusCode(404);

        given().auth().oauth2(defaultCompanyToken).contentType(JSON)
                .body("""
                        {"type":"ENTRY","supplierId":2,"items":[{"productId":1,"quantity":1}]}
                        """)
                .when().post("/stock-movements")
                .then().statusCode(404);
    }

}
