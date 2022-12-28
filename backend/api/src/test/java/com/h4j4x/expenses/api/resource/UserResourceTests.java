package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.model.Auth;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
public class UserResourceTests {
    static final String TEST_EMAIL = "test@mail.com";

    @Test
    public void whenGetMe_Anonymous_Then_Throw401() {
        RestAssured.given()
            .when().get(UserResource.ME)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void whenPostSignIn_Then_ReturnJwtToken() {
        var auth = new Auth(TEST_EMAIL, "");
        RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(auth).post(UserResource.SIGN_IN)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(not(blankOrNullString()));
    }

    @Test
    public void whenGetMe_AuthenticatedWithUserRole_Then_ReturnUserData() {
        RestAssured.given()
            .headers("Authorization", "Bearer " + authToken())
            .when().get(UserResource.ME)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("email", is(TEST_EMAIL));
    }

    private String authToken() {
        var auth = new Auth(TEST_EMAIL, "");
        var token = RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(auth).post(UserResource.SIGN_IN).body();
        return token.asString();
    }
}
