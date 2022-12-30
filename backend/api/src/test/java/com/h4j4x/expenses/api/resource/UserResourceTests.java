package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.service.UserService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.smallrye.mutiny.Uni;
import javax.ws.rs.BadRequestException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(UserResource.class)
public class UserResourceTests {
    private static final String TEST_NAME = "test";
    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASSWORD = "12345678";

    @InjectMock
    UserService userService;

    @BeforeEach
    void setUp() {
        var user = new UserEntity(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
        user.setId(1L);
        Mockito
            .when(userService.findUserByEmail(TEST_EMAIL))
            .thenReturn(Uni.createFrom().item(user));
        Mockito
            .when(userService.findUserByEmailAndPassword(TEST_EMAIL, TEST_PASSWORD))
            .thenReturn(Uni.createFrom().item(user));
    }

    @Test
    public void whenPostSignUp_WithTestUserEmail_Then_ShouldThrow400() {
        var user = new UserDTO(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
        Mockito
            .when(userService.createUser(TEST_NAME, TEST_EMAIL, TEST_PASSWORD))
            .thenThrow(new BadRequestException(UserService.USER_EMAIL_EXISTS_MESSAGE));
        RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(user).post(UserResource.SIGN_UP)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);

        Mockito.verify(userService).createUser(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignUp_WithOtherEmail_Then_ShouldGetJwtToken() {
        var user = new UserDTO(TEST_NAME, "other-" + TEST_EMAIL, TEST_PASSWORD);
        var userEntity = new UserEntity(user.getName(), user.getEmail(), user.getPassword());
        userEntity.setId(2L);
        Mockito
            .when(userService.createUser(TEST_NAME, user.getEmail(), TEST_PASSWORD))
            .thenReturn(Uni.createFrom().item(userEntity));
        RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(user).post(UserResource.SIGN_UP)
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .body(not(blankOrNullString()));

        Mockito.verify(userService).createUser(TEST_NAME, user.getEmail(), TEST_PASSWORD);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignIn_WithCorrectCredentials_Then_ShouldGetJwtToken() {
        var credentials = new UserCredentials(TEST_EMAIL, TEST_PASSWORD);
        RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(credentials).post(UserResource.SIGN_IN)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(not(blankOrNullString()));

        Mockito.verify(userService).findUserByEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignIn_WithInvalidCredentials_Then_ShouldThrow401() {
        var credentials = new UserCredentials(TEST_EMAIL, TEST_PASSWORD + "-");
        RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(credentials).post(UserResource.SIGN_IN)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

        Mockito.verify(userService).findUserByEmailAndPassword(TEST_EMAIL, TEST_PASSWORD + "-");
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenGetMe_Anonymous_Then_ShouldThrow401() {
        RestAssured.given()
            .when().get(UserResource.ME)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenGetMe_Authenticated_Then_ShouldGetUserData() {
        var credentials = new UserCredentials(TEST_EMAIL, TEST_PASSWORD);
        var token = RestAssured.given()
            .headers("Content-Type", "application/json")
            .when().body(credentials).post(UserResource.SIGN_IN).body();
        var authToken = token.asString();

        RestAssured.given()
            .headers("Authorization", "Bearer " + authToken)
            .when().get(UserResource.ME)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("email", is(TEST_EMAIL));

        Mockito.verify(userService).findUserByEmailAndPassword(TEST_EMAIL, TEST_PASSWORD);
        Mockito.verify(userService).findUserByEmail(TEST_EMAIL);
        Mockito.verifyNoMoreInteractions(userService);
    }
}
