package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.Auth;
import com.h4j4x.expenses.api.repository.UserRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.smallrye.mutiny.Uni;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
@QuarkusTestResource(H2DatabaseTestResource.class)
public class UserResourceTests {
    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASSWORD = "12345678";

    @InjectMock
    UserRepository userRepo;

    @BeforeEach
    void beforeEach() {
        var user = new UserEntity();
        user.setId(1L);
        user.setName("TEST");
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        Mockito
            .when(userRepo.findByEmail(TEST_EMAIL))
            .thenReturn(Uni.createFrom().item(user));
    }

    @Test
    public void whenGetMe_Anonymous_Then_Throw401() {
        RestAssured.given()
            .when().get(UserResource.ME)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void whenPostSignIn_Then_ReturnJwtToken() {
        var auth = new Auth(TEST_EMAIL, TEST_PASSWORD);
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
        var auth = new Auth(TEST_EMAIL, TEST_PASSWORD);
        var token = RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(auth).post(UserResource.SIGN_IN).body();
        return token.asString();
    }
}
