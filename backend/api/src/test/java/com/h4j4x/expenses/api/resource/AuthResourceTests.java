package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.DataGen;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.model.UserToken;
import com.h4j4x.expenses.api.service.UserService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import javax.ws.rs.BadRequestException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(AuthResource.class)
public class AuthResourceTests extends DataGen {
    @InjectMock
    UserService userService;
    private final String TEST_NAME = genUserName();
    private final String TEST_EMAIL = genUserEmail();
    private final String TEST_PASSWORD = genUserPassword();

    @BeforeEach
    void setUp() {
        var user = new UserEntity(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
        user.setId(1L);
        Mockito
            .when(userService.findUserByEmail(TEST_EMAIL))
            .thenReturn(Uni.createFrom().item(user));
        Mockito
            .when(userService.findUserByEmailAndPassword(new UserCredentials(TEST_EMAIL, TEST_PASSWORD)))
            .thenReturn(Uni.createFrom().item(user));
    }

    @Test
    public void whenPostSignUp_WithTestUserEmail_Then_ShouldThrow400() {
        var user = new UserDTO(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
        Mockito
            .when(userService.createUser(user))
            .thenThrow(new BadRequestException(UserService.USER_EMAIL_EXISTS_MESSAGE));
        RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(user).post(AuthResource.SIGN_UP)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);

        Mockito.verify(userService).createUser(user);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignUp_WithOtherEmail_Then_ShouldGetJwtToken() {
        var user = new UserDTO(TEST_NAME, "other-" + TEST_EMAIL, TEST_PASSWORD);
        var userEntity = new UserEntity(user.getName(), user.getEmail(), user.getPassword());
        userEntity.setId(2L);
        Mockito
            .when(userService.createUser(user))
            .thenReturn(Uni.createFrom().item(userEntity));
        RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(user).post(AuthResource.SIGN_UP)
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .body(not(blankOrNullString()));

        Mockito.verify(userService).createUser(user);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignIn_WithCorrectCredentials_Then_ShouldGetJwtToken() {
        var credentials = new UserCredentials(TEST_EMAIL, TEST_PASSWORD);
        RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(credentials).post(AuthResource.SIGN_IN)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(not(blankOrNullString()));

        Mockito.verify(userService).findUserByEmailAndPassword(credentials);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignIn_WithInvalidCredentials_Then_ShouldThrow401() {
        var credentials = new UserCredentials(TEST_EMAIL, TEST_PASSWORD + "-");
        RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(credentials).post(AuthResource.SIGN_IN)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

        Mockito.verify(userService).findUserByEmailAndPassword(credentials);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenGetMe_Anonymous_Then_ShouldThrow401() {
        RestAssured.given()
            .when().get(AuthResource.ME)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenGetMe_Authenticated_Then_ShouldGetUserData() {
        var credentials = new UserCredentials(TEST_EMAIL, TEST_PASSWORD);
        var token = RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(credentials).post(AuthResource.SIGN_IN)
            .then().extract().as(UserToken.class);

        RestAssured.given()
            .headers("Authorization", "Bearer " + token.token())
            .when().get(AuthResource.ME)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("email", is(TEST_EMAIL));

        Mockito.verify(userService).findUserByEmailAndPassword(credentials);
        Mockito.verify(userService).findUserByEmail(TEST_EMAIL);
        Mockito.verifyNoMoreInteractions(userService);
    }
}
