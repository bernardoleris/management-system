package br.com.system.controllers;

import br.com.system.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

class CompanyMembershipControllerTest extends AbstractIntegrationTest {

    @Test
    void shouldCreateUpdateAndDeactivateMemberInTheActiveCompany() {
        Long membershipId = given().auth().oauth2(ownerAccessToken()).contentType(JSON)
                .body("""
                        {"firstName":"Team","lastName":"Member","email":"team.member@test.com",
                         "phone":"11988887777","login":"team-member","password":"secret123","role":"OPERATOR"}
                        """)
                .when().post("/company/members")
                .then().statusCode(201)
                .body("membershipId", notNullValue())
                .body("login", equalTo("team-member"))
                .body("role", equalTo("OPERATOR"))
                .extract().jsonPath().getLong("membershipId");

        given().auth().oauth2(ownerAccessToken())
                .when().get("/company/members")
                .then().statusCode(200)
                .body("login", hasItem("team-member"));

        given().auth().oauth2(ownerAccessToken()).contentType(JSON)
                .body("{\"role\":\"ADMIN\"}")
                .when().put("/company/members/{membershipId}/role", membershipId)
                .then().statusCode(200)
                .body("role", equalTo("ADMIN"));

        String memberToken = accessToken("team-member", "secret123");

        given().auth().oauth2(ownerAccessToken())
                .when().delete("/company/members/{membershipId}", membershipId)
                .then().statusCode(204);

        given().auth().oauth2(memberToken)
                .when().get("/products")
                .then().statusCode(401);
    }

    @Test
    void shouldNotDeactivateTheLastActiveOwner() {
        Long ownerMembershipId = given().auth().oauth2(ownerAccessToken())
                .when().get("/company/members")
                .then().statusCode(200)
                .extract().jsonPath().getLong("[0].membershipId");

        given().auth().oauth2(ownerAccessToken())
                .when().delete("/company/members/{membershipId}", ownerMembershipId)
                .then().statusCode(400)
                .body("message", equalTo("A company must have at least one active owner!"));
    }
}
