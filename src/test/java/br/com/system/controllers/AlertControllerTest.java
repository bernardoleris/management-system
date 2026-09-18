package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(
        scripts = "classpath:sql/seed-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class AlertControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateActiveUnreadAlertForProduct() {
        createAlert(alertBody("LOW_STOCK", 5, "Product stock is low"))
                .statusCode(201)
                .body("id", notNullValue())
                .body("type", equalTo("LOW_STOCK"))
                .body("minimumQuantity", equalTo(5))
                .body("message", equalTo("Product stock is low"))
                .body("productId", equalTo(1))
                .body("productName", equalTo("Test Product"))
                .body("adminId", equalTo(1))
                .body("adminLogin", equalTo("admin"))
                .body("read", equalTo(false))
                .body("readAt", nullValue())
                .body("active", equalTo(true));
    }

    @Test
    void shouldUpdateAlertData() {
        Long alertId = createAlert(alertBody("LOW_STOCK", 5, "Initial message"))
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(alertBody("OUT_OF_STOCK", 1, "Product is out of stock"))
                .when()
                .put("/alerts/{id}", alertId)
                .then()
                .statusCode(200)
                .body("id", equalTo(alertId.intValue()))
                .body("type", equalTo("OUT_OF_STOCK"))
                .body("minimumQuantity", equalTo(1))
                .body("message", equalTo("Product is out of stock"));
    }

    @Test
    void shouldMarkAlertAsReadAndExcludeItFromUnreadList() {
        Long alertId = createAlert(alertBody("LOW_STOCK", 5, "Unread alert"))
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given().auth().oauth2(ownerAccessToken()).when().get("/alerts/unread")
                .then().statusCode(200).body("size()", equalTo(1));

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .patch("/alerts/{id}/read", alertId)
                .then()
                .statusCode(200)
                .body("read", equalTo(true))
                .body("readAt", notNullValue());

        given().auth().oauth2(ownerAccessToken()).when().get("/alerts/unread")
                .then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    void shouldFindActiveAlertByAdministratorAndProduct() {
        createAlert(alertBody("LOW_STOCK", 5, "Filtered alert")).statusCode(201);

        given().auth().oauth2(ownerAccessToken()).when().get("/alerts/active")
                .then().statusCode(200).body("size()", equalTo(1));

        given().auth().oauth2(ownerAccessToken()).when().get("/alerts/admin/{id}", 1)
                .then().statusCode(200).body("size()", equalTo(1)).body("[0].adminId", equalTo(1));

        given().auth().oauth2(ownerAccessToken()).when().get("/alerts/product/{id}", 1)
                .then().statusCode(200).body("size()", equalTo(1)).body("[0].productId", equalTo(1));
    }

    @Test
    void shouldDeactivateAlertInsteadOfRemovingItsHistory() {
        Long alertId = createAlert(alertBody("LOW_STOCK", 5, "Alert to deactivate"))
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        given()
                .auth().oauth2(ownerAccessToken())
                .when()
                .delete("/alerts/{id}", alertId)
                .then()
                .statusCode(204);

        given().auth().oauth2(ownerAccessToken()).when().get("/alerts/{id}", alertId)
                .then().statusCode(200).body("active", equalTo(false));

        given().auth().oauth2(ownerAccessToken()).when().get("/alerts")
                .then().statusCode(200).body("size()", equalTo(1));

        given().auth().oauth2(ownerAccessToken()).when().get("/alerts/active")
                .then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    void shouldReturn400WhenAlertDataIsInvalid() {
        createAlert("""
                {
                    "type": "LOW_STOCK",
                    "minimumQuantity": 0,
                    "message": ""
                }
                """)
                .statusCode(400)
                .body("message", equalTo("Validation failed"));
    }

    @Test
    void shouldReturn404WhenAlertProductDoesNotExist() {
        createAlert("""
                {
                    "productId": 999,
                    "type": "LOW_STOCK",
                    "minimumQuantity": 5,
                    "message": "Unknown product"
                }
                """)
                .statusCode(404)
                .body("message", equalTo("No product found for this ID!"));
    }

    @Test
    void shouldRequireAuthenticationForAlerts() {
        given()
                .when()
                .get("/alerts")
                .then()
                .statusCode(401);
    }

    private io.restassured.response.ValidatableResponse createAlert(String body) {
        return given()
                .auth().oauth2(ownerAccessToken())
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/alerts")
                .then()
                .log().ifValidationFails();
    }

    private String alertBody(String type, int minimumQuantity, String message) {
        return """
                {
                    "productId": 1,
                    "type": "%s",
                    "minimumQuantity": %d,
                    "message": "%s"
                }
                """.formatted(type, minimumQuantity, message);
    }
}
