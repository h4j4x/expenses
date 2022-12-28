package com.h4j4x.expenses.api.resource;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
public class UserResourceTests {
    @Test
    public void whenGetMe_Anonymous_Then_Throw401() {
        given()
            .when().get("me")
            .then()
            .statusCode(401);
    }

    @Test
    public void whenPostSignIn_Then_ReturnJwtToken() {
        given()
            .when().post("sign-in")
            .then()
            .statusCode(200)
            .body(not(blankOrNullString()));
    }

    @Test
    @TestSecurity(user = "test@mail.com", roles = "other")
    public void whenGetMe_AuthenticatedWithoutUserRole_Then_Throw403() {
        given()
            .when().get("me")
            .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "test@mail.com", roles = "user")
    public void whenGetMe_AuthenticatedWithUserRole_Then_ReturnUserData() {
        given()
            .when().get("me")
            .then()
            .statusCode(200)
            .body(is("test@mail.com"));
    }
}
