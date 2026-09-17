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
class BrandControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateBrand() {
        createBrand("  New Brand  ")
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("New Brand"));
    }

    @Test
    void shouldReturn409WhenBrandNameExistsIgnoringCaseAndSpaces() {
        createBrand("  test brand  ")
                .statusCode(409)
                .body("message", equalTo("Brand name already registered!"));
    }

    @Test
    void shouldReturn409WhenUpdatingBrandToAnExistingName() {
        createBrand("Other Brand").statusCode(201);

        given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON).body(nameBody("OTHER BRAND"))
                .when().put("/brands/{id}", 1)
                .then().statusCode(409).body("message", equalTo("Brand name already registered!"));
    }

    @Test
    void shouldUpdateBrand() {
        given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON).body(nameBody("Updated Brand"))
                .when().put("/brands/{id}", 1)
                .then().statusCode(200).body("id", equalTo(1)).body("name", equalTo("Updated Brand"));
    }

    @Test
    void shouldListAndFindBrand() {
        given().auth().oauth2(ownerAccessToken()).when().get("/brands")
                .then().statusCode(200).body("size()", equalTo(1)).body("[0].name", equalTo("Test Brand"));

        given().auth().oauth2(ownerAccessToken()).when().get("/brands/{id}", 1)
                .then().statusCode(200).body("name", equalTo("Test Brand"));
    }

    @Test
    void shouldDeleteBrandWithoutProducts() {
        Long brandId = createBrand("Disposable Brand").statusCode(201).extract().jsonPath().getLong("id");

        given().auth().oauth2(ownerAccessToken()).when().delete("/brands/{id}", brandId)
                .then().statusCode(204);

        given().auth().oauth2(ownerAccessToken()).when().get("/brands/{id}", brandId)
                .then().statusCode(404);
    }

    @Test
    void shouldReturn400WhenBrandNameIsInvalid() {
        createBrand(" ").statusCode(400).body("message", equalTo("Validation failed"));
    }

    @Test
    void shouldReturn404WhenBrandDoesNotExist() {
        given().auth().oauth2(ownerAccessToken()).when().get("/brands/{id}", 999)
                .then().statusCode(404).body("message", equalTo("No brand found for this ID!"));
    }

    @Test
    void shouldRequireAuthenticationForBrands() {
        given().when().get("/brands").then().statusCode(401);
    }

    private io.restassured.response.ValidatableResponse createBrand(String name) {
        return given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON).body(nameBody(name))
                .when().post("/brands").then().log().ifValidationFails();
    }

    private String nameBody(String name) {
        return "{ \"name\": \"%s\" }".formatted(name);
    }
}
