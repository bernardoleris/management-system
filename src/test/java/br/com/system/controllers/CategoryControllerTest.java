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
class CategoryControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateCategory() {
        createCategory("  New Category  ")
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("New Category"));
    }

    @Test
    void shouldReturn409WhenCategoryNameExistsIgnoringCaseAndSpaces() {
        createCategory("  test category  ")
                .statusCode(409)
                .body("message", equalTo("Category name already registered!"));
    }

    @Test
    void shouldReturn409WhenUpdatingCategoryToAnExistingName() {
        createCategory("Other Category").statusCode(201);

        given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON).body(nameBody("OTHER CATEGORY"))
                .when().put("/categories/{id}", 1)
                .then().statusCode(409).body("message", equalTo("Category name already registered!"));
    }

    @Test
    void shouldUpdateCategory() {
        given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON).body(nameBody("Updated Category"))
                .when().put("/categories/{id}", 1)
                .then().statusCode(200).body("id", equalTo(1)).body("name", equalTo("Updated Category"));
    }

    @Test
    void shouldListAndFindCategory() {
        given().auth().oauth2(ownerAccessToken()).when().get("/categories")
                .then().statusCode(200).body("size()", equalTo(1)).body("[0].name", equalTo("Test Category"));

        given().auth().oauth2(ownerAccessToken()).when().get("/categories/{id}", 1)
                .then().statusCode(200).body("name", equalTo("Test Category"));
    }

    @Test
    void shouldDeleteCategoryWithoutProducts() {
        Long categoryId = createCategory("Disposable Category").statusCode(201).extract().jsonPath().getLong("id");

        given().auth().oauth2(ownerAccessToken()).when().delete("/categories/{id}", categoryId)
                .then().statusCode(204);

        given().auth().oauth2(ownerAccessToken()).when().get("/categories/{id}", categoryId)
                .then().statusCode(404);
    }

    @Test
    void shouldReturn400WhenCategoryNameIsInvalid() {
        createCategory(" ").statusCode(400).body("message", equalTo("Validation failed"));
    }

    @Test
    void shouldReturn404WhenCategoryDoesNotExist() {
        given().auth().oauth2(ownerAccessToken()).when().get("/categories/{id}", 999)
                .then().statusCode(404).body("message", equalTo("No category found for this ID!"));
    }

    @Test
    void shouldRequireAuthenticationForCategories() {
        given().when().get("/categories").then().statusCode(401);
    }

    private io.restassured.response.ValidatableResponse createCategory(String name) {
        return given().auth().oauth2(ownerAccessToken()).contentType(ContentType.JSON).body(nameBody(name))
                .when().post("/categories").then().log().ifValidationFails();
    }

    private String nameBody(String name) {
        return "{ \"name\": \"%s\" }".formatted(name);
    }
}
