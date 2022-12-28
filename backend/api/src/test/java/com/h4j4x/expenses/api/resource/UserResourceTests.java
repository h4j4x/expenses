package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.model.Auth;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
public class UserResourceTests {
    static final String TEST_EMAIL = "test@mail.com";

    @Test
    public void whenGetMe_Anonymous_Then_Throw401() {
        RestAssured.given()
            .when().get("me")
            .then()
            .statusCode(401);
    }

    @Test
    public void whenPostSignIn_Then_ReturnJwtToken() {
        Auth auth = new Auth(TEST_EMAIL, "");
        RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(auth).post("sign-in")
            .then()
            .statusCode(200)
            .body(not(blankOrNullString()));
    }

    @Test
    @TestSecurity(user = TEST_EMAIL, roles = "other")
    public void whenGetMe_AuthenticatedWithoutUserRole_Then_Throw403() {
        RestAssured.given()
            .when().get("me")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = TEST_EMAIL, roles = UserResource.USER_ROLE)
    public void whenGetMe_AuthenticatedWithUserRole_Then_ReturnUserData() {
        RestAssured.given()
            .when().get("me")
            .then()
            .statusCode(200)
            .body(is(TEST_EMAIL));
    }
}
